package core.network.server;

import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.loader.DungeonLoader;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.EntitySpawnEvent;
import core.network.messages.s2c.LevelChangeEvent;
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

  private static final int MAX_TCP_OBJECT_SIZE = 1_000_000;
  private static final int MAX_UDP_DATAGRAM = 65_507;
  private static final int SAFE_UDP_MTU = 1200;
  private static final int TCP_LENGTH_FIELD_OFFSET = 0;
  private static final int TCP_LENGTH_FIELD_LENGTH = 4;
  private static final int TCP_LENGTH_ADJUSTMENT = 0;
  private static final int TCP_INITIAL_BYTES_TO_STRIP = 4;

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

  public void start(int port) {
    if (bossGroup != null || workerGroup != null) {
      LOGGER.warn("Server already started");
      return;
    }

    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();

    startTcpServer(port);
    startUdpServer(port);

    LOGGER.info("ServerNetworkService started on port {} (TCP+UDP)", port);
  }

  public void stop() {
    try {
      if (tcpServerChannel != null) tcpServerChannel.close().syncUninterruptibly();
      if (udpChannel != null) udpChannel.close().syncUninterruptibly();
    } catch (Exception e) {
      LOGGER.warn("Error closing channels", e);
    } finally {
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
  }

  public ConcurrentLinkedQueue<InputMessage> inputQueue() {
    return inputQueue;
  }

  public Map<Integer, InetSocketAddress> udpClients() {
    return Map.copyOf(clientIdToUdp);
  }

  public Map<ChannelId, Integer> tcpClientMap() {
    return Map.copyOf(tcpChannelToClientId);
  }

  public Optional<String> clientName(int clientId) {
    return Optional.ofNullable(clientIdToName.get(clientId));
  }

  public void sendUdpObject(InetSocketAddress target, Object obj) {
    if (udpChannel == null || !udpChannel.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return;
    }
    try {
      byte[] data = serialize(obj);
      if (data.length > SAFE_UDP_MTU) {
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

  // ---------- Internal helpers & startup ----------

  private void startTcpServer(int port) {
    ServerBootstrap sb = new ServerBootstrap();
    sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(
                    new LengthFieldBasedFrameDecoder(
                        MAX_TCP_OBJECT_SIZE + TCP_LENGTH_FIELD_LENGTH,
                        TCP_LENGTH_FIELD_OFFSET,
                        TCP_LENGTH_FIELD_LENGTH,
                        TCP_LENGTH_ADJUSTMENT,
                        TCP_INITIAL_BYTES_TO_STRIP));
                p.addLast(new TcpServerHandler());
              }
            });
    tcpServerChannel = sb.bind(port).syncUninterruptibly().channel();
  }

  private void startUdpServer(int port) {
    Bootstrap ub = new Bootstrap();
    ub.group(workerGroup).channel(NioDatagramChannel.class).handler(new UdpServerHandler());
    udpChannel = ub.bind(port).syncUninterruptibly().channel();
  }

  private final class TcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) throws Exception {
      Object obj = deserialize(frame);
      if (obj instanceof ConnectRequest cr) {
        handleConnectRequest(ctx, cr);
      } else if (obj instanceof RequestEntitySpawn req) {
        handleRequestEntitySpawn(ctx, req);
      } else {
        LOGGER.debug("TCP received unexpected object: {}", obj.getClass().getName());
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
  }

  private final class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
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

      if (obj instanceof RegisterUdp reg) {
        handleRegisterUdp(packet.sender(), reg);
      } else if (obj instanceof InputMessage input) {
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
  }

  // ---------- Per-message handling ----------

  private void handleConnectRequest(ChannelHandlerContext ctx, ConnectRequest req) {
    String playerName = req.playerName();
    LOGGER.info("Received ConnectRequest from channel={} name='{}'", ctx.channel(), playerName);

    if (!isValidPlayerName(playerName)) {
      LOGGER.warn(
          "Invalid player name '{}' from channel={}; rejecting connection",
          playerName,
          ctx.channel());
      sendTcpObjectChecked(
          ctx,
          new ConnectReject(
              "Invalid player name. Must be non-empty, without underscores, and unique."));
      ctx.close();
      return;
    }

    int clientId = nextClientId.getAndIncrement();
    clientIdToName.put(clientId, playerName);
    tcpChannelToClientId.put(ctx.channel().id(), clientId);

    sendTcpObjectChecked(ctx, new ConnectAck(clientId));
    sendInitialLevelChange(ctx, clientId);
  }

  private void handleRequestEntitySpawn(ChannelHandlerContext ctx, RequestEntitySpawn req) {
    int entityId = req.entityId();
    LOGGER.info(
        "Received RequestEntitySpawn for entityId='{}' from channel={}", entityId, ctx.channel());

    Optional<core.Entity> entity = Game.levelEntities().filter(e -> e.id() == entityId).findFirst();

    if (entity.isEmpty()) {
      LOGGER.warn("Could not find entity with id='{}' to send to client", entityId);
      return;
    }
    core.Entity e = entity.get();
    PositionComponent pc = e.fetch(PositionComponent.class).orElse(null);
    DrawComponent dc = e.fetch(DrawComponent.class).orElse(null);

    if (pc == null || dc == null) {
      LOGGER.warn(
          "Entity with id='{}' is missing PositionComponent or DrawComponent, cannot send spawn event",
          entityId);
      return;
    }

    EntitySpawnEvent spawnEvent =
        new EntitySpawnEvent(
            e.id(),
            pc.position(),
            pc.viewDirection(),
            dc.currentAnimationPath(),
            dc.currentAnimationName(),
            dc.tintColor());

    sendTcpObjectChecked(ctx, spawnEvent);
  }

  private void handleRegisterUdp(InetSocketAddress sender, RegisterUdp reg) {
    int clientId = reg.clientId();
    boolean known = tcpChannelToClientId.containsValue(clientId);
    if (!known) {
      LOGGER.warn("Ignoring RegisterUdp for unknown clientId={} from {}", clientId, sender);
      return;
    }

    InetSocketAddress previous = clientIdToUdp.put(clientId, sender);
    if (previous == null || !previous.equals(sender)) {
      LOGGER.info("Registered/updated UDP for clientId={} addr={}", clientId, sender);
    } else {
      LOGGER.debug("Received redundant RegisterUdp for clientId={} addr={}", clientId, sender);
    }
  }

  // ---------- Utility sending helpers ----------

  private void sendInitialLevelChange(ChannelHandlerContext ctx, int clientId) {
    try {
      String levelName;
      try {
        levelName = DungeonLoader.currentLevel();
      } catch (Throwable t) {
        levelName = null;
      }
      LevelChangeEvent ev = new LevelChangeEvent(levelName, null);
      sendTcpObjectChecked(ctx, ev);
      LOGGER.info(
          "Sent initial LEVEL_CHANGE over TCP to clientId={} level={}", clientId, levelName);
    } catch (Exception e) {
      LOGGER.warn("Failed to send initial LEVEL_CHANGE over TCP", e);
    }
  }

  private void sendTcpObjectChecked(ChannelHandlerContext ctx, Object obj) {
    try {
      byte[] data = serialize(obj);
      if (data.length <= MAX_TCP_OBJECT_SIZE) {
        ByteBuf buf = ctx.alloc().buffer(4 + data.length);
        buf.writeInt(data.length);
        buf.writeBytes(data);
        ctx.writeAndFlush(buf);
        LOGGER.debug(
            "Sent TCP object={} to channel={}", obj.getClass().getSimpleName(), ctx.channel());
      } else {
        LOGGER.warn("{} too large, not sending", obj.getClass().getSimpleName());
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to send TCP object {}", obj.getClass().getSimpleName(), e);
    }
  }

  private boolean isValidPlayerName(String playerName) {
    return playerName != null
        && !playerName.isBlank()
        && !playerName.contains("_")
        && !clientIdToName.containsValue(playerName);
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
