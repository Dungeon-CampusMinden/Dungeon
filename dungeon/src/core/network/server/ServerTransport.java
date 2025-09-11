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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static core.network.codec.NetworkCodec.deserialize;
import static core.network.codec.NetworkCodec.serialize;
import static core.network.config.NetworkConfig.*;

public final class ServerTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTransport.class);

  private final ConcurrentLinkedQueue<InputMessage> inputQueue =
    new ConcurrentLinkedQueue<>();
  private final ConcurrentHashMap<Integer, InetSocketAddress> clientIdToUdp =
    new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ChannelId, Integer> tcpChannelToClientId =
    new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, String> clientIdToName =
    new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ChannelId, ChannelHandlerContext> tcpChannels =
    new ConcurrentHashMap<>();
  private final AtomicInteger nextClientId = new AtomicInteger(1);

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel tcpServer;
  private Channel udpChannel;

  public void start(int port) {
    if (bossGroup != null || workerGroup != null) {
      LOGGER.warn("Server already started");
      return;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    startTcp(port);
    startUdp(port);
    LOGGER.info("ServerTransport started on port {} (TCP+UDP)", port);
  }

  public void stop() {
    try {
      if (tcpServer != null) tcpServer.close().syncUninterruptibly();
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
      LOGGER.info("ServerTransport stopped");
    }
  }

  public ConcurrentLinkedQueue<InputMessage> inputQueue() {
    return inputQueue;
  }

  public Map<Integer, InetSocketAddress> udpClients() {
    return Map.copyOf(clientIdToUdp);
  }

  public Map<ChannelId, ChannelHandlerContext> tcpChannels() {
    return Map.copyOf(tcpChannels);
  }

  public Map<ChannelId, Integer> tcpClientMap() {
    return Map.copyOf(tcpChannelToClientId);
  }

  public Optional<String> clientName(int clientId) {
    return Optional.ofNullable(clientIdToName.get(clientId));
  }

  CompletableFuture<Boolean> sendUdpObject(InetSocketAddress target, Object obj) {
    if (udpChannel == null || !udpChannel.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(obj);
      if (data.length > SAFE_UDP_MTU) {
        LOGGER.warn("Skip UDP send; payload too large ({} B) to {}", data.length,
          target);
        return CompletableFuture.completedFuture(false);
      }
      udpChannel.writeAndFlush(new DatagramPacket(
          udpChannel.alloc().buffer(data.length).writeBytes(data), target))
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    } catch (Exception e) {
      LOGGER.warn("Failed to send UDP object to {}", target, e);
      return CompletableFuture.completedFuture(false);
    }
    return CompletableFuture.completedFuture(true);
  }

  // ---- internals ----

  private void startTcp(int port) {
    ServerBootstrap sb = new ServerBootstrap();
    sb.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
          ChannelPipeline p = ch.pipeline();
          p.addLast(new LengthFieldBasedFrameDecoder(
            MAX_TCP_OBJECT_SIZE + TCP_LENGTH_FIELD_LENGTH,
            TCP_LENGTH_FIELD_OFFSET,
            TCP_LENGTH_FIELD_LENGTH,
            TCP_LENGTH_ADJUSTMENT,
            TCP_INITIAL_BYTES_TO_STRIP));
          p.addLast(new TcpServerHandler());
        }
      });
    tcpServer = sb.bind(port).syncUninterruptibly().channel();
  }

  private void startUdp(int port) {
    Bootstrap ub = new Bootstrap();
    ub.group(workerGroup)
      .channel(NioDatagramChannel.class)
      .handler(new UdpServerHandler());
    udpChannel = ub.bind(port).syncUninterruptibly().channel();
  }

  private final class TcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      tcpChannels.put(ctx.channel().id(), ctx);
      super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame)
      throws Exception {
      Object obj = deserialize(frame);
      if (obj instanceof ConnectRequest cr) {
        onConnectRequest(ctx, cr);
      } else if (obj instanceof RequestEntitySpawn req) {
        onRequestEntitySpawn(ctx, req);
      } else {
        LOGGER.debug("TCP received unexpected object: {}",
          obj.getClass().getName());
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      Integer id = tcpChannelToClientId.remove(ctx.channel().id());
      tcpChannels.remove(ctx.channel().id());
      if (id != null) {
        clientIdToUdp.remove(id);
        clientIdToName.remove(id);
        LOGGER.info("Client disconnected id={} {}", id, ctx.channel());
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      LOGGER.warn("TCP handler error for {}", ctx.channel(), cause);
      ctx.close();
    }
  }

  private final class UdpServerHandler
    extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket pkt) {
      ByteBuf content = pkt.content();
      int size = content.readableBytes();
      if (size <= 0) return;

      final Object obj;
      try {
        obj = deserialize(content);
      } catch (Exception e) {
        LOGGER.warn("Failed to deserialize UDP from {}", pkt.sender(), e);
        return;
      }

      if (obj instanceof RegisterUdp reg) {
        onRegisterUdp(pkt.sender(), reg);
      } else if (obj instanceof InputMessage input) {
        inputQueue.offer(input);
      } else {
        LOGGER.debug("UDP unrecognized type={} from {}",
          obj.getClass().getName(), pkt.sender());
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      LOGGER.warn("UDP handler error", cause);
    }
  }

  private void onConnectRequest(ChannelHandlerContext ctx, ConnectRequest req) {
    String playerName = req.playerName();
    if (!isValidPlayerName(playerName)) {
      sendTcpObject(ctx, new ConnectReject(
        "Invalid player name. Must be non-empty, without underscores, and"
          + " unique."));
      ctx.close();
      return;
    }
    int id = nextClientId.getAndIncrement();
    clientIdToName.put(id, playerName);
    tcpChannelToClientId.put(ctx.channel().id(), id);

    sendTcpObject(ctx, new ConnectAck(id));
    sendInitialLevel(ctx, id);
    LOGGER.info("Accepted client id={} name='{}' {}", id, playerName,
      ctx.channel());
  }

  private void onRequestEntitySpawn(ChannelHandlerContext ctx,
                                    RequestEntitySpawn req) {
    int entityId = req.entityId();
    var entity = Game.levelEntities()
      .filter(e -> e.id() == entityId)
      .findFirst();
    if (entity.isEmpty()) {
      LOGGER.warn("Entity id='{}' not found for spawn", entityId);
      return;
    }
    core.Entity e = entity.get();
    PositionComponent pc = e.fetch(PositionComponent.class).orElse(null);
    DrawComponent dc = e.fetch(DrawComponent.class).orElse(null);
    if (pc == null || dc == null) {
      LOGGER.warn("Entity id='{}' missing components for spawn", entityId);
      return;
    }
    EntitySpawnEvent spawn = new EntitySpawnEvent(
      e.id(),
      pc,
      dc);
    sendTcpObject(ctx, spawn);
  }

  private void onRegisterUdp(InetSocketAddress sender, RegisterUdp reg) {
    int id = reg.clientId();
    boolean known = tcpChannelToClientId.containsValue(id);
    if (!known) {
      LOGGER.warn("Ignoring RegisterUdp for unknown id={} from {}", id, sender);
      return;
    }
    InetSocketAddress previous = clientIdToUdp.put(id, sender);
    if (!Objects.equals(previous, sender)) {
      LOGGER.info("Registered/updated UDP for clientId={} addr={}", id, sender);
    }
  }

  private void sendInitialLevel(ChannelHandlerContext ctx, int clientId) {
    try {
      String levelName;
      try {
        levelName = DungeonLoader.currentLevel();
      } catch (Throwable t) {
        levelName = null;
      }
      LevelChangeEvent ev = new LevelChangeEvent(levelName, null);
      sendTcpObject(ctx, ev);
      LOGGER.info("Sent initial LEVEL_CHANGE to clientId={} level={}",
        clientId, levelName);
    } catch (Exception e) {
      LOGGER.warn("Failed sending initial LEVEL_CHANGE", e);
    }
  }

  CompletableFuture<Boolean> sendTcpObject(ChannelHandlerContext ctx, Object obj) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
          byte[] data = serialize(obj);
          if (data.length > MAX_TCP_OBJECT_SIZE) {
            LOGGER.warn("{} too large; not sending", obj.getClass().getSimpleName());
            result.complete(false);
            return result;
          }
          ByteBuf buf = ctx.alloc().buffer(4 + data.length);
          buf.writeInt(data.length);
          buf.writeBytes(data);
          ChannelFuture future = ctx.writeAndFlush(buf);
          future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
              LOGGER.warn("Failed to send TCP object {} to {}",
                obj.getClass().getSimpleName(), ctx.channel(), f.cause());
            }
            result.complete(f.isSuccess());
          });
        } catch (IOException e) {
          LOGGER.warn("Failed to send TCP object {}", obj.getClass().getSimpleName(), e);
          result.complete(false);
        }
        return result;
      }

  private boolean isValidPlayerName(String name) {
    return name != null && !name.isBlank() && !name.contains("_")
      && !clientIdToName.containsValue(name);
  }
}
