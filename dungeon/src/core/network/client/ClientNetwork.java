package core.network.client;

import static core.network.codec.NetworkCodec.deserialize;
import static core.network.codec.NetworkCodec.serialize;
import static core.network.config.NetworkConfig.MAX_TCP_OBJECT_SIZE;
import static core.network.config.NetworkConfig.PROTOCOL_VERSION;
import static core.network.config.NetworkConfig.SAFE_UDP_MTU;
import static core.network.config.NetworkConfig.SNAPSHOT_ACK_EXPLICIT_DELAY_MS;
import static core.network.config.NetworkConfig.TCP_CONNECT_TIMEOUT_MS;
import static core.network.config.NetworkConfig.TCP_INITIAL_BYTES_TO_STRIP;
import static core.network.config.NetworkConfig.TCP_LENGTH_ADJUSTMENT;
import static core.network.config.NetworkConfig.TCP_LENGTH_FIELD_LENGTH;
import static core.network.config.NetworkConfig.TCP_LENGTH_FIELD_OFFSET;

import contrib.entities.CharacterClass;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.NetworkTelemetry;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.SnapshotAck;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.RegisterAck;
import core.network.server.ClientState;
import core.network.server.Session;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty-backed client transport using a single {@link Session} to mirror server-side semantics.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>TCP connection for reliable messages and handshake
 *   <li>UDP socket for unreliable messages (inputs, snapshots)
 *   <li>Inbound queue of deserialized {@link NetworkMessage}s
 *   <li>UDP "registration" (RegisterUdp) retransmits until server observes client's UDP source
 *       address
 *   <li>Expose a {@link Session} instance for message handlers to reply via {@code
 *       session.sendMessage(...)}
 * </ul>
 */
