package core.network.netty;

import core.network.*;
import core.network.messages.ConnectAck;
import core.network.messages.ConnectRequest;
import core.network.messages.InputMessage;
import core.network.messages.NetworkMessage;
import core.network.messages.RegisterUdp;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty-backed network handler that supports TCP (reliable) and UDP (unreliable) transport.
 *
 * <p>Design constraints: - IO threads only enqueue fully deserialized {@link NetworkMessage}s -
 * Game loop must invoke {@link #pollAndDispatch()} on the game thread to process queued messages -
 * Java native serialization is used initially (Object streams) - TCP uses a simple 4-byte
 * big-endian length-prefixed frame (max 1 MiB payload) - UDP uses datagrams; messages are
 * serialized into a byte array (≈1400 B cap)
 *
 * <p>Note: Message classes intended for transport through this handler must implement {@link
 * java.io.Serializable}.
 *
 * <p>Usage (client):
 *
 * <pre>
 * NettyNetworkHandler h = new NettyNetworkHandler();
 * h._setRawMessageConsumer(h.messageDispatcher()::dispatch);
 * h.addConnectionListener(new ConnectionListener() { ... });
 * h.initialize(false, "127.0.0.1", 7777);
 * h.start();
 * // in game loop each frame:
 * h.pollAndDispatch();
 * </pre>
 */
public final class NettyNetworkHandler implements INetworkHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyNetworkHandler.class);

  private static final LocalNetworkHandler LOCAL_HANDLER =
    new LocalNetworkHandler(); // for local prediction

  // 1 MiB guard for object size; protects against excessively large frames
  private static final int MAX_OBJECT_SIZE = 1 << 20;
  // Conservative UDP payload limit to avoid fragmentation on typical networks
  private static final int MAX_UDP_PAYLOAD = 1400;

  private final MessageDispatcher dispatcher = new MessageDispatcher();
  private volatile Consumer<NetworkMessage> rawMessageConsumer;
  private final List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
  private final Queue<NetworkMessage> inboundQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Runnable> lifecycleEvents = new ConcurrentLinkedQueue<>();

  private final AtomicBoolean initialized = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);

  private String username;
  private boolean serverMode;
  private String remoteHost;
  private int port;

  // Event loop groups
  private EventLoopGroup bossGroup; // server TCP acceptor
  private EventLoopGroup workerGroup; // server TCP workers and UDP
  private EventLoopGroup clientGroup; // client TCP/UDP

  // Channels
  private Channel serverTcpChannel; // server listening socket
  private Channel clientTcpChannel; // client TCP connection
  private Channel udpChannel; // UDP channel (server bind or client bind)

  // Remote for UDP sends (in client mode)
  private InetSocketAddress udpRemote;
  private volatile Integer assignedClientId; // set after ConnectAck
  private volatile SnapshotTranslator translator;

  @Override
  public void initialize(boolean isServer, String serverAddress, int port, String username) throws NetworkException {
    if (initialized.get()) return;
    this.serverMode = isServer;
    this.remoteHost = serverAddress;
    this.port = port;
    this.username = username;
    try {
      if (serverMode) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
      } else {
        clientGroup = new NioEventLoopGroup();
        udpRemote = new InetSocketAddress(serverAddress, port);
      }
      initialized.set(true);
    } catch (Exception e) {
      throw new NetworkException("Failed to initialize Netty groups", e);
    }
  }

  @Override
  public void start() {
    if (!initialized.get()) {
      LOGGER.error("NettyNetworkHandler cannot start before initialize().");
      return;
    }
    if (!running.compareAndSet(false, true)) return;

    if (serverMode) {
      startServer();
    } else {
      startClient();
    }
  }

  private void startServer() {
    try {
      // TCP server
      ServerBootstrap sb = new ServerBootstrap();
      sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
          new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ChannelPipeline p = ch.pipeline();
              p.addLast(new LengthFieldBasedFrameDecoder(MAX_OBJECT_SIZE + 4, 0, 4, 0, 4));
              p.addLast(
                new SimpleChannelInboundHandler<ByteBuf>() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
                    try {
                      int size = frame.readableBytes();
                      Object object = deserialize(frame);
                      if (object instanceof NetworkMessage nm) {
                        inboundQueue.offer(nm);
                        LOGGER.info(
                          "TCP server inbound message type={} size={}B",
                          object.getClass().getSimpleName(),
                          size);
                      }
                    } catch (Exception e) {
                      LOGGER.warn("TCP server decode error", e);
                    }
                  }

                  @Override
                  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    LOGGER.warn("TCP server handler error", cause);
                    ctx.close();
                    notifyDisconnected(cause);
                  }
                });
            }
          });
      ChannelFuture bindFuture = sb.bind(port).syncUninterruptibly();
      serverTcpChannel = bindFuture.channel();

      // UDP server (bind to same port)
      Bootstrap ub = new Bootstrap();
      ub.group(workerGroup)
        .channel(NioDatagramChannel.class)
        .handler(
          new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
              try {
                Object object = deserialize(packet.content());
                if (object instanceof NetworkMessage nm) {
                  inboundQueue.offer(nm);
                }
              } catch (Exception e) {
                LOGGER.warn("UDP server decode error", e);
              }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
              LOGGER.warn("UDP server handler error", cause);
            }
          });
      ChannelFuture udpBind = ub.bind(port).syncUninterruptibly();
      udpChannel = udpBind.channel();

      connected.set(true);
      enqueueLifecycle(this::notifyConnected);
      LOGGER.info("NettyNetworkHandler server started on port " + port);
    } catch (Exception e) {
      LOGGER.error("Failed to start Netty server", e);
      notifyDisconnected(e);
    }
  }

  private void startClient() {
    try {
      // TCP client
      Bootstrap cb = new Bootstrap();
      cb.group(clientGroup)
        .channel(NioSocketChannel.class)
        .handler(
          new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ChannelPipeline p = ch.pipeline();
              p.addLast(new LengthFieldBasedFrameDecoder(MAX_OBJECT_SIZE + 4, 0, 4, 0, 4));
              p.addLast(
                new SimpleChannelInboundHandler<ByteBuf>() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
                    try {
                      int size = frame.readableBytes();
                      Object object = deserialize(frame);
                      if (object instanceof ConnectAck(int clientId)) {
                        assignedClientId = clientId;
                        LOGGER.info("Received ConnectAck clientId={}", clientId);
                        // Send dedicated RegisterUDP message with retransmit attempts
                        try {
                          if (udpChannel != null
                            && udpChannel.isActive()
                            && udpRemote != null) {
                            byte[] payload = serialize(new RegisterUdp(clientId));
                            if (payload.length <= MAX_UDP_PAYLOAD) {
                              LOGGER.info(
                                "Sending UDP registration datagram bytes={} to {}",
                                payload.length,
                                udpRemote);
                              udpChannel.writeAndFlush(
                                new DatagramPacket(
                                  udpChannel
                                    .alloc()
                                    .buffer(payload.length)
                                    .writeBytes(payload),
                                  udpRemote));

                              // Retransmit a few times in case of packet loss / NAT
                              final AtomicInteger attempts = new AtomicInteger(1);
                              final AtomicReference<ScheduledFuture<?>> sfRef =
                                new AtomicReference<>();
                              Runnable retryTask =
                                () -> {
                                  try {
                                    int a = attempts.incrementAndGet();
                                    if (a > 5) {
                                      ScheduledFuture<?> f = sfRef.get();
                                      if (f != null) f.cancel(false);
                                      return;
                                    }
                                    if (udpChannel != null && udpChannel.isActive()) {
                                      udpChannel.writeAndFlush(
                                        new DatagramPacket(
                                          udpChannel
                                            .alloc()
                                            .buffer(payload.length)
                                            .writeBytes(payload),
                                          udpRemote));
                                      LOGGER.info(
                                        "Retransmitted UDP RegisterUdp attempt={} clientId={} to {}",
                                        a,
                                        clientId,
                                        udpRemote);
                                    }
                                  } catch (Throwable t) {
                                    LOGGER.warn("Error in RegisterUdp retransmit task", t);
                                  }
                                };
                              ScheduledFuture<?> sf =
                                udpChannel
                                  .eventLoop()
                                  .scheduleAtFixedRate(
                                    retryTask, 500, 500, TimeUnit.MILLISECONDS);
                              sfRef.set(sf);
                            } else {
                              LOGGER.warn("RegisterUdp payload too large, not sending");
                            }
                          }
                        } catch (IOException e) {
                          LOGGER.warn("Failed to send UDP registration datagram", e);
                        }
                      } else if (object instanceof NetworkMessage nm) {
                        inboundQueue.offer(nm);
                        LOGGER.info(
                          "TCP client inbound message type={} size={}B",
                          object.getClass().getSimpleName(),
                          size);
                      }
                    } catch (Exception e) {
                      LOGGER.warn("TCP client decode error", e);
                    }
                  }

                  @Override
                  public void channelActive(ChannelHandlerContext ctx) {
                    connected.set(true);
                    enqueueLifecycle(NettyNetworkHandler.this::notifyConnected);
                    // Send ConnectRequest on TCP connect
                    try {
                      byte[] data = serialize(new ConnectRequest(username));
                      if (data.length <= MAX_OBJECT_SIZE) {
                        ByteBuf buf = ctx.alloc().buffer(4 + data.length);
                        buf.writeInt(data.length);
                        buf.writeBytes(data);
                        ctx.writeAndFlush(buf);
                      } else {
                        LOGGER.warn("ConnectRequest too large, skipping");
                      }
                    } catch (IOException e) {
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
                    LOGGER.warn("TCP client handler error", cause);
                    ctx.close();
                    connected.set(false);
                    enqueueLifecycle(() -> notifyDisconnected(cause));
                  }
                });
            }
          });
      ChannelFuture connectFuture =
        cb.connect(new InetSocketAddress(remoteHost, port)).syncUninterruptibly();
      clientTcpChannel = connectFuture.channel();

      // UDP client (bind to ephemeral port and connect logically to server for convenience)
      Bootstrap ub = new Bootstrap();
      ub.group(clientGroup)
        .channel(NioDatagramChannel.class)
        .handler(
          new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
              try {
                Object object = deserialize(packet.content());
                if (object instanceof NetworkMessage nm) inboundQueue.offer(nm);
              } catch (Exception e) {
                LOGGER.warn("UDP client decode error", e);
              }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
              LOGGER.warn("UDP client handler error", cause);
            }
          });
      udpChannel = ub.bind(0).syncUninterruptibly().channel();
      // Optional connect to filter inbound
      udpChannel.connect(udpRemote).syncUninterruptibly();

      LOGGER.info("NettyNetworkHandler client connected to " + remoteHost + ":" + port);
    } catch (Exception e) {
      LOGGER.error("Failed to start Netty client", e);
      enqueueLifecycle(() -> notifyDisconnected(e));
    }
  }

  @Override
  public void shutdown() {
    if (!running.compareAndSet(true, false)) return;
    try {
      if (serverTcpChannel != null) serverTcpChannel.close().syncUninterruptibly();
      if (clientTcpChannel != null) clientTcpChannel.close().syncUninterruptibly();
      if (udpChannel != null) udpChannel.close().syncUninterruptibly();
    } catch (Exception e) {
      LOGGER.warn("Error while closing channels", e);
    }

    try {
      if (bossGroup != null) bossGroup.shutdownGracefully();
      if (workerGroup != null) workerGroup.shutdownGracefully();
      if (clientGroup != null) clientGroup.shutdownGracefully();
    } catch (Exception e) {
      LOGGER.warn("Error while shutting down event loops", e);
    }

    connected.set(false);
    enqueueLifecycle(() -> notifyDisconnected(null));
    LOGGER.info("NettyNetworkHandler shutdown complete.");
  }

  @Override
  public boolean isConnected() {
    return connected.get();
  }

  @Override
  public boolean isServer() {
    return serverMode;
  }

  /** Returns the assigned clientId after ConnectAck or 0 if not yet assigned. */
  public int getAssignedClientId() {
    Integer v = assignedClientId;
    return v != null ? v : 0;
  }

  @Override
  public MessageDispatcher messageDispatcher() {
    return dispatcher;
  }

  /**
   * Returns the configured SnapshotTranslator or throws if not set.
   *
   * <p>Explicit injection required: callers must set a translator before use.
   */
  @Override
  public SnapshotTranslator snapshotTranslator() {
    SnapshotTranslator t = translator;
    if (t == null)
      throw new IllegalStateException(
        "SnapshotTranslator not set on INetworkHandler. Set via setSnapshotTranslator(...) before starting network or provide translator in starter.");
    return t;
  }

  /** Sets the SnapshotTranslator to be used by this handler. */
  @Override
  public void setSnapshotTranslator(SnapshotTranslator translator) {
    if (translator != null) this.translator = translator;
  }

  @Override
  public void _setRawMessageConsumer(Consumer<NetworkMessage> rawMessageConsumer) {
    this.rawMessageConsumer = rawMessageConsumer;
  }

  @Override
  public void sendInput(InputMessage input) {
    int knownId = assignedClientId != null ? assignedClientId : 0;
    if (input.clientId() <= 0) {
      if (knownId <= 0) {
        LOGGER.info("Dropping InputMessage: no assigned clientId yet");
        return;
      }
      input = new InputMessage(knownId, input.action(), input.point());
    }
    if (!running.get()) {
      LOGGER.warn("sendInput called while handler not running");
      return;
    }
    if (!isConnected()) {
      LOGGER.warn("sendInput called while not connected");
      return;
    }
    Channel ch = udpChannel;
    if (ch == null || !ch.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send unreliable input");
      return;
    }
    try {
      byte[] data = serialize(input);
      if (data.length > MAX_UDP_PAYLOAD) {
        LOGGER.warn(
          "Dropping UDP input; payload too large ({} bytes) > {}",
          data.length,
          MAX_UDP_PAYLOAD);
        return;
      }
      ch.writeAndFlush(
          new DatagramPacket(ch.alloc().buffer(data.length).writeBytes(data), udpRemote))
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    } catch (IOException e) {
      LOGGER.warn("Failed to serialize UDP input", e);
    }
  }

  @Override
  public void addConnectionListener(ConnectionListener listener) {
    if (listener != null) connectionListeners.add(listener);
  }

  @Override
  public void removeConnectionListener(ConnectionListener listener) {
    if (listener != null) connectionListeners.remove(listener);
  }

  /**
   * Drains all queued messages and delivers them to the raw consumer on the caller's thread.
   *
   * <p>Call this from the game loop to ensure that all message processing runs on the game thread
   * (not on Netty's IO threads).
   */
  public void pollAndDispatch() {
    // First, process lifecycle events to keep connection state callbacks on the game thread
    Runnable r;
    while ((r = lifecycleEvents.poll()) != null) {
      try {
        r.run();
      } catch (Exception e) {
        LOGGER.warn("Error running lifecycle event", e);
      }
    }
    NetworkMessage msg;
    while ((msg = inboundQueue.poll()) != null) {
      try {
        Consumer<NetworkMessage> consumer = this.rawMessageConsumer;
        if (consumer != null) consumer.accept(msg);
        else dispatcher.dispatch(msg);
      } catch (Exception e) {
        LOGGER.error("Error dispatching inbound message", e);
      }
    }
  }

  private void notifyConnected() {
    for (ConnectionListener l : connectionListeners) {
      try {
        l.onConnected();
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onConnected error", e);
      }
    }
  }

  private void enqueueLifecycle(Runnable runnable) {
    if (runnable != null) lifecycleEvents.offer(runnable);
  }

  private void notifyDisconnected(Throwable cause) {
    for (ConnectionListener l : connectionListeners) {
      try {
        l.onDisconnected(cause);
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onDisconnected error", e);
      }
    }
  }

  // -- Serialization helpers for UDP --
  private static byte[] serialize(Object obj) throws IOException {
    if (!(obj instanceof Serializable)) {
      throw new NotSerializableException(
        "Message does not implement Serializable: " + obj.getClass().getName());
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    return bos.toByteArray();
  }

  private static Object deserialize(io.netty.buffer.ByteBuf buf)
    throws IOException, ClassNotFoundException {
    byte[] array;
    int len = buf.readableBytes();
    array = new byte[len];
    buf.getBytes(buf.readerIndex(), array);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array))) {
      return ois.readObject();
    }
  }
}
