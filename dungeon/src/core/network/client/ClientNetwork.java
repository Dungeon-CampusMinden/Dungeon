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
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.RegisterAck;
import core.network.server.ClientState;
import core.network.server.Session;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.MultiThreadIoEventLoopGroup;
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
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

  private static final short CLIENT_PROTOCOL_VERSION = 1;
  private static final String LAST_SESSION_FILE_NAME = "last_session.dat";
  private final MessageDispatcher dispatcher = new MessageDispatcher();
  private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
  private final Queue<Tuple<Session, NetworkMessage>> inboundQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Runnable> lifecycleEvents = new ConcurrentLinkedQueue<>();

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);

  private volatile Session session;

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
   * <p>This method allocates the event loop group and prepares the UDP remote address. It does not
   * open sockets call {@link #start()} to connect.
   *
   * @param host server hostname or IP to connect to
   * @param port server port
   * @param username username used in the TCP ConnectRequest handshake
   */
  public void initialize(String host, int port, String username) {
    this.remoteHost = host;
    this.port = port;
    this.username = username;
    this.group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
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
   * <p>Encodes the message using protobuf and sends a datagram. If the payload exceeds the safe UDP
   * MTU, it uses TCP instead.
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
        dispatcher.dispatch(msg.a(), msg.b());
      } catch (Exception e) {
        LOGGER.error("Dispatch error", e);
      }
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
                        NetworkMessage msg = deserialize(frame);
                        if (msg
                            instanceof ConnectAck(short id, int sessionId, byte[] sessionToken)) {
                          onConnectAck(id, sessionId, sessionToken);
                        } else if (msg instanceof ConnectReject(byte reason)) {
                          onConnectReject(session, ConnectReject.Reason.fromCode(reason));
                        } else if (msg instanceof RegisterAck(boolean ok)) {
                          onRegisterAck(ok);
                        } else {
                          onNetworkMessage(msg, size);
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
                          byte[] data;
                          // Attempt to load last session from file
                          Tuple<Integer, byte[]> lastSession = loadLastSessionFromFile();
                          if (lastSession != null) {
                            int sessionId = lastSession.a();
                            byte[] sessionToken = lastSession.b();
                            LOGGER.info(
                                "Loaded last session from file: sessionId={}; Trying to reconnect.",
                                sessionId);
                            data =
                                serialize(
                                    new ConnectRequest(
                                        CLIENT_PROTOCOL_VERSION,
                                        username,
                                        sessionId,
                                        sessionToken));
                          } else {
                            LOGGER.info("No valid last session file found; starting new session.");
                            data = serialize(new ConnectRequest(CLIENT_PROTOCOL_VERSION, username));
                          }
                          if (data.length <= MAX_TCP_OBJECT_SIZE) {
                            ByteBuf buf = ctx.alloc().buffer(4 + data.length);
                            buf.writeInt(data.length);
                            buf.writeBytes(data);
                            ctx.writeAndFlush(buf);
                          } else {
                            LOGGER.error(
                                "ConnectRequest too large ({} bytes); cannot connect", data.length);
                            enqueueLifecycle(() -> notifyDisconnected("ConnectRequest too large"));
                            Game.exit("Unable to connect to server at " + remoteHost + ":" + port);
                          }
                        } catch (IOException e) {
                          LOGGER.warn("Failed to send ConnectRequest", e);
                        }
                      }

                      @Override
                      public void channelInactive(ChannelHandlerContext ctx) {
                        LOGGER.info("TCP connection closed by server");
                        connected.set(false);
                        enqueueLifecycle(() -> notifyDisconnected(null));
                        // invalidateLastSessionFile(); // TODO: decide if we want this
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
    try {
      ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, port)).syncUninterruptibly();
      tcp = f.channel();
    } catch (Exception e) {
      if (e.getCause() instanceof ConnectException) {
        LOGGER.error(
            "Failed to connect TCP to server at {}:{} - {}", remoteHost, port, e.getMessage());
        enqueueLifecycle(() -> notifyDisconnected("Connection refused"));
        Game.exit("Unable to connect to server at " + remoteHost + ":" + port);
      } else {
        throw e;
      }
    }
  }

  private void onNetworkMessage(NetworkMessage msg, int size) {
    if (session != null) {
      inboundQueue.offer(Tuple.of(session, msg));
    } else {
      LOGGER.debug(
          "Dropping TCP inbound before session init: {} size={}B",
          msg.getClass().getSimpleName(),
          size);
    }
    LOGGER.debug("TCP inbound {} size={}B", msg.getClass().getSimpleName(), size);
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
                  NetworkMessage msg = deserialize(pkt.content());
                  if (session != null) {
                    inboundQueue.offer(Tuple.of(session, msg));
                  } else {
                    LOGGER.debug(
                        "Dropping UDP inbound before session init: {}",
                        msg.getClass().getSimpleName());
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
    try {
      udp.connect(udpRemote).syncUninterruptibly();
    } catch (Exception e) {
      if (e.getCause() instanceof ConnectException) {
        LOGGER.error(
            "Failed to connect UDP to server at {}:{} - {}", remoteHost, port, e.getMessage());
        enqueueLifecycle(() -> notifyDisconnected("Connection refused"));
        Game.exit("Unable to connect to server at " + remoteHost + ":" + port);
      } else {
        throw e;
      }
    }
    // If TCP session already exists, update its udpAddress to our peer
    if (session != null) session.udpAddress(udpRemote);
    LOGGER.info("Client connected to {}:{} (TCP+UDP)", remoteHost, port);
  }

  private void onConnectAck(short newClientId, int sessionId, byte[] sessionToken) {
    this.clientId = newClientId;
    LOGGER.info("Received ConnectAck clientId={}, sessionId={}", newClientId, sessionId);
    session.attachClientState(new ClientState(newClientId, username, sessionId, sessionToken));
    scheduleUdpRegistration(newClientId);
    saveLastSessionToFile(sessionId, sessionToken);
  }

  private void onConnectReject(Session session, ConnectReject.Reason reason) {
    String reasonStr = "Connection rejected by server: " + reason;
    LOGGER.warn(reasonStr);
    if (reason == ConnectReject.Reason.NO_SESSION_FOUND
        || reason == ConnectReject.Reason.INVALID_SESSION_TOKEN) {
      // Invalidate last session file upon session-related rejections; Try to connect again without
      // session
      invalidateLastSessionFile();
      session.sendMessage(new ConnectRequest(CLIENT_PROTOCOL_VERSION, username), true);
    } else {
      // Close the connection upon rejection
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
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(msg);
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
