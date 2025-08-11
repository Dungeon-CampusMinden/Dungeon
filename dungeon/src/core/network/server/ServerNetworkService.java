package core.network.server;

import core.level.loader.DungeonLoader;
import core.network.SnapshotTranslator;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.messages.c2s.RegisterUdp;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal Netty-based server service that accepts TCP clients and learns UDP addresses.
 *
 * <p>Responsibilities: - Listens on TCP and UDP on the same port - Assigns an incremental clientId
 * per TCP connection and replies with {@link ConnectAck} - Learns client UDP address either from a
 * UDP {@link RegisterUdp} or from a future UDP message - Decodes UDP {@link InputMessage} objects
 * and enqueues them for server simulation
 *
 * <p>Prototype quality; intended to be replaced by a production transport later.
 */
public final class ServerNetworkService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerNetworkService.class);

  private static final int MAX_TCP_OBJECT_SIZE = 1_000_000; // 1 MiB payload limit
  private static final int MAX_UDP_DATAGRAM = 65_507; // theoretical IPv4 UDP payload max

  private final ConcurrentLinkedQueue<InputMessage> inputQueue = new ConcurrentLinkedQueue<>();
  private final ConcurrentHashMap<Integer, InetSocketAddress> clientIdToUdp =
    new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ChannelId, Integer> tcpChannelToClientId =
    new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, String> clientIdToName = new ConcurrentHashMap<>();
  private final AtomicInteger nextClientId = new AtomicInteger(1);

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel tcpServerChannel;
  private Channel udpChannel;
  private volatile SnapshotTranslator snapshotTranslator;

  /** Starts the server on the given port (binds TCP and UDP). */
  public void start(int port) {
    if (bossGroup != null || workerGroup != null) {
      LOGGER.warn("Server already started");
      return;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();

    // Start TCP server
    ServerBootstrap sb = new ServerBootstrap();
    sb.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(
        new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new LengthFieldBasedFrameDecoder(MAX_TCP_OBJECT_SIZE + 4, 0, 4, 0, 4));
            p.addLast(
              new SimpleChannelInboundHandler<ByteBuf>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame)
                  throws Exception {
                  Object obj = deserialize(frame);
                  if (obj instanceof ConnectRequest(String playerName)) {
                    LOGGER.info(
                      "Received ConnectRequest from channel={} name='{}'",
                      ctx.channel(),
                      playerName);

                    if (!isValidPlayerName(playerName)) {
                      LOGGER.warn(
                        "Invalid player name '{}' from channel={}; rejecting connection",
                        playerName,
                        ctx.channel());
                      ctx.close();
                      return;
                    }

                    // Append incremental int for duplicate names
                    if (clientIdToName.containsValue(playerName)) {
                      while (clientIdToName.containsValue(playerName + "_" + nextClientId.get())) {
                        nextClientId.incrementAndGet();
                      }
                    }

                    int clientId = nextClientId.getAndIncrement();
                    clientIdToName.put(clientId, playerName);
                    tcpChannelToClientId.put(ctx.channel().id(), clientId);

                    // Send ConnectAck
                    try {
                      byte[] data = serialize(new ConnectAck(clientId));
                      if (data.length <= MAX_TCP_OBJECT_SIZE) {
                        ByteBuf buf = ctx.alloc().buffer(4 + data.length);
                        buf.writeInt(data.length);
                        buf.writeBytes(data);
                        ctx.writeAndFlush(buf);
                        LOGGER.info("Sent ConnectAck clientId={} to channel={}", clientId, ctx.channel());
                      } else {
                        LOGGER.warn("ConnectAck too large, not sending");
                      }
                    } catch (Exception e) {
                      LOGGER.warn("Failed to send ConnectAck", e);
                    }

                    // Send current level info reliably over TCP for late joiners
                    try {
                      String levelName;
                      try {
                        levelName = DungeonLoader.currentLevel();
                      } catch (Throwable t) {
                        levelName = null;
                      }
                      // TODO: Change spawnpoint to last known or any default
                      byte[] payload = serialize(new LevelChangeEvent(levelName, null));
                      if (payload.length <= MAX_TCP_OBJECT_SIZE) {
                        ByteBuf buf2 = ctx.alloc().buffer(4 + payload.length);
                        buf2.writeInt(payload.length);
                        buf2.writeBytes(payload);
                        ctx.writeAndFlush(buf2);
                        LOGGER.info(
                          "Sent initial LEVEL_CHANGE over TCP to clientId={} level={}",
                          clientId,
                          levelName);
                      } else {
                        LOGGER.warn("LEVEL_CHANGE payload too large, not sending over TCP");
                      }
                    } catch (Exception e) {
                      LOGGER.warn("Failed to send initial LEVEL_CHANGE over TCP", e);
                    }
                  } else {
                    // Other objects can be logged or ignored for now
                    LOGGER.debug(
                      "TCP received unexpected object: {}", obj.getClass().getName());
                  }
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) {
                  Integer id = tcpChannelToClientId.remove(ctx.channel().id());
                  if (id != null) {
                    clientIdToUdp.remove(id);
                    LOGGER.info("Client disconnected id={} {}", id, ctx.channel());
                  }
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                  LOGGER.warn("TCP handler error for {}", ctx.channel(), cause);
                  ctx.close();
                }
              });
          }
        });
    tcpServerChannel = sb.bind(port).syncUninterruptibly().channel();

    // Start UDP server (same port)
    Bootstrap ub = new Bootstrap();
    ub.group(workerGroup)
      .channel(NioDatagramChannel.class)
      .handler(
        new SimpleChannelInboundHandler<DatagramPacket>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
            ByteBuf content = packet.content();
            int size = content.readableBytes();
            if (size <= 0 || size > MAX_UDP_DATAGRAM) {
              LOGGER.warn("Dropping UDP packet size={} from {}", size, packet.sender());
              return;
            }

            final Object obj;
            try {
              obj = deserialize(content);
            } catch (Exception e) {
              LOGGER.warn("Failed to deserialize UDP packet from {}", packet.sender(), e);
              return;
            }

            if (obj instanceof RegisterUdp(int clientId)) {
              // Validate that the clientId is known via an active TCP mapping
              boolean known = tcpChannelToClientId.containsValue(clientId);
              if (!known) {
                LOGGER.warn(
                  "Ignoring RegisterUdp for unknown clientId={} from {}", clientId, packet.sender());
                return;
              }

              // Map clientId to UDP address for future communication (update if changed)
              InetSocketAddress previous = clientIdToUdp.put(clientId, packet.sender());
              if (previous == null || !previous.equals(packet.sender())) {
                LOGGER.info(
                  "Registered/updated UDP for clientId={} addr={}", clientId, packet.sender());
              } else {
                LOGGER.debug("Received redundant RegisterUdp for clientId={} addr={}", clientId, packet.sender());
              }
            } else if (obj instanceof InputMessage input) {
              // Enqueue input for simulation
              inputQueue.offer(input);
            } else {
              LOGGER.debug(
                "UDP received unrecognized object type={} from {}",
                obj.getClass().getName(),
                packet.sender());
            }
          }

          @Override
          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.warn("UDP handler error", cause);
          }
        });
    udpChannel = ub.bind(port).syncUninterruptibly().channel();

    LOGGER.info("ServerNetworkService started on port {} (TCP+UDP)", port);
  }

  /** Stops the server and releases resources. */
  public void stop() {
    try {
      if (tcpServerChannel != null) tcpServerChannel.close().syncUninterruptibly();
      if (udpChannel != null) udpChannel.close().syncUninterruptibly();
    } catch (Exception e) {
      LOGGER.warn("Error closing channels", e);
    }
    try {
      if (bossGroup != null) bossGroup.shutdownGracefully();
      if (workerGroup != null) workerGroup.shutdownGracefully();
    } catch (Exception e) {
      LOGGER.warn("Error shutting down event loops", e);
    }
    bossGroup = null;
    workerGroup = null;
    LOGGER.info("ServerNetworkService stopped");
  }

  /** Queue of decoded InputMessage instances received via UDP. */
  public ConcurrentLinkedQueue<InputMessage> inputQueue() {
    return inputQueue;
  }

  /** Snapshot of known UDP client addresses keyed by clientId. */
  public Map<Integer, InetSocketAddress> udpClients() {
    return Map.copyOf(clientIdToUdp);
  }

  /** Snapshot mapping TCP ChannelId to assigned clientId. */
  public Map<ChannelId, Integer> tcpClientMap() {
    return Map.copyOf(tcpChannelToClientId);
  }

  /** Returns the configured SnapshotTranslator, lazily defaulting to a default instance. */
  public SnapshotTranslator snapshotTranslator() {
    SnapshotTranslator t = snapshotTranslator;
    if (t == null)
      throw new IllegalStateException(
        "SnapshotTranslator not set on ServerNetworkService. Set via setSnapshotTranslator(...) before starting server loop.");
    return t;
  }

  /** Sets the SnapshotTranslator for server-side snapshot building. */
  public void setSnapshotTranslator(SnapshotTranslator translator) {
    if (translator != null) this.snapshotTranslator = translator;
  }

  /**
   * Gets the client name for the given clientId, or null if not known.
   *
   * @param clientId The client ID to look up.
   */
  public Optional<String> clientName(int clientId) {
    return Optional.ofNullable(clientIdToName.get(clientId));
  }

  /**
   * Sends the given object via UDP to the target address, using Java serialization. Drops payloads
   * that exceed a conservative MTU (~1200 bytes) to avoid fragmentation.
   */
  public void sendUdpObject(InetSocketAddress target, Object obj) {
    if (udpChannel == null || !udpChannel.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return;
    }
    try {
      byte[] data = serialize(obj);
      // MTU guard
      int safeMtu = 1200;
      if (data.length > safeMtu) {
        LOGGER.warn("Skipping UDP send; payload too large ({} bytes) to {}", data.length, target);
        return;
      }
      udpChannel
        .writeAndFlush(
          new DatagramPacket(udpChannel.alloc().buffer(data.length).writeBytes(data), target))
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    } catch (Exception e) {
      LOGGER.warn("Failed to send UDP object to {}", target, e);
    }
  }

  private boolean isValidPlayerName(String playerName) {
    return playerName != null && !playerName.isBlank() && !playerName.contains("_");
  }

  // -- Serialization helpers --
  private static byte[] serialize(Object obj) throws Exception {
    if (!(obj instanceof Serializable)) {
      throw new NotSerializableException("Object not serializable: " + Objects.toString(obj));
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    return bos.toByteArray();
  }

  private static Object deserialize(ByteBuf buf) throws Exception {
    byte[] array = new byte[buf.readableBytes()];
    buf.getBytes(buf.readerIndex(), array);
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array))) {
      return ois.readObject();
    }
  }
}