public final class ClientNetwork {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientNetwork.class);

  private static final String LAST_SESSION_FILE_NAME = "last_session.dat";
  private final MessageDispatcher dispatcher = new MessageDispatcher();
  private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
  private final Queue<QueuedNetworkMessage> inboundQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Runnable> lifecycleEvents = new ConcurrentLinkedQueue<>();
  private final Object snapshotAckLock = new Object();

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);

  private volatile Session session;

  private String remoteHost;
  private int port;
  private String username;
  private Optional<CharacterClass> requestedCharacterClass = Optional.empty();

  private EventLoopGroup group;
  private Channel tcp;
  private Channel udp;
  private InetSocketAddress udpRemote;

  // assigned after ConnectAck
  private volatile Short clientId;

  private final UdpRecoveryState udpRecoveryState = new UdpRecoveryState();
  private volatile ScheduledFuture<?> udpMaintenanceFuture;
  private int pendingSnapshotAckTick = -1;
  private long pendingSnapshotAckDeadlineNanos = 0L;
  private int inFlightExplicitSnapshotAckTick = -1;
  private int lastExplicitSnapshotAckTick = -1;
  private int lastPiggybackedSnapshotAckTick = -1;
  private long lastPiggybackedSnapshotAckNanos = 0L;
  private int lastReliablePiggybackedSnapshotAckTick = -1;
  private long snapshotAckGeneration = 0L;

  private record QueuedNetworkMessage(Session session, NetworkMessage message, long receiveNanos) {}

  private record ExplicitSnapshotAck(int serverTick, long generation) {}

  /**
   * Initialize the client network with connection parameters.
   *
   * <p>This method allocates the event loop group and prepares the UDP remote address. It does not
   * open sockets call {@link #start()} to connect.
   *
   * @param host server hostname or IP to connect to
   * @param port server port
   * @param username username used in the TCP ConnectRequest handshake
   * @param characterClass requested character class for the player, or empty to use the server
   *     default
   */
  public void initialize(
      String host, int port, String username, Optional<CharacterClass> characterClass) {
    if (running.get()) {
      LOGGER.warn("ClientNetwork is already running; ignoring re-initialization request.");
      return;
    }
    closeTransportResources();
    resetSnapshotAckState();
    connected.set(false);
    session = null;
    clientId = null;
    udpRecoveryState.enterRetryMode();
    this.remoteHost = host;
    this.port = port;
    this.username = username;
    this.requestedCharacterClass = characterClass == null ? Optional.empty() : characterClass;
    this.group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    this.udpRemote = InetSocketAddress.createUnresolved(host, port);
  }

  /**
   * Start network IO: connect TCP and bind UDP.
   *
   * <p>This method is safe to call once. TCP startup waits up to the configured connect timeout;
   * after TCP connects, Netty performs IO on its own threads and sends the ConnectRequest
   * automatically.
   */
  public void start() {
    if (!running.compareAndSet(false, true)) return;
    if (!startTcp()) {
      return;
    }
    startUdpIfNeeded();
  }

  /**
   * Gracefully shuts down network activity and cancels any outstanding UDP registration retries.
   *
   * @param reason human-readable reason used for lifecycle callbacks and logging
   */
  public void shutdown(String reason) {
    if (!running.compareAndSet(true, false)) {
      closeTransportResources();
      connected.set(false);
      return;
    }
    closeTransportResources();
    connected.set(false);
    resetSnapshotAckState();
    LOGGER.info("ClientNetwork shutdown. Reason: {}", reason);
  }

  /**
   * Returns whether the transport has an active TCP connection.
   *
   * <p>Note: UDP may be bound even if this returns false during transitional states; use lifecycle
   * callbacks to observe connection changes.
   *
   * @return true if TCP is connected, false otherwise
   */
  public boolean isConnected() {
    return connected.get();
  }

  /**
   * Returns the message dispatcher used for registering handlers for inbound messages.
   *
   * <p>Handlers registered here will be invoked on the game thread.
   *
   * @return message dispatcher
   */
  public MessageDispatcher dispatcher() {
    return dispatcher;
  }

  /**
   * Returns the current {@link Session} instance if not yet established.
   *
   * <p>The session becomes available after the TCP connection is established and the ConnectAck is
   * received.
   *
   * @return current Session (or empty if not yet established)
   */
  public Session session() {
    return session;
  }

  UdpRecoveryState udpRecoveryState() {
    return udpRecoveryState;
  }

  /**
   * Returns the client id assigned by the server, or 0 if not yet assigned.
   *
   * <p>The id becomes available after receiving a {@link ConnectAck}.
   *
   * @return client id, or 0 if not yet assigned
   */
  public Short clientId() {
    Short id = clientId;
    return id != null ? id : 0;
  }

  /**
   * Send a reliable {@link NetworkMessage} over TCP to the server.
   *
   * <p>Encodes the message using protobuf and writes a 4-byte length-prefixed frame. Drops the
   * message if it exceeds the configured TCP object size.
   *
   * @param msg message to send
   * @return CompletableFuture that completes with true if the message was sent, false if
   *     serialization failed or the message was dropped
   */
  public CompletableFuture<Boolean> sendReliable(NetworkMessage msg) {
    return send(msg, true);
  }

  /**
   * Send a {@link NetworkMessage} to the server honoring the requested reliability.
   *
   * @param msg message to send
   * @param reliable true to force TCP, false to prefer UDP with transparent TCP fallback
   * @return CompletableFuture indicating whether the message send succeeded
   */
  public CompletableFuture<Boolean> send(NetworkMessage msg, boolean reliable) {
    final CompletableFuture<Boolean> result = new CompletableFuture<>();
    if (!running.get() || !isConnected() || tcp == null || !tcp.isActive() || session == null) {
      LOGGER.warn("TCP not active; cannot send {} message", reliable ? "reliable" : "fallback");
      result.complete(false);
      return result;
    }

    try {
      NetworkMessage message = withSnapshotAck(msg);
      long snapshotAckGeneration = snapshotAckGeneration();
      return session
          .sendMessageWithTransport(message, reliable)
          .thenApply(
              sendResult -> {
                if (sendResult.success()) {
                  recordPiggybackedSnapshotAck(
                      message, sendResult.reliableTransport(), snapshotAckGeneration);
                }
                LOGGER.debug(
                    "Sending {} message: {}",
                    reliable ? "reliable" : "transport-selected",
                    msg.getClass().getSimpleName());
                return sendResult.success();
              });
    } catch (Exception e) {
      LOGGER.warn("Failed to send message via Session", e);
      result.complete(false);
      return result;
    }
  }

  /**
   * Send an unreliable {@link InputMessage} over UDP to the server.
   *
   * <p>Encodes the message using protobuf and sends a datagram. If the payload exceeds the safe UDP
   * MTU, it uses TCP instead.
   *
   * @param input input message to send
   */
  public void sendUnreliableInput(InputMessage input) {
    InputMessage message = inputWithSnapshotAck(input);
    try {
      byte[] data = serialize(message);
      if (data.length <= SAFE_UDP_MTU) {
        send(message, false)
            .thenAccept(
                success ->
                    LOGGER.debug("InputMessage sent using active transport size={}B", data.length));
      } else {
        LOGGER.warn(
            "InputMessage too large ({} bytes); sending via TCP instead of UDP", data.length);
        NetworkTelemetry.recordUdpOversized(message, data.length);
        NetworkTelemetry.recordUdpFallback("client input oversized");
        sendReliable(message);
      }
    } catch (IOException e) {
      LOGGER.warn("Failed to serialize InputMessage", e);
    }
  }

  /**
   * Records that a snapshot was applied locally and queues the latest tick for acknowledgement.
   *
   * <p>Explicit reliable acknowledgements are delayed briefly so outgoing input messages can carry
   * the newest applied tick first. Newer snapshot ticks supersede older pending ticks.
   *
   * @param serverTick applied server snapshot tick
   */
  public void acknowledgeSnapshot(int serverTick) {
    acknowledgeSnapshot(serverTick, false);
  }

  /**
   * Records that a snapshot was applied locally and queues or sends its acknowledgement.
   *
   * <p>Use immediate reliable acknowledgements for full snapshots because they establish or refresh
   * delta baselines. Delta snapshots should keep the delayed coalescing path.
   *
   * @param serverTick applied server snapshot tick
   * @param immediateReliable true to send an explicit reliable acknowledgement immediately
   */
  public void acknowledgeSnapshot(int serverTick, boolean immediateReliable) {
    if (serverTick < 0) {
      return;
    }
    if (immediateReliable) {
      acknowledgeSnapshotImmediately(serverTick);
      return;
    }
    queueSnapshotAck(serverTick);
  }

  private void queueSnapshotAck(int serverTick) {
    long now = java.lang.System.nanoTime();
    synchronized (snapshotAckLock) {
      int alreadyReliable = Math.max(lastExplicitSnapshotAckTick, inFlightExplicitSnapshotAckTick);
      if (serverTick <= alreadyReliable && serverTick <= pendingSnapshotAckTick) {
        return;
      }
      if (serverTick > pendingSnapshotAckTick) {
        pendingSnapshotAckTick = serverTick;
      }
      if (pendingSnapshotAckDeadlineNanos == 0L) {
        pendingSnapshotAckDeadlineNanos = now + snapshotAckExplicitDelayNanos();
      }
    }
  }

  private void acknowledgeSnapshotImmediately(int serverTick) {
    ExplicitSnapshotAck ack = immediateSnapshotAck(serverTick);
    if (ack == null) {
      return;
    }
    sendReliable(new SnapshotAck(ack.serverTick()))
        .thenAccept(
            success -> completeExplicitSnapshotAck(ack.serverTick(), ack.generation(), success));
  }

  private ExplicitSnapshotAck immediateSnapshotAck(int serverTick) {
    synchronized (snapshotAckLock) {
      if (!running.get() || !isConnected()) {
        return null;
      }
      int reliableAckTick =
          Math.max(
              Math.max(lastExplicitSnapshotAckTick, inFlightExplicitSnapshotAckTick),
              lastReliablePiggybackedSnapshotAckTick);
      int ackTick = Math.max(serverTick, pendingSnapshotAckTick);
      if (ackTick <= reliableAckTick) {
        if (pendingSnapshotAckTick <= reliableAckTick) {
          pendingSnapshotAckTick = -1;
          pendingSnapshotAckDeadlineNanos = 0L;
        }
        return null;
      }
      if (pendingSnapshotAckTick <= ackTick) {
        pendingSnapshotAckTick = -1;
        pendingSnapshotAckDeadlineNanos = 0L;
      }
      inFlightExplicitSnapshotAckTick = ackTick;
      return new ExplicitSnapshotAck(ackTick, snapshotAckGeneration);
    }
  }

  private NetworkMessage withSnapshotAck(NetworkMessage message) {
    if (message instanceof InputMessage input) {
      return inputWithSnapshotAck(input);
    }
    return message;
  }

  private InputMessage inputWithSnapshotAck(InputMessage input) {
    if (session == null) {
      return input;
    }
    int latestTick = session.clientState().map(ClientState::latestAppliedSnapshotTick).orElse(-1);
    synchronized (snapshotAckLock) {
      latestTick = Math.max(latestTick, pendingSnapshotAckTick);
    }
    if (latestTick < 0 || input.lastSnapshotTick().orElse(-1) >= latestTick) {
      return input;
    }
    return input.withLastSnapshotTick(latestTick);
  }

  private void recordPiggybackedSnapshotAck(
      NetworkMessage message, boolean reliableCarrier, long generation) {
    if (!(message instanceof InputMessage input)) {
      return;
    }
    input
        .lastSnapshotTick()
        .ifPresent(
            tick -> {
              if (tick < 0) {
                return;
              }
              synchronized (snapshotAckLock) {
                if (generation != snapshotAckGeneration) {
                  return;
                }
                if (tick >= lastPiggybackedSnapshotAckTick) {
                  lastPiggybackedSnapshotAckTick = tick;
                  lastPiggybackedSnapshotAckNanos = java.lang.System.nanoTime();
                }
                if (reliableCarrier && tick > lastReliablePiggybackedSnapshotAckTick) {
                  lastReliablePiggybackedSnapshotAckTick = tick;
                }
              }
            });
  }

  /**
   * Drain lifecycle events and inbound messages and dispatch them on the caller's thread.
   *
   * <p>Call this from the game loop to ensure all message handling runs on the game thread.
   */
  public void pollAndDispatch() {
    Runnable r;
    while ((r = lifecycleEvents.poll()) != null) {
      try {
        r.run();
      } catch (Exception e) {
        LOGGER.warn("Lifecycle runnable error", e);
      }
    }
    QueuedNetworkMessage msg;
    while ((msg = inboundQueue.poll()) != null) {
      long dispatchStartNanos = java.lang.System.nanoTime();
      try {
        dispatcher.dispatch(msg.session(), msg.message());
      } catch (Exception e) {
        LOGGER.error("Dispatch error", e);
      } finally {
        NetworkTelemetry.recordQueuedMessageDispatch(
            msg.message(),
            dispatchStartNanos - msg.receiveNanos(),
            java.lang.System.nanoTime() - dispatchStartNanos);
      }
    }
    updateTelemetryClientState();
    flushPendingSnapshotAckIfDue();
  }

  private void flushPendingSnapshotAckIfDue() {
    ExplicitSnapshotAck ack = explicitSnapshotAckDue(java.lang.System.nanoTime());
    if (ack == null) {
      return;
    }
    sendReliable(new SnapshotAck(ack.serverTick()))
        .thenAccept(
            success -> completeExplicitSnapshotAck(ack.serverTick(), ack.generation(), success));
  }

  private ExplicitSnapshotAck explicitSnapshotAckDue(long now) {
    synchronized (snapshotAckLock) {
      if (!running.get() || !isConnected() || pendingSnapshotAckTick < 0) {
        return null;
      }
      int reliableAckTick =
          Math.max(
              Math.max(lastExplicitSnapshotAckTick, inFlightExplicitSnapshotAckTick),
              lastReliablePiggybackedSnapshotAckTick);
      if (pendingSnapshotAckTick <= reliableAckTick) {
        pendingSnapshotAckTick = -1;
        pendingSnapshotAckDeadlineNanos = 0L;
        return null;
      }
      if (pendingSnapshotAckDeadlineNanos > now) {
        return null;
      }
      long explicitDelayNanos = snapshotAckExplicitDelayNanos();
      if (pendingSnapshotAckTick <= lastPiggybackedSnapshotAckTick
          && lastPiggybackedSnapshotAckNanos > 0L) {
        long nextExplicitDeadline = lastPiggybackedSnapshotAckNanos + explicitDelayNanos;
        if (now < nextExplicitDeadline) {
          pendingSnapshotAckDeadlineNanos = nextExplicitDeadline;
          return null;
        }
      }

      int ackTick = pendingSnapshotAckTick;
      pendingSnapshotAckTick = -1;
      pendingSnapshotAckDeadlineNanos = 0L;
      inFlightExplicitSnapshotAckTick = ackTick;
      return new ExplicitSnapshotAck(ackTick, snapshotAckGeneration);
    }
  }

  private void completeExplicitSnapshotAck(int serverTick, long generation, boolean success) {
    long now = java.lang.System.nanoTime();
    synchronized (snapshotAckLock) {
      if (generation != snapshotAckGeneration) {
        return;
      }
      if (inFlightExplicitSnapshotAckTick == serverTick) {
        inFlightExplicitSnapshotAckTick = -1;
      }
      if (success) {
        if (serverTick > lastExplicitSnapshotAckTick) {
          lastExplicitSnapshotAckTick = serverTick;
        }
        return;
      }
      if (!running.get() || !isConnected()) {
        return;
      }
      if (serverTick > pendingSnapshotAckTick && serverTick > lastExplicitSnapshotAckTick) {
        pendingSnapshotAckTick = serverTick;
        pendingSnapshotAckDeadlineNanos = now + snapshotAckExplicitDelayNanos();
      }
    }
  }

  private long snapshotAckExplicitDelayNanos() {
    return TimeUnit.MILLISECONDS.toNanos(SNAPSHOT_ACK_EXPLICIT_DELAY_MS);
  }

  private long snapshotAckGeneration() {
    synchronized (snapshotAckLock) {
      return snapshotAckGeneration;
    }
  }

  /**
   * Register a connection lifecycle listener. Callbacks (onConnected/onDisconnected) will be
   * invoked on the game thread.
   *
   * @param l listener to register
   */
  public void addConnectionListener(ConnectionListener l) {
    if (l != null) connectionListeners.add(l);
  }

  /**
   * Remove a previously registered connection listener.
   *
   * @param l listener to remove
   */
  public void removeConnectionListener(ConnectionListener l) {
    if (l != null) connectionListeners.remove(l);
  }

  // ---- internals ----

  private boolean startTcp() {
    Bootstrap cb = new Bootstrap();
    cb.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TCP_CONNECT_TIMEOUT_MS)
        .handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(
                    new LengthFieldBasedFrameDecoder(
                        MAX_TCP_OBJECT_SIZE + 4,
                        TCP_LENGTH_FIELD_OFFSET,
                        TCP_LENGTH_FIELD_LENGTH,
                        TCP_LENGTH_ADJUSTMENT,
                        TCP_INITIAL_BYTES_TO_STRIP));
                p.addLast(
                    new SimpleChannelInboundHandler<ByteBuf>() {
                      @Override
                      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame)
                          throws Exception {
                        int size = frame.readableBytes();
                        long receiveNanos = java.lang.System.nanoTime();
                        NetworkMessage msg = deserialize(frame);
                        NetworkTelemetry.recordInboundTcp(
                            msg, size, java.lang.System.nanoTime() - receiveNanos);
                        if (msg
                            instanceof ConnectAck(short id, int sessionId, byte[] sessionToken)) {
                          onConnectAck(id, sessionId, sessionToken);
                        } else if (msg instanceof ConnectReject(byte reason)) {
                          onConnectReject(session, ConnectReject.Reason.fromCode(reason));
                        } else if (msg instanceof RegisterAck(boolean ok)) {
                          onRegisterAck(ok);
                        } else {
                          onNetworkMessage(msg, size, receiveNanos);
                        }
                      }

                      @Override
                      public void channelActive(ChannelHandlerContext ctx) {
                        updateUdpRemote(ctx.channel());
                        // Create a client-side Session bound to this TCP ctx and using client
                        // senders
                        session =
                            new Session(
                                ctx,
                                ClientNetwork.this::sendUdpObject,
                                ClientNetwork.this::sendTcpObject);
                        // Point the session UDP to the server address as our logical peer
                        if (udpRemote != null) session.udpAddress(udpRemote);

                        try {
                          ConnectRequest request;
                          // Attempt to load last session from file
                          Tuple<Integer, byte[]> lastSession = loadLastSessionFromFile();
                          if (lastSession != null) {
                            int sessionId = lastSession.a();
                            byte[] sessionToken = lastSession.b();
                            LOGGER.info(
                                "Loaded last session from file: sessionId={}; Trying to reconnect.",
                                sessionId);
                            request = connectRequest(sessionId, sessionToken);
                          } else {
                            LOGGER.info("No valid last session file found; starting new session.");
                            request = connectRequest();
                          }
                          byte[] data = serialize(request);
                          if (data.length <= MAX_TCP_OBJECT_SIZE) {
                            ByteBuf buf = ctx.alloc().buffer(4 + data.length);
                            buf.writeInt(data.length);
                            buf.writeBytes(data);
                            ctx.writeAndFlush(buf)
                                .addListener(
                                    future -> {
                                      if (future.isSuccess()) {
                                        NetworkTelemetry.recordOutboundTcp(request, data.length);
                                        return;
                                      }
                                      LOGGER.warn("Failed to write ConnectRequest", future.cause());
                                      ctx.close();
                                    });
                          } else {
                            LOGGER.error(
                                "ConnectRequest too large ({} bytes); cannot connect", data.length);
                            ctx.close();
                          }
                        } catch (IOException e) {
                          LOGGER.warn("Failed to send ConnectRequest", e);
                          ctx.close();
                        }
                      }

                      @Override
                      public void channelInactive(ChannelHandlerContext ctx) {
                        LOGGER.info("TCP connection closed by server");
                        handleTransportClosed(null);
                        // invalidateLastSessionFile(); // TODO: decide if we want this
                      }

                      @Override
                      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        LOGGER.warn("TCP client error", cause);
                        handleTransportClosed(cause.getMessage());
                        ctx.close();
                      }
                    });
              }
            });
    try {
      ChannelFuture f = cb.connect(remoteHost, port).syncUninterruptibly();
      tcp = f.channel();
      updateUdpRemote(tcp);
      return true;
    } catch (Exception e) {
      if (hasCause(e, ConnectException.class)) {
        LOGGER.error(
            "Failed to connect TCP to server at {}:{} - {}", remoteHost, port, e.getMessage());
        handleImmediateStartFailure("Connection refused");
      } else {
        LOGGER.error("Failed to start TCP client for {}:{} - {}", remoteHost, port, e.getMessage());
        handleImmediateStartFailure("Network start failed: " + e.getMessage());
      }
      return false;
    }
  }

  private void updateUdpRemote(Channel channel) {
    if (channel.remoteAddress() instanceof InetSocketAddress remoteAddress
        && remoteAddress.getAddress() != null) {
      udpRemote = new InetSocketAddress(remoteAddress.getAddress(), port);
    }
  }

  private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
    Throwable current = throwable;
    while (current != null) {
      if (causeType.isInstance(current)) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private void handleImmediateStartFailure(String reason) {
    enqueueLifecycle(() -> notifyDisconnected(reason));
    closeTransportResources();
    connected.set(false);
    running.set(false);
    session = null;
    clientId = null;
    udpRecoveryState.enterRetryMode();
    resetSnapshotAckState();
  }

  private void resetSnapshotAckState() {
    synchronized (snapshotAckLock) {
      pendingSnapshotAckTick = -1;
      pendingSnapshotAckDeadlineNanos = 0L;
      inFlightExplicitSnapshotAckTick = -1;
      lastExplicitSnapshotAckTick = -1;
      lastPiggybackedSnapshotAckTick = -1;
      lastPiggybackedSnapshotAckNanos = 0L;
      lastReliablePiggybackedSnapshotAckTick = -1;
      snapshotAckGeneration++;
    }
  }

  private void closeTransportResources() {
    try {
      cancelUdpMaintenance();
      if (tcp != null) {
        tcp.close().syncUninterruptibly();
        tcp = null;
      }
      if (udp != null) {
        udp.close().syncUninterruptibly();
        udp = null;
      }
    } catch (Exception e) {
      LOGGER.warn("Error closing channels", e);
    }
    try {
      if (group != null) {
        group.shutdownGracefully();
        group = null;
      }
    } catch (Exception e) {
      LOGGER.warn("Error shutting down event loop", e);
    }
  }

  private void handleTransportClosed(String reason) {
    boolean wasActive = running.getAndSet(false) || connected.get();
    connected.set(false);
    cancelUdpMaintenance();
    clientId = null;
    session = null;
    tcp = null;
    resetSnapshotAckState();
    if (udp != null) {
      udp.close();
      udp = null;
    }
    if (group != null) {
      group.shutdownGracefully();
      group = null;
    }
    udpRecoveryState.enterRetryMode();
    if (wasActive) {
      enqueueLifecycle(() -> notifyDisconnected(reason));
    }
  }

  private void onNetworkMessage(NetworkMessage msg, int size, long receiveNanos) {
    if (session != null) {
      inboundQueue.offer(new QueuedNetworkMessage(session, msg, receiveNanos));
    } else {
      LOGGER.debug(
          "Dropping TCP inbound before session init: {} size={}B",
          msg.getClass().getSimpleName(),
          size);
    }
    LOGGER.debug("TCP inbound {} size={}B", msg.getClass().getSimpleName(), size);
  }

  private void startUdpIfNeeded() {
    if (udp != null && udp.isActive()) {
      return;
    }

    Bootstrap ub = new Bootstrap();
    ub.group(group)
        .channel(NioDatagramChannel.class)
        .handler(
            new SimpleChannelInboundHandler<DatagramPacket>() {
              @Override
              protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket pkt) {
                int size = pkt.content().readableBytes();
                long receiveNanos = java.lang.System.nanoTime();
                try {
                  if (size <= 0) {
                    NetworkTelemetry.recordUdpDrop("client invalid UDP size");
                    return;
                  }
                  NetworkMessage msg = deserialize(pkt.content());
                  NetworkTelemetry.recordInboundUdp(msg, size);
                  if (session != null) {
                    inboundQueue.offer(new QueuedNetworkMessage(session, msg, receiveNanos));
                  } else {
                    NetworkTelemetry.recordUdpDrop("client session missing");
                    LOGGER.debug(
                        "Dropping UDP inbound before session init: {}",
                        msg.getClass().getSimpleName());
                  }
                } catch (Exception e) {
                  NetworkTelemetry.recordUdpDrop("client UDP decode error");
                  LOGGER.warn("UDP client decode error", e);
                }
              }

              @Override
              public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                LOGGER.warn("UDP client error", cause);
                onUdpUnavailable("UDP unavailable, using TCP fallback");
              }
            });
    try {
      udp = ub.bind(0).syncUninterruptibly().channel();
      udp.closeFuture()
          .addListener(
              future -> {
                udp = null;
                onUdpUnavailable("UDP unavailable, using TCP fallback");
              });
    } catch (Exception e) {
      LOGGER.warn("Failed to bind UDP channel for {}:{} - {}", remoteHost, port, e.getMessage());
      udp = null;
      return;
    }
    if (session != null) {
      session.udpAddress(udpRemote);
    }
    LOGGER.info("Client opened UDP channel for {}:{}", remoteHost, port);
  }

  private void onConnectAck(short newClientId, int sessionId, byte[] sessionToken) {
    this.clientId = newClientId;
    connected.set(true);
    LOGGER.info("Received ConnectAck clientId={}, sessionId={}", newClientId, sessionId);
    session.attachClientState(
        new ClientState(
            newClientId,
            username,
            sessionId,
            sessionToken,
            requestedCharacterClass.orElse(CharacterClass.WIZARD)));
    session.udpReady(false);
    LOGGER.info("UDP unavailable, using TCP fallback");
    onUdpUnavailable("UDP unavailable, using TCP fallback");
    ensureUdpMaintenanceScheduled(udpRecoveryState.nextDelayMs());
    saveLastSessionToFile(sessionId, sessionToken);
    enqueueLifecycle(this::notifyConnected);
  }

  private void onConnectReject(Session session, ConnectReject.Reason reason) {
    String reasonStr = "Connection rejected by server: " + reason;
    LOGGER.warn(reasonStr);
    if (reason == ConnectReject.Reason.NO_SESSION_FOUND
        || reason == ConnectReject.Reason.INVALID_SESSION_TOKEN) {
      // Invalidate last session file upon session-related rejections; Try to connect again without
      // session
      invalidateLastSessionFile();
      session.sendMessage(connectRequest(), true);
    } else {
      // Close the connection upon rejection
      enqueueLifecycle(() -> notifyRejected(reason));
      enqueueLifecycle(() -> notifyDisconnected(reasonStr));
      session.tcpCtx().close();
    }
  }

  /**
   * Save session token and session id to a file for future reconnects.
   *
   * <p>This method writes the session id (4 bytes) followed by the session token bytes to a file
   * `last_session.dat`.
   *
   * @param sessionId the session id to save
   * @param sessionToken the session token bytes to save
   */
  private void saveLastSessionToFile(int sessionId, byte[] sessionToken) {
    try {
      byte[] data =
          ByteBuffer.allocate(4 + sessionToken.length).putInt(sessionId).put(sessionToken).array();
      Files.write(Path.of(LAST_SESSION_FILE_NAME), data);
    } catch (IOException e) {
      LOGGER.warn("Failed to save last session to file", e);
    }
  }

  /**
   * Load session token and session id from a file for reconnects.
   *
   * <p>This method reads the session id (4 bytes) followed by the session token bytes from a file
   * `last_session.dat`.
   *
   * @return a Tuple containing the session id and session token bytes, or null if loading failed
   */
  private Tuple<Integer, byte[]> loadLastSessionFromFile() {
    try {
      Path filePath = Path.of(LAST_SESSION_FILE_NAME);
      boolean exists = Files.exists(filePath);
      if (!exists) {
        return null;
      }
      byte[] data = Files.readAllBytes(filePath);
      ByteBuffer buffer = ByteBuffer.wrap(data);
      int sessionId = buffer.getInt();
      byte[] sessionToken = new byte[buffer.remaining()];
      buffer.get(sessionToken);
      return Tuple.of(sessionId, sessionToken);
    } catch (IOException e) {
      LOGGER.warn("Failed to load last session from file", e);
      return null;
    }
  }

  /**
   * Invalidate the last session file `last_session.dat` by deleting it.
   *
   * <p>This method is called when you want to discard the saved last session, for example, after a
   * session-related rejection from the server. Or after completing a session and wanting to start
   * fresh next time.
   */
  public static void invalidateLastSessionFile() {
    try {
      Files.deleteIfExists(Path.of(LAST_SESSION_FILE_NAME));
    } catch (IOException e) {
      LOGGER.warn("Failed to invalidate last session file", e);
    }
  }

  void onRegisterAck(boolean ok) {
    // TCP ACK confirms UDP registration
    if (ok) {
      Short id = clientId;
      if (id != null && id > 0) {
        boolean recovered = udpRecoveryState.markRecovered(System.currentTimeMillis());
        if (session != null) {
          session.udpReady(true);
          session.markUdpActivity();
        }
        updateTelemetryClientState();
        if (recovered) {
          LOGGER.info("UDP recovered, resuming UDP");
        }
        ensureUdpMaintenanceScheduled(udpRecoveryState.nextDelayMs());
      } else {
        throw new IllegalStateException("Received RegisterAck before ConnectAck");
      }
    } else {
      LOGGER.warn("UDP registration rejected by server for clientId={}", clientId);
      udpRecoveryState.markRetryAckFailure();
      if (session != null) {
        session.udpReady(false);
      }
      updateTelemetryClientState();
      ensureUdpMaintenanceScheduled(udpRecoveryState.nextDelayMs());
    }
  }

  private void ensureUdpMaintenanceScheduled(long delayMs) {
    if (!running.get() || !connected.get() || group == null) {
      return;
    }
    ScheduledFuture<?> future = udpMaintenanceFuture;
    if (future != null && !future.isDone()) {
      future.cancel(false);
    }
    udpMaintenanceFuture =
        group.next().schedule(this::runUdpMaintenance, delayMs, TimeUnit.MILLISECONDS);
  }

  private void cancelUdpMaintenance() {
    ScheduledFuture<?> future = udpMaintenanceFuture;
    if (future != null) {
      future.cancel(false);
      udpMaintenanceFuture = null;
    }
  }

  private void runUdpMaintenance() {
    udpMaintenanceFuture = null;
    if (!running.get()
        || !connected.get()
        || session == null
        || clientId == null
        || clientId <= 0) {
      return;
    }

    long now = System.currentTimeMillis();
    if (udpRecoveryState.stale(now)) {
      onUdpUnavailable("UDP stale, reverting to TCP fallback");
    }

    startUdpIfNeeded();
    boolean sent = sendRegisterUdp();
    if (udpRecoveryState.retryMode()) {
      udpRecoveryState.afterMaintenanceAttempt();
    }
    long nextDelay = udpRecoveryState.nextDelayMs();
    LOGGER.debug(
        "UDP maintenance cycle mode={} sent={} nextDelayMs={}",
        udpRecoveryState.retryMode() ? "retry" : "keepalive",
        sent,
        nextDelay);
    ensureUdpMaintenanceScheduled(nextDelay);
  }

  private boolean sendRegisterUdp() {
    if (session == null || clientId == null || clientId <= 0) {
      return false;
    }
    if (udp == null || !udp.isActive()) {
      LOGGER.debug("UDP channel not active; cannot send RegisterUdp");
      return false;
    }

    RegisterUdp registerUdp =
        new RegisterUdp(session.sessionId(), session.sessionToken(), clientId);
    final byte[] payload;
    try {
      payload = serialize(registerUdp);
    } catch (IOException e) {
      LOGGER.warn("Failed to serialize RegisterUdp for clientId={}", clientId, e);
      return false;
    }

    if (payload.length > SAFE_UDP_MTU) {
      LOGGER.warn(
          "RegisterUdp too large ({} bytes); skipping clientId={}", payload.length, clientId);
      NetworkTelemetry.recordUdpOversized(registerUdp, payload.length);
      return false;
    }

    try {
      udp.writeAndFlush(
              new DatagramPacket(udp.alloc().buffer(payload.length).writeBytes(payload), udpRemote))
          .addListener(
              future -> {
                if (future.isSuccess()) {
                  NetworkTelemetry.recordOutboundUdp(registerUdp, payload.length);
                  return;
                }
                NetworkTelemetry.recordUdpSendFailure(
                    registerUdp, "client RegisterUdp write failed");
                LOGGER.warn(
                    "Failed to write RegisterUdp for clientId={}", clientId, future.cause());
              });
      LOGGER.debug(
          "Sent RegisterUdp for clientId={} to {} using {} mode",
          clientId,
          udpRemote,
          udpRecoveryState.retryMode() ? "retry" : "keepalive");
      return true;
    } catch (Throwable t) {
      NetworkTelemetry.recordUdpSendFailure(registerUdp, "client RegisterUdp send failed");
      LOGGER.warn("Error while sending RegisterUdp for clientId={}", clientId, t);
      return false;
    }
  }

  private void onUdpUnavailable(String message) {
    boolean changed = udpRecoveryState.enterRetryMode();
    if (session != null) {
      session.udpReady(false);
    }
    updateTelemetryClientState();
    if (changed) {
      LOGGER.info(message);
    }
    ensureUdpMaintenanceScheduled(udpRecoveryState.nextDelayMs());
  }

  private void enqueueLifecycle(Runnable r) {
    if (r != null) lifecycleEvents.offer(r);
  }

  private ConnectRequest connectRequest() {
    return connectRequest(0, new byte[0]);
  }

  private ConnectRequest connectRequest(int sessionId, byte[] sessionToken) {
    return new ConnectRequest(
        PROTOCOL_VERSION, username, sessionId, sessionToken, requestedCharacterClass);
  }

  private void notifyConnected() {
    for (ConnectionListener l : connectionListeners) {
      try {
        l.onConnected();
      } catch (Exception e) {
        LOGGER.warn("onConnected error", e);
      }
    }
  }

  private void notifyDisconnected(String cause) {
    for (ConnectionListener l : connectionListeners) {
      try {
        l.onDisconnected(cause);
      } catch (Exception e) {
        LOGGER.warn("onDisconnected error", e);
      }
    }
  }

  private void notifyRejected(ConnectReject.Reason reason) {
    for (ConnectionListener l : connectionListeners) {
      try {
        l.onRejected(reason);
      } catch (Exception e) {
        LOGGER.warn("onRejected error", e);
      }
    }
  }

  private void updateTelemetryClientState() {
    Session currentSession = session;
    long lastAck = udpRecoveryState.lastRegisterAckTimeMs();
    long ackAge = lastAck > 0L ? System.currentTimeMillis() - lastAck : -1L;
    int latestSnapshotTick =
        currentSession == null
            ? -1
            : currentSession.clientState().map(ClientState::latestAppliedSnapshotTick).orElse(-1);
    NetworkTelemetry.recordClientState(
        isConnected(),
        clientId(),
        currentSession != null && currentSession.udpReady(),
        udpRecoveryState.retryMode(),
        ackAge,
        latestSnapshotTick);
  }

  // ---- client-side Session senders ----

  /**
   * Client-side UDP sender for {@link Session}. Mirrors server-side send path and size checks.
   *
   * @param target target address
   * @param msg message to send
   * @return CompletableFuture indicating success sending the message
   */
  private CompletableFuture<Boolean> sendUdpObject(InetSocketAddress target, NetworkMessage msg) {
    if (udp == null || !udp.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      NetworkTelemetry.recordUdpSendFailure(msg, "client UDP channel inactive");
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(msg);
      if (data.length > SAFE_UDP_MTU) {
        LOGGER.warn("Skip UDP send; payload too large ({} B) to {}", data.length, target);
        NetworkTelemetry.recordUdpOversized(msg, data.length);
        return CompletableFuture.completedFuture(false);
      }
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      ChannelFuture writeFuture =
          udp.writeAndFlush(
              new DatagramPacket(udp.alloc().buffer(data.length).writeBytes(data), target));
      writeFuture.addListener(
          future -> {
            if (future.isSuccess()) {
              NetworkTelemetry.recordOutboundUdp(msg, data.length);
              result.complete(true);
              return;
            }
            NetworkTelemetry.recordUdpSendFailure(msg, "client UDP write failed");
            LOGGER.warn("Failed to write UDP object to {}", target, future.cause());
            result.complete(false);
          });
      return result;
    } catch (Exception e) {
      NetworkTelemetry.recordUdpSendFailure(msg, "client UDP send failed");
      LOGGER.warn("Failed to send UDP object to {}", target, e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Client-side TCP sender for {@link Session}. Mirrors server-side send path and size checks.
   *
   * @param ctx channel context
   * @param msg message to send
   * @return CompletableFuture indicating the acknowledgment of the sent message by the recipient
   */
  private CompletableFuture<Boolean> sendTcpObject(ChannelHandlerContext ctx, NetworkMessage msg) {
    if (ctx == null || ctx.channel() == null || !ctx.channel().isActive()) {
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(msg);
      if (data.length > MAX_TCP_OBJECT_SIZE) {
        LOGGER.warn("Skip TCP send; payload too large ({} B) to {}", data.length, ctx.channel());
        return CompletableFuture.completedFuture(false);
      }
      ByteBuf buf = ctx.alloc().buffer(TCP_LENGTH_FIELD_LENGTH + data.length);
      buf.writeInt(data.length);
      buf.writeBytes(data);
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      ctx.writeAndFlush(buf)
          .addListener(
              future -> {
                if (future.isSuccess()) {
                  NetworkTelemetry.recordOutboundTcp(msg, data.length);
                  result.complete(true);
                  return;
                }
                LOGGER.warn("Failed to write TCP object to {}", ctx.channel(), future.cause());
                result.complete(false);
              });
      return result;
    } catch (IOException e) {
      LOGGER.warn("Failed to send TCP object to {}: {}", ctx.channel(), e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }
}
