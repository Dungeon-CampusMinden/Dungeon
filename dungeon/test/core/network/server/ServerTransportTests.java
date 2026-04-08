package core.network.server;

import static org.junit.jupiter.api.Assertions.*;

import contrib.entities.CharacterClass;
import core.Game;
import core.game.PreRunConfiguration;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.RegisterUdp;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/**
 * Unit tests for {@link ServerTransport}.
 *
 * <p>Validates Netty-based server transport initialization, channel lifecycle, session management,
 * message broadcasting, and input queue handling.
 */
public class ServerTransportTests {

  private static final int TEST_PORT = 17777;
  private static int portCounter = 0;
  private static final List<ServerTransport> transports = new ArrayList<>();
  private static final ThreadLocal<ServerTransport> currentTransport = new ThreadLocal<>();

  /**
   * Generates a unique port starting from the base TEST_PORT. Each call increments the port number
   * to ensure no conflicts between concurrent or sequential tests.
   *
   * @return a unique port number for testing
   */
  private static synchronized int uniquePort() {
    return TEST_PORT + (portCounter++);
  }

  /**
   * Stops all transports in the transports list and clears the list. This can be called to perform
   * batch cleanup of all created transports across all tests.
   */
  private static synchronized void stopAllTransports() {
    for (ServerTransport transport : transports) {
      if (transport != null) {
        try {
          transport.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    transports.clear();
  }

  /**
   * Sets up the server transport for testing by initializing {@link Game} with multiplayer server
   * configuration.
   *
   * <p>Configures the game to run in server mode and initializes a fresh {@link ServerTransport}
   * instance.
   */
  @BeforeEach
  public void setup() {
    ServerTransport transport = new ServerTransport();
    transports.add(transport);
    currentTransport.set(transport);
    PreRunConfiguration.multiplayerCharacterClasses(CharacterClass.WIZARD);
    MockNetworkHandler.useLocalNetworkHandler();
  }

  /**
   * Cleans up server transport resources and game state after each test.
   *
   * <p>Stops the transport, resets multiplayer configuration, and exits the game to ensure
   * isolation between tests.
   */
  @AfterEach
  public void cleanup() {
    ServerTransport transport = currentTransport.get();
    if (transport != null) {
      transport.stop();
      transports.remove(transport);
      currentTransport.remove();
    }
    PreRunConfiguration.multiplayerCharacterClasses(CharacterClass.WIZARD);
  }

  /**
   * Validates that the transport starts successfully on the configured test port and creates both
   * TCP and UDP channels.
   */
  @Test
  public void test_transportStartsOnConfiguredPort() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertNotNull(transport.tcpServerChannel());
    assertNotNull(transport.udpChannel());
  }

  /** Validates that both TCP and UDP channels are active after the transport starts. */
  @Test
  public void test_transportChannelsActive() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();

    transport.start(port);
    assertTrue(transport.tcpServerChannel().isActive());
    assertTrue(transport.udpChannel().isActive());
  }

  /** Validates that both TCP and UDP channels are properly closed when the transport stops. */
  @Test
  public void test_stopClosesChannels() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.tcpServerChannel().isActive());
    assertTrue(transport.udpChannel().isActive());

