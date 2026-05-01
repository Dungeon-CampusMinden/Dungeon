package core.network.client;

import static org.junit.jupiter.api.Assertions.*;

import core.network.ConnectionListener;
import core.network.config.NetworkConfig;
import core.network.messages.c2s.InputMessage;
import core.network.server.Session;
import core.utils.Vector2;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link ClientNetwork}.
 *
 * <p>Validates initialization, lifecycle management, message sending, connection handling, and
 * event dispatching of the client-side network transport.
 */
public class ClientNetworkTests {

  private static final String TEST_HOST = "127.0.0.1";
  private static final int TEST_PORT = 17777;

  private ClientNetwork client;

  /** Initializes a fresh {@link ClientNetwork} instance before each test. */
  @BeforeEach
  public void setup() {
    client = new ClientNetwork();
  }

  /** Shuts down the client network after each test to ensure clean state. */
  @AfterEach
  public void cleanup() {
    client.shutdown("test");
  }

  /** Validates that client network initializes without throwing exceptions. */
  @Test
  public void test_clientInitializes() {
    assertDoesNotThrow(
        () -> client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty()));
  }

  /** Validates that client is not connected after initialization with valid parameters. */
  @Test
  public void test_initializeWithValidParams() {
    client.initialize("localhost", TEST_PORT, "Player1", Optional.empty());
    assertFalse(client.isConnected());
  }

  /** Validates that the message dispatcher is available after initialization. */
  @Test
  public void test_dispatcherAvailable() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertNotNull(client.dispatcher());
  }

  /** Validates that client is not connected before calling start. */
  @Test
  public void test_notConnectedBeforeStart() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertFalse(client.isConnected());
  }

  /** Validates that shutdown can be called multiple times without error. */
  @Test
  public void test_shutdownIdempotent() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    client.shutdown("test");
    client.shutdown("test");
  }

  /** Validates that client ID returns 0 before connection is established. */
  @Test
  public void test_clientIdBeforeConnection() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertEquals(0, (short) client.clientId());
  }

  /** Validates that sending a reliable message returns a CompletableFuture. */
  @Test
  public void test_sendReliableReturnsFuture() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    CompletableFuture<Boolean> result = client.sendReliable(null);
    assertNotNull(result);
  }

  /** Validates that sending an unreliable input message does not throw exceptions. */
  @Test
  public void test_sendUnreliableInputSafe() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertDoesNotThrow(
        () ->
            client.sendUnreliableInput(
                new InputMessage(
                    0,
                    0,
                    (short) 0,
                    InputMessage.Action.MOVE,
                    new InputMessage.Move(Vector2.of(0, 0)))));
  }

  /** Validates that adding a null connection listener does not throw exceptions. */
  @Test
  public void test_addNullListenerSafe() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertDoesNotThrow(() -> client.addConnectionListener(null));
  }

  /** Validates that removing a null connection listener does not throw exceptions. */
  @Test
  public void test_removeNullListenerSafe() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    assertDoesNotThrow(() -> client.removeConnectionListener(null));
  }

  /** Validates that connection listeners can be added and removed without error. */
  @Test
  public void test_addRemoveListener() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer", Optional.empty());
    ConnectionListener listener = new TestConnectionListener();
    assertDoesNotThrow(() -> client.addConnectionListener(listener));
    assertDoesNotThrow(() -> client.removeConnectionListener(listener));
  }

  /** Validates that small inputs fall back to TCP before UDP registration succeeds. */
  @Test
  public void test_sendUnreliableInputFallsBackToTcpBeforeAck() throws Exception {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session = testSession(udpCalls, tcpCalls);
    session.udpAddress(new InetSocketAddress(TEST_HOST, TEST_PORT));
    session.udpReady(false);
    prepareConnectedClient(session, (short) 7);

    client.sendUnreliableInput(
        new InputMessage(
            1, 1, (short) 1, InputMessage.Action.MOVE, new InputMessage.Move(Vector2.of(1, 0))));

    assertEquals(0, udpCalls.get());
    assertEquals(1, tcpCalls.get());
  }

  /** Validates that UDP resumes once the server acknowledges the registration. */
  @Test
  public void test_sendUnreliableInputUsesUdpAfterAck() throws Exception {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session = testSession(udpCalls, tcpCalls);
    session.udpAddress(new InetSocketAddress(TEST_HOST, TEST_PORT));
    session.udpReady(false);
    prepareConnectedClient(session, (short) 7);

    client.onRegisterAck(true);
    client.sendUnreliableInput(
        new InputMessage(
            1, 1, (short) 1, InputMessage.Action.MOVE, new InputMessage.Move(Vector2.of(0, 1))));

    assertTrue(client.udpRecoveryState().udpReady());
    assertEquals(1, udpCalls.get());
    assertEquals(0, tcpCalls.get());
  }

  /** Validates that oversized inputs always use TCP regardless of UDP state. */
  @Test
  public void test_sendUnreliableInputUsesTcpForOversizedPayload() throws Exception {
    AtomicInteger udpCalls = new AtomicInteger();
    AtomicInteger tcpCalls = new AtomicInteger();
    Session session = testSession(udpCalls, tcpCalls);
    session.udpAddress(new InetSocketAddress(TEST_HOST, TEST_PORT));
    session.udpReady(true);
    prepareConnectedClient(session, (short) 7);

    client.sendUnreliableInput(
        new InputMessage(
            1,
            1,
            (short) 1,
            InputMessage.Action.CUSTOM,
            new InputMessage.Custom("test:large", new byte[NetworkConfig.SAFE_UDP_MTU + 1], 1)));

    assertEquals(0, udpCalls.get());
    assertEquals(1, tcpCalls.get());
  }

  private Session testSession(AtomicInteger udpCalls, AtomicInteger tcpCalls) {
    return new Session(
        Mockito.mock(ChannelHandlerContext.class),
        (target, msg) -> {
          udpCalls.incrementAndGet();
          return CompletableFuture.completedFuture(true);
        },
        (ctx, msg) -> {
          tcpCalls.incrementAndGet();
          return CompletableFuture.completedFuture(true);
        });
  }

  private void prepareConnectedClient(Session session, short assignedClientId) throws Exception {
    setField("session", session);
    setField("clientId", assignedClientId);

    Channel tcpChannel = Mockito.mock(Channel.class);
    Mockito.when(tcpChannel.isActive()).thenReturn(true);
    setField("tcp", tcpChannel);

    AtomicBoolean running = atomicBooleanField("running");
    AtomicBoolean connected = atomicBooleanField("connected");
    running.set(true);
    connected.set(true);
  }

  private AtomicBoolean atomicBooleanField(String fieldName) throws Exception {
    Field field = ClientNetwork.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return (AtomicBoolean) field.get(client);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ClientNetwork.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(client, value);
  }

  /** Test implementation of {@link ConnectionListener} for validating listener behavior. */
  private static class TestConnectionListener implements ConnectionListener {

    /** Invoked when connection is established. */
    @Override
    public void onConnected() {}

    /**
     * Invoked when connection is closed.
     *
     * @param cause human-readable reason for disconnection
     */
    @Override
    public void onDisconnected(String cause) {}
  }
}
