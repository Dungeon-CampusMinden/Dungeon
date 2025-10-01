package core.network.server;

import static core.network.codec.NetworkCodec.deserialize;
import static core.network.codec.NetworkCodec.serialize;
import static core.network.config.NetworkConfig.*;

import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.MessageDispatcher;
import core.network.config.NetworkConfig;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.s2c.*;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTransport.class);
  private static final short SERVER_PROTOCOL_VERSION = 1;
  private static final Random RANDOM = new Random();

  private final ConcurrentLinkedQueue<InputMessage> inputQueue = new ConcurrentLinkedQueue<>();

  // Transport/session mappings
  private final ConcurrentHashMap<ChannelId, Session> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, Session> clientIdToSession = new ConcurrentHashMap<>();

  // UDP/TCP address and channel lookups
  private final ConcurrentHashMap<Integer, InetSocketAddress> clientIdToUdp =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<InetSocketAddress, Integer> udpToClientId =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ChannelId, Integer> tcpChannelToClientId =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ChannelId, ChannelHandlerContext> tcpChannels =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, String> clientIdToName = new ConcurrentHashMap<>();

  private final AtomicInteger nextClientId = new AtomicInteger(1);
  private final int SESSION_ID = RANDOM.nextInt();

  // Netty resources and dispatcher
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel tcpServer;
  private Channel udpChannel;
  private MessageDispatcher dispatcher;

  public void start(int port) {
    if (bossGroup != null || workerGroup != null) {
      LOGGER.warn("Server already started");
      return;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    setupDispatchers();
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

  public Map<ChannelId, Session> sessions() {
    return Map.copyOf(sessions);
  }

  public Set<ClientState> connectedClients() {
    Set<ClientState> clients = new HashSet<>();
    for (Session s : sessions.values()) {
      s.clientState().ifPresent(clients::add);
    }
    return clients;
  }

  public Map<Integer, Session> clientIdToSessionMap() {
    return Map.copyOf(clientIdToSession);
  }

  public int nextClientIdValue() {
    return nextClientId.get();
  }

  public int serverSessionId() {
    return SESSION_ID;
  }

  public MessageDispatcher dispatcher() {
    return dispatcher;
  }

  public Channel tcpServerChannel() {
    return tcpServer;
  }

  public Channel udpChannel() {
    return udpChannel;
  }

  private CompletableFuture<Boolean> sendUdpObject(InetSocketAddress target, Object obj) {
    if (udpChannel == null || !udpChannel.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(obj);
      if (data.length > SAFE_UDP_MTU) {
        LOGGER.warn("Skip UDP send; payload too large ({} B) to {}", data.length, target);
        return CompletableFuture.completedFuture(false);
      }
      udpChannel
          .writeAndFlush(
              new DatagramPacket(udpChannel.alloc().buffer(data.length).writeBytes(data), target))
          .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      return CompletableFuture.completedFuture(true);
    } catch (Exception e) {
      LOGGER.warn("Failed to send UDP object to {}", target, e);
      return CompletableFuture.completedFuture(false);
    }
  }

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

  public CompletableFuture<Boolean> broadcast(Object obj, boolean reliable) {
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();
    LOGGER.debug(
        "Broadcasting {} to {} clients via {}",
        obj.getClass().getSimpleName(),
        reliable ? sessions.size() : clientIdToUdp.size(),
        reliable ? "TCP" : "UDP");

    if (reliable) {
      for (Session s : List.copyOf(sessions.values())) {
        ChannelHandlerContext ctx = s.tcpCtx();
        if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
          futures.add(sendTcpObject(ctx, obj));
        } else {
          futures.add(CompletableFuture.completedFuture(false));
          LOGGER.warn(
              "Skipping TCP broadcast to inactive/missing channel for clientId={}", s.clientId());
        }
      }
    } else {
      for (Session s : List.copyOf(clientIdToSession.values())) {
        InetSocketAddress addr = clientIdToUdp.get(s.clientId());
        if (addr != null) {
          futures.add(sendUdpObject(addr, obj));
        } else {
          futures.add(CompletableFuture.completedFuture(false));
          LOGGER.warn(
              "Skipping UDP broadcast to clientId={} with no registered UDP address", s.clientId());
        }
      }
    }

    if (futures.isEmpty()) return CompletableFuture.completedFuture(true);
    CompletableFuture<Void> all =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    return all.thenApply(v -> futures.stream().allMatch(CompletableFuture::join));
  }

  // ---- internals ----

  private void startTcp(int port) {
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
    tcpServer = sb.bind(port).syncUninterruptibly().channel();
  }

  private void startUdp(int port) {
    Bootstrap ub = new Bootstrap();
    ub.group(workerGroup).channel(NioDatagramChannel.class).handler(new UdpServerHandler());
    udpChannel = ub.bind(port).syncUninterruptibly().channel();
  }

  private final class TcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      Function<Object, CompletableFuture<Boolean>> tcpSenderForSession =
          obj -> sendTcpObject(ctx, obj);
      Session session = new Session(ctx, ServerTransport.this::sendUdpObject, tcpSenderForSession);
      sessions.put(ctx.channel().id(), session);
      tcpChannels.put(ctx.channel().id(), ctx);
      super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) throws Exception {
      Object obj = deserialize(frame);
      if (obj instanceof NetworkMessage msg) {
        if (dispatcher != null) dispatcher.dispatch(ctx, msg);
      } else {
        LOGGER.debug("TCP received unexpected object: {}", obj.getClass().getName());
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      Integer id = tcpChannelToClientId.remove(ctx.channel().id());
      tcpChannels.remove(ctx.channel().id());

      Session sess = sessions.remove(ctx.channel().id());
      if (sess != null) {
        try {
          sess.close();
        } catch (Exception ignored) {
        }
        int sid = sess.clientId();
        if (sid != 0) {
          clientIdToSession.remove(sid);
          InetSocketAddress prev = clientIdToUdp.remove(sid);
          if (prev != null) udpToClientId.remove(prev);
          clientIdToName.remove(sid);
          LOGGER.info("Client disconnected id={} {}", sid, ctx.channel());
        } else {
          LOGGER.info("TCP channel closed (no assigned clientId) {}", ctx.channel());
        }
      } else if (id != null) {
        InetSocketAddress prev = clientIdToUdp.remove(id);
        if (prev != null) udpToClientId.remove(prev);
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

  private final class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
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

      switch (obj) {
        case RegisterUdp reg -> onRegisterUdp(pkt.sender(), reg);
        case InputMessage input -> onInputMessage(pkt.sender(), input);
        default ->
            LOGGER.debug(
                "UDP unrecognized type={} from {}", obj.getClass().getName(), pkt.sender());
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      LOGGER.warn("UDP handler error", cause);
    }
  }

  private void setupDispatchers() {
    dispatcher = Game.network().messageDispatcher();
    if (dispatcher == null)
      throw new IllegalStateException("Game.network().messageDispatcher() is null");

    dispatcher.registerHandler(ConnectRequest.class, this::onConnectRequest);
    dispatcher.registerHandler(RequestEntitySpawn.class, this::onRequestEntitySpawn);
    dispatcher.registerHandler(InputMessage.class, this::onInputMessage);
  }

  private void onConnectRequest(ChannelHandlerContext ctx, ConnectRequest req) {
    if (req.protocolVersion() != SERVER_PROTOCOL_VERSION) {
      sendTcpObject(
          ctx,
          new ConnectReject(
              "Protocol version mismatch. Server="
                  + SERVER_PROTOCOL_VERSION
                  + ", yours="
                  + req.protocolVersion()));
      ctx.close();
      return;
    }

    String playerName = req.playerName();
    if (!isValidPlayerName(playerName)) {
      sendTcpObject(
          ctx,
          new ConnectReject(
              "Invalid player name. Must be non-empty, without underscores, and unique."));
      ctx.close();
      return;
    }

    if (req.sessionId() != 0 || (req.sessionToken() != null && req.sessionToken().length > 0)) {
      // TODO: implement session reconnection
      LOGGER.info(
          "Session reconnection not implemented yet, ignoring session data. Got: id={} tokenLength={}",
          req.sessionId(),
          req.sessionToken() != null ? req.sessionToken().length : 0);
    }

    int id = nextClientId.getAndIncrement();
    clientIdToName.put(id, playerName);
    tcpChannelToClientId.put(ctx.channel().id(), id);

    Session session = sessions.get(ctx.channel().id());
    long clientSessionId = RANDOM.nextLong();
    byte[] sessionToken = SessionTokenUtil.generate(NetworkConfig.SESSION_TOKEN_LENGTH_BYTES);

    if (session != null) {
      session.attachClientState(new ClientState(id, playerName, clientSessionId, sessionToken));
      clientIdToSession.put(id, session);
    } else {
      LOGGER.warn("Accepted client id={} but no Session found for channel {}", id, ctx.channel());
    }

    sendTcpObject(ctx, new ConnectAck(id));
    sendInitialLevel(ctx, id);
    LOGGER.info("Accepted client id={} name='{}' {}", id, playerName, ctx.channel());
  }

  private void onRequestEntitySpawn(ChannelHandlerContext ctx, RequestEntitySpawn req) {
    int entityId = req.entityId();
    var entity = Game.levelEntities().filter(e -> e.id() == entityId).findFirst();
    if (entity.isEmpty()) {
      LOGGER.warn("Entity id='{}' not found for spawn", entityId);
      return;
    }
    core.Entity e = entity.get();
    PositionComponent pc = e.fetch(PositionComponent.class).orElse(null);
    DrawComponent dc = e.fetch(DrawComponent.class).orElse(null);
    if (pc == null || dc == null) {
      LOGGER.warn(
          "Entity id='{}' missing components for spawn (entity was: '{}')", entityId, e.name());
      return;
    }
    EntitySpawnEvent spawn = new EntitySpawnEvent(e.id(), pc, dc, e.isPersistent());
    sendTcpObject(ctx, spawn);
  }

  private void onInputMessage(ChannelHandlerContext ctx, InputMessage msg) {
    Integer clientId = tcpChannelToClientId.get(ctx.channel().id());
    if (clientId == null) {
      Session s = sessions.get(ctx.channel().id());
      if (s != null) clientId = s.clientId();
    }
    if (clientId == null) {
      LOGGER.warn("Received TCP InputMessage from unknown channel {}", ctx.channel());
      return;
    }
    inputQueue.offer(
        new InputMessage(msg.clientTick(), msg.sequence(), msg.action(), msg.point(), clientId));
  }

  private void onInputMessage(InetSocketAddress sender, InputMessage msg) {
    Integer clientId = udpToClientId.get(sender);
    if (clientId == null) {
      // Fallback: search in clientIdToUdp map
      for (Map.Entry<Integer, InetSocketAddress> e : clientIdToUdp.entrySet()) {
        if (Objects.equals(e.getValue(), sender)) {
          clientId = e.getKey();
          udpToClientId.put(sender, clientId);
          break;
        }
      }
    }
    if (clientId == null) {
      LOGGER.warn("Received UDP InputMessage from unknown address {}", sender);
      return;
    }
    inputQueue.offer(
        new InputMessage(msg.clientTick(), msg.sequence(), msg.action(), msg.point(), clientId));
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
      if (previous != null) udpToClientId.remove(previous);
      udpToClientId.put(sender, id);
      LOGGER.info("Registered/updated UDP for clientId={} addr={}", id, sender);
      Session s = clientIdToSession.get(id);
      if (s != null) s.udpAddress(sender);
    }
    sendUdpObject(sender, new RegisterAck(true));
  }

  private void sendInitialLevel(ChannelHandlerContext ctx, int clientId) {
    try {
      LevelChangeEvent ev = LevelChangeEvent.currentLevel();
      sendTcpObject(ctx, ev);
      LOGGER.info("Sent initial LEVEL_CHANGE to clientId={}", clientId);
    } catch (Exception e) {
      LOGGER.warn("Failed sending initial LEVEL_CHANGE", e);
    }
  }

  /**
   * Validates a proposed player name.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>Not null
   *   <li>Not blank
   *   <li>No underscores
   *   <li>Unique among connected players
   * </ul>
   *
   * @param name The player name to validate.
   * @return True if valid, false otherwise.
   */
  private boolean isValidPlayerName(String name) {
    return name != null
        && !name.isBlank()
        && !name.contains("_")
        && !clientIdToName.containsValue(name);
  }
}