    transport.stop();
    assertFalse(transport.tcpServerChannel().isActive());
    assertFalse(transport.udpChannel().isActive());
  }

  /** Validates that the session map is empty after transport initialization. */
  @Test
  public void test_sessionsMapEmpty() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.sessions().isEmpty());
  }

  /** Validates that the client ID to session mapping is empty after transport initialization. */
  @Test
  public void test_clientIdMappingEmpty() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.clientIdToSessionMap().isEmpty());
  }

  /**
   * Validates that broadcasting a message to an empty session map returns a completed {@link
   * CompletableFuture}.
   */
  @Test
  public void test_broadcastEmptySessionMap() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = transport.broadcast(msg, true);
    assertNotNull(result);
    assertTrue(result.isDone());
  }

  /**
   * Validates that the transport can be started and stopped multiple times without error,
   * demonstrating idempotent lifecycle management.
   */
  @Test
  public void test_startStopIdempotent() {
    ServerTransport transport = currentTransport.get();
    int port1 = uniquePort();

    transport.start(port1);
    Channel tcpChannel1 = transport.tcpServerChannel();
    Channel udpChannel1 = transport.udpChannel();
    assertTrue(tcpChannel1.isActive());
    assertTrue(udpChannel1.isActive());

    transport.stop();
    assertFalse(tcpChannel1.isActive());
    assertFalse(udpChannel1.isActive());

    int port2 = uniquePort();
    transport.start(port2);
    Channel tcpChannel2 = transport.tcpServerChannel();
    Channel udpChannel2 = transport.udpChannel();
    assertNotEquals(tcpChannel1, tcpChannel2);
    assertNotEquals(udpChannel1, udpChannel2);
    assertTrue(tcpChannel2.isActive());
    assertTrue(udpChannel2.isActive());
  }

  /** Validates that calling start() multiple times without stop() is idempotent. */
  @Test
  public void test_doubleStartIsIdempotent() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();

    transport.start(port);
    Channel tcpChannel1 = transport.tcpServerChannel();
    Channel udpChannel1 = transport.udpChannel();

    transport.start(port);
    Channel tcpChannel2 = transport.tcpServerChannel();
    Channel udpChannel2 = transport.udpChannel();

    assertSame(tcpChannel1, tcpChannel2);
    assertSame(udpChannel1, udpChannel2);
  }

  /** Validates that calling stop() before start() does not cause errors. */
  @Test
  public void test_stopWithoutStartIsIdempotent() {
    ServerTransport transport = currentTransport.get();
    transport.stop();
    transport.stop();
  }

  /** Validates that multiple transport instances can run concurrently on different ports. */
  @Test
  public void test_multipleTransportsCanCoexist() {
    ServerTransport transport1 = new ServerTransport();
    ServerTransport transport2 = new ServerTransport();
    transports.add(transport1);
    transports.add(transport2);

    int port1 = uniquePort();
    int port2 = uniquePort();

    transport1.start(port1);
    transport2.start(port2);

    assertTrue(transport1.tcpServerChannel().isActive());
    assertTrue(transport1.udpChannel().isActive());
    assertTrue(transport2.tcpServerChannel().isActive());
    assertTrue(transport2.udpChannel().isActive());

    transport1.stop();
    transport2.stop();

    assertFalse(transport1.tcpServerChannel().isActive());
    assertFalse(transport2.tcpServerChannel().isActive());
  }

  /** Validates that fallback character classes are assigned in round-robin order. */
  @Test
  public void test_fallbackCharacterClassesRotate() {
    ServerTransport transport = currentTransport.get();
    PreRunConfiguration.multiplayerCharacterClasses(
        CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03);

    assertEquals(
        CharacterClass.THE_LAST_HOUR_ROGUE,
        transport.selectedCharacterClass(new ConnectRequest((short) 1, "player1")));
    assertEquals(
        CharacterClass.THE_LAST_HOUR_CHAR03,
        transport.selectedCharacterClass(new ConnectRequest((short) 1, "player2")));
    assertEquals(
        CharacterClass.THE_LAST_HOUR_ROGUE,
        transport.selectedCharacterClass(new ConnectRequest((short) 1, "player3")));
  }

  /** Validates that explicit character-class requests do not consume the fallback rotation. */
  @Test
  public void test_explicitCharacterClassDoesNotAdvanceFallbackRotation() {
    ServerTransport transport = currentTransport.get();
    PreRunConfiguration.multiplayerCharacterClasses(
        CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03);

    assertEquals(
        CharacterClass.HUNTER,
        transport.selectedCharacterClass(
            new ConnectRequest(
                (short) 1, "player1", 0, new byte[0], Optional.of(CharacterClass.HUNTER))));
    assertEquals(
        CharacterClass.THE_LAST_HOUR_ROGUE,
        transport.selectedCharacterClass(new ConnectRequest((short) 1, "player2")));
  }

  /**
   * Validates that UDP registration marks the session as UDP-ready and stores the sender mapping.
   */
  @Test
  public void test_udpRegisterActivatesSession() throws Exception {
    ServerTransport transport = currentTransport.get();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session = testSession(tcpCalls);
    byte[] token = new byte[] {1, 2, 3};
    short clientId = 4;
    session.attachClientState(
        new ClientState(
            clientId, "player", ServerRuntime.SESSION_ID, token, CharacterClass.WIZARD));
    clientIdToSessionMap(transport).put(clientId, session);
    InetSocketAddress sender = new InetSocketAddress("127.0.0.1", 25000);

    invokeUdpRegister(
        transport, sender, session, new RegisterUdp(ServerRuntime.SESSION_ID, token, clientId));

    assertTrue(session.udpReady());
    assertEquals(sender, session.udpAddress());
    assertEquals(clientId, udpToClientIdMap(transport).get(sender));
    assertEquals(1, tcpCalls.get());
  }

  /** Validates that stale UDP mappings are removed without closing the TCP session. */
  @Test
  public void test_expireStaleUdpSessionsClearsMapping() throws Exception {
    ServerTransport transport = currentTransport.get();
    Session session = testSession(new AtomicInteger());
    short clientId = 5;
    session.attachClientState(
        new ClientState(
            clientId,
            "player",
            ServerRuntime.SESSION_ID,
            new byte[] {4, 5, 6},
            CharacterClass.WIZARD));
    session.udpAddress(new InetSocketAddress("127.0.0.1", 25001));
    session.markUdpActivity();
    session.udpReady(true);
    clientIdToSessionMap(transport).put(clientId, session);
    udpToClientIdMap(transport).put(session.udpAddress(), clientId);

    transport.expireStaleUdpSessions(session.udpLastSeenTimeMs() + 4_501L);

    assertFalse(session.udpReady());
    assertFalse(udpToClientIdMap(transport).containsKey(session.udpAddress()));
    assertFalse(session.isClosed());
  }

  /** Validates that the same client can reactivate UDP after the stale mapping was removed. */
  @Test
  public void test_udpCanReregisterAfterStaleExpiry() throws Exception {
    ServerTransport transport = currentTransport.get();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session = testSession(tcpCalls);
    byte[] token = new byte[] {7, 8, 9};
    short clientId = 6;
    InetSocketAddress originalSender = new InetSocketAddress("127.0.0.1", 25002);
    InetSocketAddress newSender = new InetSocketAddress("127.0.0.1", 25003);
    session.attachClientState(
        new ClientState(
            clientId, "player", ServerRuntime.SESSION_ID, token, CharacterClass.WIZARD));
    clientIdToSessionMap(transport).put(clientId, session);

    invokeUdpRegister(
        transport,
        originalSender,
        session,
        new RegisterUdp(ServerRuntime.SESSION_ID, token, clientId));
    transport.expireStaleUdpSessions(session.udpLastSeenTimeMs() + 4_501L);
    invokeUdpRegister(
        transport, newSender, session, new RegisterUdp(ServerRuntime.SESSION_ID, token, clientId));

    assertTrue(session.udpReady());
    assertEquals(newSender, session.udpAddress());
    assertEquals(clientId, udpToClientIdMap(transport).get(newSender));
    assertEquals(2, tcpCalls.get());
  }

  private Session testSession(AtomicInteger tcpCalls) {
    ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
    Channel channel = Mockito.mock(Channel.class);
    Mockito.when(ctx.channel()).thenReturn(channel);
    Mockito.when(channel.isActive()).thenReturn(true);
    return new Session(
        ctx,
        (target, msg) -> CompletableFuture.completedFuture(true),
        (channelCtx, msg) -> {
          tcpCalls.incrementAndGet();
          return CompletableFuture.completedFuture(true);
        });
  }

  @SuppressWarnings("unchecked")
  private Map<Short, Session> clientIdToSessionMap(ServerTransport transport) throws Exception {
    Field field = ServerTransport.class.getDeclaredField("clientIdToSession");
    field.setAccessible(true);
    return (Map<Short, Session>) field.get(transport);
  }

  @SuppressWarnings("unchecked")
  private Map<InetSocketAddress, Short> udpToClientIdMap(ServerTransport transport)
      throws Exception {
    Field field = ServerTransport.class.getDeclaredField("udpToClientId");
    field.setAccessible(true);
    return (Map<InetSocketAddress, Short>) field.get(transport);
  }

  private void invokeUdpRegister(
      ServerTransport transport, InetSocketAddress sender, Session session, RegisterUdp registerUdp)
      throws Exception {
    Method method =
        ServerTransport.class.getDeclaredMethod(
            "onUdpRegister", InetSocketAddress.class, Session.class, RegisterUdp.class);
    method.setAccessible(true);
    method.invoke(transport, sender, session, registerUdp);
  }
}
