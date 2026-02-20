package core.network.server;

import static core.network.codec.NetworkCodec.deserialize;
import static core.network.codec.NetworkCodec.serialize;
import static core.network.config.NetworkConfig.*;

import contrib.entities.HeroController;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.MessageDispatcher;
import core.network.config.NetworkConfig;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.*;
import core.network.messages.s2c.*;
import core.utils.Tuple;
import core.utils.logging.DungeonLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The server-side transport layer handling TCP and UDP communication with clients.
 *
 * <p>Manages client sessions, message sending/receiving, and dispatching messages to handlers.
 *
 * @see Session Represents a client session on the server.
 * @see MessageDispatcher Dispatches incoming messages to registered handlers
 * @see ServerRuntime The main server runtime managing the transport and game loop.
 */
public final class ServerTransport {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerTransport.class);
  private static final short SERVER_PROTOCOL_VERSION = 1;

  private final Queue<Tuple<Session, NetworkMessage>> inboundQueue = new ConcurrentLinkedQueue<>();

  // Transport/session mappings
  private final ConcurrentHashMap<ChannelId, Session> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Short, Session> clientIdToSession = new ConcurrentHashMap<>();

  // UDP/TCP address and channel lookups
  private final ConcurrentHashMap<InetSocketAddress, Short> udpToClientId =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Short, String> clientIdToName = new ConcurrentHashMap<>();

  private final AtomicInteger nextClientId = new AtomicInteger(1);

  // Netty resources
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel tcpServer;
  private Channel udpChannel;

  /**
   * Starts the server transport on the specified port, initializing TCP and UDP channels.
   *
   * <p>If the transport is already started, this method logs a warning and returns without action.
   *
   * @param port The port to bind the server to for both TCP and UDP communication.
   */
  public void start(int port) {
    if (bossGroup != null || workerGroup != null) {
      LOGGER.warn("Server already started");
      return;
    }
    bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
    workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    setupDispatchers();
    startTcp(port);
    startUdp(port);
    LOGGER.info("ServerTransport started on port {} (TCP+UDP)", port);
  }

  /**
   * Stops the server transport, closing TCP and UDP channels and shutting down event loops.
   *
   * <p>Any exceptions during channel closure or event loop shutdown are logged as warnings.
   *
   * @see #start(int)
   */
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

  /**
   * Gets a copy of the current active sessions mapped by their channel IDs.
   *
   * <p>This map is unmodifiable and represents a snapshot of the sessions at the time of the call.
   * It contains all sessions, including those that may be closed.
   *
   * @return An unmodifiable map of channel IDs to sessions.
   * @see #connectedClients()
   */
  public Map<ChannelId, Session> sessions() {
    return Map.copyOf(sessions);
  }

  /**
   * Gets a set of all currently connected clients' states.
   *
   * <p>This method filters out any sessions that are closed and collects the associated ClientState
   * objects.
   *
   * @return A set of ClientState objects representing connected clients.
   */
  public Set<ClientState> connectedClients() {
    Set<ClientState> clients = new HashSet<>();
    for (Session s : sessions.values()) {
      if (s.isClosed()) continue;
      s.clientState().ifPresent(clients::add);
    }
    return clients;
  }

  /**
   * Gets a copy of the mapping from client IDs to their respective sessions.
   *
   * <p>This map is unmodifiable and represents a snapshot of the client ID to session mappings at
   * the time of the call.
   *
   * @return An unmodifiable map of client IDs to sessions.
   */
  public Map<Short, Session> clientIdToSessionMap() {
    return Map.copyOf(clientIdToSession);
  }

  /**
   * Gets the current TCP server channel.
   *
   * <p>This channel is used for accepting incoming TCP connections from clients.
   *
   * @return The TCP server channel.
   */
  Channel tcpServerChannel() {
    return tcpServer;
  }

  /**
   * Gets the current UDP channel.
   *
   * <p>This channel is used for sending and receiving UDP packets to/from clients.
   *
   * @return The UDP channel.
   */
  Channel udpChannel() {
    return udpChannel;
  }

  private CompletableFuture<Boolean> sendUdpObject(InetSocketAddress target, NetworkMessage msg) {
    if (udpChannel == null || !udpChannel.isActive()) {
      LOGGER.warn("UDP channel not active; cannot send to {}", target);
      return CompletableFuture.completedFuture(false);
    }
    try {
      byte[] data = serialize(msg);
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
      LOGGER.warn(
          "Failed to send TCP object to {}: {} ({})", ctx.channel(), e.getClass(), e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Broadcasts a {@link NetworkMessage} to all connected clients.
   *
   * @param msg The message to broadcast.
   * @param reliable True to send via a reliable channel (TCP), false for unreliable (UDP).
   * @return A CompletableFuture that completes with true if (reliable) all clients acknowledged the
   *     message, or false if any failed. For unreliable messages, the Future completes immediately
   *     with true. For any errors during sending, the Future completes with false.
   */
  public CompletableFuture<Boolean> broadcast(NetworkMessage msg, boolean reliable) {
    List<Session> activeSessions = sessions.values().stream().filter(s -> !s.isClosed()).toList();
    if (activeSessions.isEmpty()) {
      return CompletableFuture.completedFuture(true);
    }

    List<CompletableFuture<Boolean>> futures = new ArrayList<>();
    LOGGER.trace(
        "Broadcasting {} to {} clients via {}",
        msg.getClass().getSimpleName(),
        activeSessions.size(),
        reliable ? "TCP" : "UDP");

    for (Session s : activeSessions) {
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
      NetworkMessage msg = deserialize(frame);
      Session session = sessions.get(ctx.channel().id());
      if (session == null) {
        LOGGER.warn("Received TCP message for unknown session on channel {}", ctx.channel());
        return;
      }
      inboundQueue.offer(Tuple.of(session, msg));
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

        // Clean up UDP mapping
        InetSocketAddress udpAddr = session.udpAddress();
        if (udpAddr != null) {
          udpToClientId.remove(udpAddr);
        }

        // Remove Player Entity on disconnect
        session.clientState().flatMap(ClientState::playerEntity).ifPresent(Game::remove);

        LOGGER.info("TCP Session closed for {}", session);
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      if (cause instanceof SocketException && "Connection reset".equals(cause.getMessage())) {
        LOGGER.debug("TCP connection reset by peer: {}", ctx.channel());
      } else {
        LOGGER.warn("TCP handler error for {}", ctx.channel(), cause);
      }
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

      NetworkMessage msg;
      try {
        msg = deserialize(content);
      } catch (Exception e) {
        LOGGER.warn("Failed to deserialize UDP from {}", pkt.sender(), e);
        return;
      }

      InetSocketAddress sender = pkt.sender();
      Short mappedClientId = udpToClientId.get(sender);

      if (msg instanceof RegisterUdp reg) {
        Session tcpSender = clientIdToSession.get(reg.clientId());
        if (tcpSender == null) {
          LOGGER.warn("RegisterUdp for unknown clientId={} from {}", reg.clientId(), sender);
          return;
        }
        onUdpRegister(sender, tcpSender, reg);
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

      inboundQueue.offer(Tuple.of(session, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      LOGGER.warn("UDP handler error", cause);
    }
  }

  /**
   * Polls the inbound message queue and dispatches messages to their respective handlers.
   *
   * <p>This method should be called regularly on the game loop thread to ensure timely processing
   * of incoming messages.
   *
   * @see MessageDispatcher
   */
  public void pollAndDispatch() {
    Tuple<Session, NetworkMessage> msg;
    while ((msg = inboundQueue.poll()) != null) {
      try {
        Game.network().messageDispatcher().dispatch(msg.a(), msg.b());
      } catch (Exception e) {
        LOGGER.error("Dispatch error", e);
      }
    }
  }

  private void setupDispatchers() {
    MessageDispatcher dispatcher = Game.network().messageDispatcher();
    dispatcher.registerHandler(ConnectRequest.class, this::onConnectRequest);
    dispatcher.registerHandler(RequestEntitySpawn.class, this::onRequestEntitySpawn);
    dispatcher.registerHandler(InputMessage.class, this::onInputMessage);
    dispatcher.registerHandler(SoundFinishedMessage.class, this::onSoundFinished);
    dispatcher.registerHandler(DialogResponseMessage.class, this::onDialogResponse);
  }

  private void onSoundFinished(Session session, SoundFinishedMessage msg) {
    short clientId = session.clientState().map(ClientState::clientId).orElse((short) 0);
    SoundTracker tracker = SoundTracker.instance();

    if (!tracker.isTracked(msg.soundInstanceId())) {
      LOGGER.debug(
          "Ignoring SoundFinishedMessage for unknown sound {} from client {}",
          msg.soundInstanceId(),
          clientId);
      return;
    }

    if (!tracker.canReport(clientId, msg.soundInstanceId())) {
      LOGGER.warn(
          "Client {} not authorized to report sound {} finished", clientId, msg.soundInstanceId());
      return;
    }

    LOGGER.debug(
        "Received SoundFinishedMessage from client {}: instanceId={}",
        clientId,
        msg.soundInstanceId());
    Game.audio().notifySoundFinished(msg.soundInstanceId());
  }

  private void onConnectRequest(Session session, ConnectRequest req) {
    LOGGER.info(
        "Received ConnectRequest protocolVersion={} username='{}' sessionId={} from {}",
        req.protocolVersion(),
        req.playerName(),
        req.sessionId(),
        session);
    if (req.protocolVersion() != SERVER_PROTOCOL_VERSION) {
      session.sendMessage(new ConnectReject(ConnectReject.Reason.INCOMPATIBLE_VERSION), true);
      LOGGER.info(
          "Rejected ConnectRequest due to incompatible version: server={} client={}",
          SERVER_PROTOCOL_VERSION,
          req.protocolVersion());
      return;
    }

    String playerName = req.playerName();
    if (!isValidPlayerName(playerName)) {
      session.sendMessage(new ConnectReject(ConnectReject.Reason.INVALID_NAME), true);
      LOGGER.info("Rejected ConnectRequest due to invalid player name: '{}'", playerName);
      return;
    }

    if (req.sessionToken() != null && req.sessionToken().length > 0) {
      handleRestoringSession(session, req);
      return;
    }

    short newClientId = (short) nextClientId.getAndIncrement();
    clientIdToName.put(newClientId, playerName);

    byte[] sessionToken = SessionTokenUtil.generate(NetworkConfig.SESSION_TOKEN_LENGTH_BYTES);

    session.attachClientState(
        new ClientState(newClientId, playerName, ServerRuntime.SESSION_ID, sessionToken));
    clientIdToSession.put(newClientId, session);

    session.sendMessage(new ConnectAck(newClientId, ServerRuntime.SESSION_ID, sessionToken), true);

    sendInitialLevel(session.tcpCtx(), newClientId);

    // Resync dialogs for the new client
    DialogTracker.instance().resyncDialogsToClient(newClientId);
    SoundTracker.instance().resyncSoundsToClient(newClientId);

    LOGGER.info("Accepted client id={} name='{}' {}", newClientId, playerName, session);
  }

  private void handleRestoringSession(Session session, ConnectRequest req) {
    // 1. Validate session ID
    if (req.sessionId() != ServerRuntime.SESSION_ID) {
      LOGGER.info(
          "No session found for restore attempt with sessionId={} from {}",
          req.sessionId(),
          session);
      session.sendMessage(new ConnectReject(ConnectReject.Reason.NO_SESSION_FOUND), true);
      return;
    }

    // 2. Validate session token and find matching client ID
    short clientId = -1;
    Session oldSession = null;
    for (Map.Entry<Short, Session> entry : clientIdToSession.entrySet()) {
      Session s = entry.getValue();
      ClientState state = s.clientState().orElse(null);
      if (state != null
          && state.sessionToken() != null
          && SessionTokenUtil.validate(state.sessionToken(), req.sessionToken())) {
        clientId = entry.getKey();
        oldSession = s;
        // check if the session is already active
        if (!s.isClosed()) {
          LOGGER.warn("Session restore attempt for already active clientId={}", clientId);
          session.sendMessage(new ConnectReject(ConnectReject.Reason.INVALID_SESSION_TOKEN), true);
          return;
        }
        // check if name is the same
        if (!state.username().equals(req.playerName())) {
          LOGGER.warn(
              "Session restore attempt with mismatched player name: expected='{}' provided='{}'",
              state.username(),
              req.playerName());
          session.sendMessage(new ConnectReject(ConnectReject.Reason.INVALID_SESSION_TOKEN), true);
          return;
        }
        break;
      }
    }

    if (clientId == -1) {
      LOGGER.info("No matching session found for restore attempt");
      session.sendMessage(new ConnectReject(ConnectReject.Reason.INVALID_SESSION_TOKEN), true);
      return;
    }

    // 3. Restore session

    String playerName = req.playerName();
    byte[] newSessionToken = SessionTokenUtil.generate(NetworkConfig.SESSION_TOKEN_LENGTH_BYTES);

    // reattach old ClientState to new Session
    ClientState oldClientState = oldSession.clientState().orElseThrow();
    oldClientState.resetForReconnect(ServerRuntime.SESSION_ID, newSessionToken, true);
    session.attachClientState(oldClientState);
    clientIdToSession.put(clientId, session);

    // remove old mappings
    clientIdToName.put(clientId, playerName);
    sessions.remove(oldSession.tcpCtx().channel().id());
    try {
      oldSession.close(); // should be already closed, but just in case
    } catch (Exception ignored) {
    }

    // 4. Send ConnectAck
    session.sendMessage(new ConnectAck(clientId, ServerRuntime.SESSION_ID, newSessionToken), true);

    sendInitialLevel(session.tcpCtx(), clientId);

    // Resync dialogs for the reconnecting client
    DialogTracker.instance().resyncDialogsToClient(clientId);
    SoundTracker.instance().resyncSoundsToClient(clientId);

    LOGGER.info("Restored client id={} name='{}' {}", clientId, playerName, session);
  }

  private void onUdpRegister(InetSocketAddress sender, Session tcpSession, RegisterUdp reg) {
    // 1. Validate session ID
    if (reg.sessionId() != ServerRuntime.SESSION_ID) {
      LOGGER.warn("RegisterUdp with invalid sessionId={} from {}", reg.sessionId(), sender);
      tcpSession.sendMessage(new RegisterAck(false), true);
      return;
    }

    // 2. Validate client ID
    if (reg.clientId() <= 0) {
      LOGGER.warn("RegisterUdp with invalid clientId={} from {}", reg.clientId(), sender);
      tcpSession.sendMessage(new RegisterAck(false), true);
      return;
    }

    // 3. Lookup session
    Session sess = clientIdToSession.get(reg.clientId());
    if (sess == null) {
      LOGGER.warn("RegisterUdp for unknown clientId={} from {}", reg.clientId(), sender);
      tcpSession.sendMessage(new RegisterAck(false), true);
      return;
    }

    ClientState state = sess.clientState().orElse(null);
    if (state == null) {
      LOGGER.error(
          "Session for clientId={} is missing ClientState. This should not happen.",
          reg.clientId());
      tcpSession.sendMessage(new RegisterAck(false), true);
      return;
    }

    // 4. Validate Session Token
    if (!Arrays.equals(state.sessionToken(), reg.sessionToken())) {
      LOGGER.warn(
          "RegisterUdp with invalid session token for clientId={} from {}", reg.clientId(), sender);
      tcpSession.sendMessage(new RegisterAck(false), true);
      return;
    }

    // 5. Handle registration/re-registration
    InetSocketAddress currentUdpAddress = sess.udpAddress();
    if (currentUdpAddress != null && !currentUdpAddress.equals(sender)) {
      // Address has changed, update the mappings
      LOGGER.info(
          "Updating UDP address for clientId={}: {} -> {}",
          reg.clientId(),
          currentUdpAddress,
          sender);
      udpToClientId.remove(currentUdpAddress);
    } else if (currentUdpAddress == null) {
      LOGGER.info("Associated UDP {} -> clientId={}", sender, reg.clientId());
    }

    sess.udpAddress(sender);
    udpToClientId.put(sender, reg.clientId());
    tcpSession.sendMessage(new RegisterAck(true), true);
  }

  /**
   * Validates a proposed player name.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>We allow reusing names of disconnected players.
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
    // if we already have this name, check if the session is closed
    Short clientId =
        clientIdToName.entrySet().stream()
            .filter(e -> e.getValue().equals(name))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    if (clientId != null) {
      // name is taken, check if session is closed
      Session sess = clientIdToSession.get(clientId);
      return sess != null
          && sess.isClosed(); // previously had to validate, so we can skip other checks
    }

    return name != null
        && !name.isBlank()
        && !name.contains("_")
        && !clientIdToName.containsValue(name);
  }

  private void onRequestEntitySpawn(Session session, RequestEntitySpawn req) {
    int entityId = req.entityId();
    Optional<Entity> optEntity = Game.levelEntities().filter(e -> e.id() == entityId).findFirst();
    if (optEntity.isEmpty()) {
      LOGGER.warn("Entity id='{}' not found for spawn", entityId);
      return;
    }
    Entity entity = optEntity.get();
    PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
    DrawComponent dc = entity.fetch(DrawComponent.class).orElse(null);
    if (pc == null || dc == null) {
      LOGGER.warn(
          "Entity id='{}' missing components for spawn (entity was: '{}')",
          entityId,
          entity.name());
      return;
    }
    session.sendMessage(new EntitySpawnEvent(entity), true);
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
    HeroController.enqueueInput(state, msg);
  }

  /**
   * Handles a dialog response from a client.
   *
   * <p>Validates the response, executes the appropriate callback, and closes the dialog on all
   * clients.
   *
   * @param session the session of the responding client
   * @param msg the dialog response message
   */
  private void onDialogResponse(Session session, DialogResponseMessage msg) {
    // 1. Validate session
    if (!isSessionValid(session)) {
      LOGGER.warn("Ignoring DialogResponseMessage from invalid session: {}", session);
      return;
    }

    short clientId = session.clientState().map(ClientState::clientId).orElse((short) 0);
    String dialogId = msg.dialogId();

    // 2. Validate client authorization
    DialogTracker tracker = DialogTracker.instance();
    if (!tracker.canRespond(clientId, dialogId)) {
      LOGGER.warn("Client {} attempted to respond to unauthorized dialog: {}", clientId, dialogId);
      return;
    }

    // 3. Try to claim (first-responder wins)
    if (!tracker.tryClaimDialog(dialogId, clientId)) {
      LOGGER.debug("Dialog {} already claimed by another client, ignoring response", dialogId);
      return;
    }

    // 4. Execute callback by key from DialogTracker
    Optional<Consumer<DialogResponseMessage.Payload>> callbackOpt =
        DialogTracker.instance().getCallback(dialogId, msg.callbackKey());
    if (callbackOpt.isPresent()) {
      Consumer<DialogResponseMessage.Payload> callback = callbackOpt.get();
      try {
        callback.accept(msg.payload());
      } catch (Exception e) {
        LOGGER.error("Error executing callback for dialog {}", dialogId, e);
      }
    } else {
      LOGGER.debug("No callback found for dialog {} with key {}", dialogId, msg.callbackKey());
    }
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
