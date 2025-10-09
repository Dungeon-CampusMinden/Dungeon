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
import core.utils.Tuple;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTransport.class);
  private static final short SERVER_PROTOCOL_VERSION = 1;

  private final ConcurrentLinkedQueue<Tuple<ClientState, InputMessage>> inputQueue =
      new ConcurrentLinkedQueue<>();

  // Transport/session mappings
  private final ConcurrentHashMap<ChannelId, Session> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Short, Session> clientIdToSession = new ConcurrentHashMap<>();

  // UDP/TCP address and channel lookups
  private final ConcurrentHashMap<InetSocketAddress, Short> udpToClientId =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Short, String> clientIdToName = new ConcurrentHashMap<>();

  private final AtomicInteger nextClientId = new AtomicInteger(1);

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

  public ConcurrentLinkedQueue<Tuple<ClientState, InputMessage>> inputQueue() {
    return inputQueue;
  }

  public Map<ChannelId, Session> sessions() {
    return Map.copyOf(sessions);
  }

  public Set<ClientState> connectedClients() {
    Set<ClientState> clients = new HashSet<>();
    for (Session s : sessions.values()) {
      if (s.isClosed()) continue;
      s.clientState().ifPresent(clients::add);
    }
    return clients;
  }

  public Map<Short, Session> clientIdToSessionMap() {
    return Map.copyOf(clientIdToSession);
  }

  public int nextClientIdValue() {
    return nextClientId.get();
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

  public CompletableFuture<Boolean> broadcast(NetworkMessage msg, boolean reliable) {
    if (sessions.isEmpty()) {
      return CompletableFuture.completedFuture(true);
    }

    List<CompletableFuture<Boolean>> futures = new ArrayList<>();
    LOGGER.debug(
        "Broadcasting {} to {} clients via {}",
        msg.getClass().getSimpleName(),
        sessions.size(),
        reliable ? "TCP" : "UDP");

    for (Session s : List.copyOf(sessions.values())) {
      futures.add(s.sendMessage(msg, reliable));
    }

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
      LOGGER.trace("New TCP channel active: {}", ctx.channel());
      Session session =
          new Session(
              ctx, ServerTransport.this::sendUdpObject, ServerTransport.this::sendTcpObject);
      sessions.put(ctx.channel().id(), session);
      super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) throws Exception {
      LOGGER.trace("TCP received {} bytes from {}", frame.readableBytes(), ctx.channel());
      Object obj = deserialize(frame);
      Session session = sessions.get(ctx.channel().id());
      if (session == null) {
        LOGGER.warn("Received TCP message for unknown session on channel {}", ctx.channel());
        return;
      }
      if (obj instanceof NetworkMessage msg) {
        if (dispatcher != null) dispatcher.dispatch(session, msg);
      } else {
        LOGGER.debug("TCP received unexpected object: {}", obj.getClass().getName());
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      LOGGER.trace("TCP channel inactive: {}", ctx.channel());
      Session session = sessions.get(ctx.channel().id());

      if (session != null) {
        try {
          session.close();
        } catch (Exception ignored) {
        }
        if (session.clientId() != 0) {
          clientIdToSession.remove(session.clientId());
          clientIdToName.remove(session.clientId());
          LOGGER.info("Client disconnected id={} {}", session.clientId(), ctx.channel());
        } else {
          LOGGER.info("TCP channel closed (no assigned clientId) {}", ctx.channel());
        }
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
      LOGGER.trace("UDP received {} bytes from {}", pkt.content().readableBytes(), pkt.sender());
      ByteBuf content = pkt.content();
      int size = content.readableBytes();
      if (size <= 0 || size > NetworkConfig.MAX_UDP_OBJECT_SIZE) {
        LOGGER.warn("Ignoring UDP packet with invalid size={} from {}", size, pkt.sender());
        return;
      }

      Object obj;
      try {
        obj = deserialize(content);
      } catch (Exception e) {
        LOGGER.warn("Failed to deserialize UDP from {}", pkt.sender(), e);
        return;
      }

      InetSocketAddress sender = pkt.sender();
      Short mappedClientId = udpToClientId.get(sender);

      if (obj instanceof RegisterUdp(short clientId)) {
        Session sess = clientIdToSession.get(clientId);
        if (sess == null) {
          LOGGER.warn("RegisterUdp for unknown clientId={} from {}", clientId, sender);
          sendUdpObject(sender, new RegisterAck(false));
          return;
        }

        sess.udpAddress(sender);
        udpToClientId.put(sender, clientId);
        LOGGER.info("Associated UDP {} -> clientId={}", sender, clientId);
        sendUdpObject(sender, new RegisterAck(true));
        return;
      }

      if (mappedClientId == null) {
        LOGGER.warn("UDP from unregistered addr {} (no registration)", sender);
        return;
      }

      Session session = clientIdToSession.get(mappedClientId);
      if (session == null) {
        udpToClientId.remove(sender);
        LOGGER.warn("Stale UDP mapping for {} removed", sender);
        return;
      }

      if (obj instanceof NetworkMessage msg) {
        // Enqueue dispatch to Game-Thread instead of handling heavy logic here
        dispatcher.dispatch(session, msg);
      } else {
        LOGGER.debug("Unexpected UDP object {} from {}", obj.getClass().getName(), sender);
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

  private void onConnectRequest(Session session, ConnectRequest req) {
    if (req.protocolVersion() != SERVER_PROTOCOL_VERSION) {
      session.sendMessage(
          new ConnectReject(
              "Protocol version mismatch. Server="
                  + SERVER_PROTOCOL_VERSION
                  + ", yours="
                  + req.protocolVersion()),
          true);
      session.close();
      return;
    }

    String playerName = req.playerName();
    if (!isValidPlayerName(playerName)) {
      session.sendMessage(
          new ConnectReject(
              "Invalid player name. Must be non-empty, without underscores, and unique."),
          true);
      session.close();
      return;
    }

    if (req.sessionToken() != null && req.sessionToken().length > 0) {
      // TODO: implement session reconnection
      LOGGER.info(
          "Session reconnection not implemented yet, ignoring session data. Got: id={} tokenLength={}",
          req.sessionId(),
          req.sessionToken().length);
    }

    short newClientId = (short) nextClientId.getAndIncrement();
    clientIdToName.put(newClientId, playerName);

    byte[] sessionToken = SessionTokenUtil.generate(NetworkConfig.SESSION_TOKEN_LENGTH_BYTES);

    session.attachClientState(
        new ClientState(newClientId, playerName, ServerRuntime.SESSION_ID, sessionToken));
    clientIdToSession.put(newClientId, session);

    session.sendMessage(new ConnectAck(newClientId, ServerRuntime.SESSION_ID, sessionToken), true);

    sendInitialLevel(session.tcpCtx(), newClientId);
    LOGGER.info("Accepted client id={} name='{}' {}", newClientId, playerName, session);
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

  private void onRequestEntitySpawn(Session session, RequestEntitySpawn req) {
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
    session.sendMessage(new EntitySpawnEvent(e.id(), pc, dc, e.isPersistent()), true);
  }

  private void onInputMessage(Session session, InputMessage msg) {
    // 1. Validate session
    if (!isSessionValid(session)) {
      LOGGER.warn("Ignoring InputMessage from invalid session: {}", session);
      return;
    }

    // 2. Validate sequence
    ClientState state = session.clientState().orElseThrow();
    if (!state.isSeqPlausible(msg.sequence())) {
      LOGGER.debug(
          "Ignoring InputMessage with implausible sequence={} from clientId={}",
          msg.sequence(),
          state.clientId());
      state.updateLastActivity(); // Still update activity timestamp
      return;
    }

    // 3. Validate Session ID
    if (msg.sessionId() != ServerRuntime.SESSION_ID) {
      LOGGER.debug(
          "Ignoring InputMessage with invalid sessionId={} from clientId={}",
          msg.sessionId(),
          state.clientId());
      return;
    }

    // 4. Enqueue
    inputQueue.offer(Tuple.of(state, msg));
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

  private boolean isSessionValid(Session session) {
    if (session == null) {
      LOGGER.trace("Session is null");
      return false;
    }
    short clientId = session.clientId();
    if (clientId == 0) {
      LOGGER.trace("Session has no assigned clientId: {}", session);
      return false;
    }
    if (!sessions.containsKey(session.tcpCtx().channel().id())) {
      LOGGER.trace("Session not found in sessions map: {}", session);
      return false;
    }
    if (session.clientState().isEmpty()) {
      LOGGER.trace("Session has no attached ClientState: {}", session);
      return false;
    }
    return true;
  }
}
