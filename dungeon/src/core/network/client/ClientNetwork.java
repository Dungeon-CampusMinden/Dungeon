package core.network.client;

import static core.network.codec.NetworkCodec.deserialize;
import static core.network.codec.NetworkCodec.serialize;
import static core.network.config.NetworkConfig.*;

import core.Game;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.RegisterAck;
import core.network.server.ClientState;
import core.network.server.Session;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Netty-backed client transport using a single {@link Session} to mirror server-side semantics.
 *
 * <p>Responsibilities: - Establish a TCP connection to the server for reliable messages and
 * handshake. - Bind an ephemeral UDP socket for sending/receiving unreliable messages (inputs,
 * snapshots). - Maintain an inbound queue of deserialized {@link NetworkMessage}s and lifecycle
 * events to be consumed on the game thread via {@link #pollAndDispatch()}. - Manage UDP
 * "registration" (RegisterUdp) retransmits until the server observes the client's UDP source
 * address; retries are cancellable when a UDP response is seen. - Expose a {@link Session} instance
 * backed by client-side senders so message handlers can reply via {@code session.sendMessage(...)}
 * just like on the server.
 *
 * <p>Threading model: Netty IO threads enqueue messages and lifecycle events; the game thread calls
 * {@link #pollAndDispatch()} and lifecycle listeners are invoked on the game thread via enqueued
 * runnables.
 */
public final class ClientNetwork {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientNetwork.class);

  private static final short CLIENT_PROTOCOL_VERSION = 1;
  private final MessageDispatcher dispatcher = new MessageDispatcher();
  private volatile BiConsumer<Session, NetworkMessage> rawConsumer;
  private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
  private final Queue<Tuple<Session, NetworkMessage>> inboundQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Runnable> lifecycleEvents = new ConcurrentLinkedQueue<>();

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);

  private volatile Session session = new Session(null);

  private String remoteHost;
  private int port;
  private String username;

  private EventLoopGroup group;
  private Channel tcp;
  private Channel udp;
  private InetSocketAddress udpRemote;

  // assigned after ConnectAck
  private volatile Short clientId;

  // Track scheduled UDP registration retries per clientId to allow cancellation/cleanup
  private final ConcurrentHashMap<Short, UdpRegistrationTask> udpRegisterTasks =
      new ConcurrentHashMap<>();

  /**
   * Initialize the client network with connection parameters.
   *
   * @param host server hostname or IP to connect to
   * @param port server port
   * @param username username used in the TCP ConnectRequest handshake
   *     <p>This method allocates the event loop group and prepares the UDP remote address. It does
   *     not open sockets call {@link #start()} to connect.
   */
  public void initialize(String host, int port, String username) {
    this.remoteHost = host;
    this.port = port;
    this.username = username;
    this.group = new NioEventLoopGroup();
    this.udpRemote = new InetSocketAddress(host, port);
  }

  /**
   * Start network IO: connect TCP and bind UDP.
   *
   * <p>This method is safe to call once. It returns quickly Netty performs IO on its own threads.
   * After start completes, the client will attempt the handshake over TCP; on TCP connect the
   * ConnectRequest is sent automatically.
   */
  public void start() {
    if (!running.compareAndSet(false, true)) return;
    startTcp();
    startUdp();
  }

  /**
   * Gracefully shuts down network activity and cancels any outstanding UDP registration retries.
   *
   * @param reason human-readable reason used for lifecycle callbacks and logging
   */
  public void shutdown(String reason) {
    if (!running.compareAndSet(true, false)) return;
    try {
      cancelAllUdpRegistrations();
      if (tcp != null) tcp.close().syncUninterruptibly();
      if (udp != null) udp.close().syncUninterruptibly();
    } catch (Exception e) {
      LOGGER.warn("Error closing channels", e);
    }
    try {
      if (group != null) group.shutdownGracefully();
    } catch (Exception e) {
      LOGGER.warn("Error shutting down event loop", e);
    }
    connected.set(false);
    LOGGER.info("ClientNetwork shutdown. Reason: {}", reason);
  }

  /**
   * Returns whether the transport has an active TCP connection.
   *
   * <p>Note: UDP may be bound even if this returns false during transitional states; use lifecycle
   * callbacks to observe connection changes.
   */
  public boolean isConnected() {
    return connected.get();
  }

  /**
   * Returns the message dispatcher used for registering handlers for inbound messages.
   *
   * <p>Handlers registered here will be invoked from {@link #pollAndDispatch()} on the game thread
   * (or immediately by the raw consumer if set).
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

  /**
   * Set an optional raw message consumer used by higher-level code to directly receive inbound
   * {@link NetworkMessage} objects.
   *
   * <p>If a raw consumer is set, inbound messages are forwarded to it instead of the dispatcher.
   *
   * @param consumer BiConsumer receiving the {@link Session} and the deserialized {@link
   *     NetworkMessage}.
   */
  public void setRawMessageConsumer(BiConsumer<Session, NetworkMessage> consumer) {
    this.rawConsumer = consumer;
  }

  /**
   * Returns the client id assigned by the server, or 0 if not yet assigned.
   *
   * <p>The id becomes available after receiving a {@link ConnectAck}.
   */
  public Short clientId() {
    Short id = clientId;
    return id != null ? id : 0;
  }

  /**
   * Send a reliable {@link NetworkMessage} over TCP to the server.
   *
   * <p>Performs Java serialization and writes a 4-byte length-prefixed frame. Drops the message if
   * it exceeds the configured TCP object size.
   *
   * @param msg message to send
   * @return CompletableFuture that completes with true if the message was sent, false if
   *     serialization failed or the message was dropped
   */
  public CompletableFuture<Boolean> sendReliable(NetworkMessage msg) {
    final CompletableFuture<Boolean> result = new CompletableFuture<>();
    if (!running.get() || !isConnected() || tcp == null || !tcp.isActive()) {
      LOGGER.warn("TCP not active; cannot send reliable message");
      result.complete(false);
      return result;
    }

    try {
      return session
          .sendMessage(msg, true)
          .thenApply(
              success -> {
                LOGGER.debug("Sending reliable message: {}", msg.getClass().getSimpleName());
                return success;
              });
    } catch (Exception e) {
      LOGGER.warn("Failed to send reliable message via Session", e);
      result.complete(false);
      return result;
    }
  }

  /**
   * Send an unreliable {@link InputMessage} over UDP to the server.
   *
   * <p>Performs Java serialization and sends a datagram. If the payload exceeds the safe UDP MTU,
   * it uses TCP instead.
   *
   * @param input input message to send
   */
  public void sendUnreliableInput(InputMessage input) {
    if (!running.get() || udp == null || !udp.isActive()) {
      LOGGER.warn("UDP not active; cannot send input message");
      return;
    }
    try {
      byte[] data = serialize(input);
      if (data.length <= SAFE_UDP_MTU) {
        session
            .sendMessage(input, false)
            .thenAccept(success -> LOGGER.debug("UDP outbound InputMessage size={}B", data.length));
      } else {
        LOGGER.warn(
            "InputMessage too large ({} bytes); sending via TCP instead of UDP", data.length);
        sendReliable(input);
      }
    } catch (IOException e) {
      LOGGER.warn("Failed to serialize InputMessage", e);
    }
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
    Tuple<Session, NetworkMessage> msg;
    while ((msg = inboundQueue.poll()) != null) {
      try {
        BiConsumer<Session, NetworkMessage> consumer = rawConsumer;
        if (consumer != null) consumer.accept(msg.a(), msg.b());
        else dispatcher.dispatch(msg.a(), msg.b());
      } catch (Exception e) {
        LOGGER.error("Dispatch error", e);
      }
    }
  }

  /**
   * Register a connection lifecycle listener. Callbacks (onConnected/onDisconnected) will be
   * invoked on the game thread via {@link #pollAndDispatch()}.
   */
  public void addConnectionListener(ConnectionListener l) {
    if (l != null) connectionListeners.add(l);
  }

  /** Remove a previously registered connection listener. */
  public void removeConnectionListener(ConnectionListener l) {
    if (l != null) connectionListeners.remove(l);
  }

  // ---- internals ----

  private void startTcp() {
    Bootstrap cb = new Bootstrap();
    cb.group(group)
        .channel(NioSocketChannel.class)
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
                        Object obj = deserialize(frame);
                        if (obj
                            instanceof ConnectAck(short id, int sessionId, byte[] sessionToken)) {
                          onConnectAck(id, sessionId, sessionToken);
                        } else if (obj instanceof RegisterAck(boolean ok)) {
                          onRegisterAck(ok);
                        } else if (obj instanceof NetworkMessage nm) {
                          if (session != null) {
                            inboundQueue.offer(Tuple.of(session, nm));
                          } else {
                            LOGGER.debug(
                                "Dropping TCP inbound before session init: {} size={}B",
                                obj.getClass().getSimpleName(),
                                size);
                          }
                          LOGGER.debug(
                              "TCP inbound {} size={}B", obj.getClass().getSimpleName(), size);
                        }
                      }

                      @Override
                      public void channelActive(ChannelHandlerContext ctx) {
                        connected.set(true);
                        // Create a client-side Session bound to this TCP ctx and using client
                        // senders
                        session =
                            new Session(
                                ctx,
                                ClientNetwork.this::sendUdpObject,
                                ClientNetwork.this::sendTcpObject);
                        // Point the session UDP to the server address as our logical peer
                        if (udpRemote != null) session.udpAddress(udpRemote);

                        enqueueLifecycle(ClientNetwork.this::notifyConnected);
                        try {
                          byte[] data =
                              serialize(new ConnectRequest(CLIENT_PROTOCOL_VERSION, username));
                          if (data.length <= MAX_TCP_OBJECT_SIZE) {
                            ByteBuf buf = ctx.alloc().buffer(4 + data.length);
                            buf.writeInt(data.length);
                            buf.writeBytes(data);
                            ctx.writeAndFlush(buf);
                          }
                        } catch (Exception e) {
                          LOGGER.warn("Failed to send ConnectRequest", e);
                        }
                      }

                      @Override
                      public void channelInactive(ChannelHandlerContext ctx) {
                        connected.set(false);
                        enqueueLifecycle(() -> notifyDisconnected(null));
                      }

                      @Override
                      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        LOGGER.warn("TCP client error", cause);
                        enqueueLifecycle(() -> notifyDisconnected(cause.getMessage()));
                        Game.exit("TCP error: " + cause.getMessage());
                      }
                    });
              }
            });
    ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, port)).syncUninterruptibly();
    tcp = f.channel();
  }

  private void startUdp() {
    Bootstrap ub = new Bootstrap();
    ub.group(group)
        .channel(NioDatagramChannel.class)
        .handler(
            new SimpleChannelInboundHandler<DatagramPacket>() {
              @Override
              protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket pkt) {
                try {
                  Object obj = deserialize(pkt.content());
                  if (obj instanceof NetworkMessage nm) {
                    if (session != null) {
                      inboundQueue.offer(Tuple.of(session, nm));
                    } else {
                      LOGGER.debug(
                          "Dropping UDP inbound before session init: {}",
                          nm.getClass().getSimpleName());
                    }
                  }
                } catch (Exception e) {
                  LOGGER.warn("UDP client decode error", e);
                }
              }

              @Override
              public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                LOGGER.warn("UDP client error", cause);
              }
            });
    udp = ub.bind(0).syncUninterruptibly().channel();
    // Cancel all pending registrations when the channel closes
    udp.closeFuture().addListener(future -> cancelAllUdpRegistrations());
    udp.connect(udpRemote).syncUninterruptibly();
    // If TCP session already exists, update its udpAddress to our peer
    if (session != null) session.udpAddress(udpRemote);
    LOGGER.info("Client connected to {}:{} (TCP+UDP)", remoteHost, port);
  }

  private void onConnectAck(short newClientId, int sessionId, byte[] sessionToken) {
    this.clientId = newClientId;
    LOGGER.info("Received ConnectAck clientId={}, sessionId={}", newClientId, sessionId);
    session.attachClientState(new ClientState(newClientId, username, sessionId, sessionToken));
    scheduleUdpRegistration(newClientId);
  }

  private void onRegisterAck(boolean ok) {
    // TCP ACK confirms UDP registration
    if (ok) {
      LOGGER.info("UDP registration acknowledged by server for clientId={}", clientId);
      Short id = clientId;
      if (id != null && id > 0) {
        cancelUdpRegistration(id);
      } else {
        throw new IllegalStateException("Received RegisterAck before ConnectAck");
      }
    } else {
      LOGGER.warn("UDP registration rejected by server for clientId={}", clientId);
    }
  }

  private void scheduleUdpRegistration(short clientId) {
    if (udp == null || !udp.isActive()) {
      return;
    }

    // Avoid duplicate scheduling
    if (udpRegisterTasks.containsKey(clientId)) {
      LOGGER.debug("UDP registration already scheduled for clientId={}", clientId);
      return;
    }

    final byte[] payload;
    try {
      payload = serialize(new RegisterUdp(session.sessionId(), session().sessionToken(), clientId));
    } catch (IOException e) {
      LOGGER.warn("Failed to serialize RegisterUdp for clientId={}", clientId, e);
      return;
    }

    if (payload.length > SAFE_UDP_MTU) {
      LOGGER.warn(
          "RegisterUdp too large ({} bytes); skipping clientId={}", payload.length, clientId);
      return;
    }

    UdpRegistrationTask task = new UdpRegistrationTask(clientId, payload);
    if (udpRegisterTasks.putIfAbsent(clientId, task) == null) {
      task.start();
    }
  }

  private void cancelUdpRegistration(short clientId) {
    UdpRegistrationTask task = udpRegisterTasks.remove(clientId);
    if (task != null) {
      task.cancel();
    }
  }

  private void cancelAllUdpRegistrations() {
    // Iterate over a copy of the values to avoid ConcurrentModificationException
    for (UdpRegistrationTask task : List.copyOf(udpRegisterTasks.values())) {
      task.cancel();
    }
    udpRegisterTasks.clear();
  }

  private void enqueueLifecycle(Runnable r) {
    if (r != null) lifecycleEvents.offer(r);
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

  // ---- client-side Session senders ----

  /** Client-side UDP sender for {@link Session}. Mirrors server-side send path and size checks. */
  private CompletableFuture<Boolean> sendUdpObject(InetSocketAddress target, Object obj) {
    if (udp == null || !udp.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(obj);
      if (data.length > SAFE_UDP_MTU) {
        LOGGER.warn("Skip UDP send; payload too large ({} B) to {}", data.length, target);
        return CompletableFuture.completedFuture(false);
      }
      udp.writeAndFlush(
              new DatagramPacket(udp.alloc().buffer(data.length).writeBytes(data), target))
          .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      return CompletableFuture.completedFuture(true);
    } catch (Exception e) {
      LOGGER.warn("Failed to send UDP object to {}", target, e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /** Client-side TCP sender for {@link Session}. Mirrors server-side send path and size checks. */
  private CompletableFuture<Boolean> sendTcpObject(ChannelHandlerContext ctx, Object obj) {
    if (ctx == null || ctx.channel() == null || !ctx.channel().isActive()) {
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(obj);
      if (data.length > MAX_TCP_OBJECT_SIZE) {
        LOGGER.warn("Skip TCP send; payload too large ({} B) to {}", data.length, ctx.channel());
        return CompletableFuture.completedFuture(false);
      }
      ByteBuf buf = ctx.alloc().buffer(TCP_LENGTH_FIELD_LENGTH + data.length);
      buf.writeInt(data.length);
      buf.writeBytes(data);
      ctx.writeAndFlush(buf).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      return CompletableFuture.completedFuture(true);
    } catch (IOException e) {
      LOGGER.warn("Failed to send TCP object to {}: {}", ctx.channel(), e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }

  private final class UdpRegistrationTask implements Runnable {
    private final short clientId;
    private final byte[] payload;
    private final AtomicInteger attempts = new AtomicInteger(0);
    private volatile ScheduledFuture<?> future;

    UdpRegistrationTask(short clientId, byte[] payload) {
      this.clientId = clientId;
      this.payload = payload;
    }

    void start() {
      if (udp == null || !udp.isActive()) {
        LOGGER.warn("UDP channel not active, cannot start registration for clientId={}", clientId);
        return;
      }
      // Schedule the first run immediately on the event loop
      this.future = udp.eventLoop().schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    void cancel() {
      if (future != null) {
        future.cancel(false);
      }
      // Remove from the main map
      udpRegisterTasks.remove(this.clientId);
      LOGGER.debug("Cancelled UDP registration retries for clientId={}", clientId);
    }

    @Override
    public void run() {
      if (udp == null || !udp.isActive()) {
        LOGGER.debug(
            "UDP channel inactive; stopping registration retries for clientId={}", clientId);
        udpRegisterTasks.remove(this.clientId);
        return;
      }

      int attempt = attempts.incrementAndGet();
      if (attempt > UDP_REGISTER_ATTEMPTS) {
        LOGGER.warn(
            "UDP registration failed for clientId={} after {} attempts", clientId, attempt - 1);
        udpRegisterTasks.remove(this.clientId);
        return;
      }

      // Send the datagram
      try {
        udp.writeAndFlush(
                new DatagramPacket(
                    udp.alloc().buffer(payload.length).writeBytes(payload), udpRemote))
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        LOGGER.debug("Sent RegisterUdp attempt={} clientId={} to {}", attempt, clientId, udpRemote);
      } catch (Throwable t) {
        LOGGER.warn("Error while sending RegisterUdp for clientId={}", clientId, t);
      }

      // Schedule the next retry if not cancelled
      if (!future.isCancelled()) {
        future = udp.eventLoop().schedule(this, UDP_REGISTER_INTERVAL_MS, TimeUnit.MILLISECONDS);
      }
    }
  }
}
