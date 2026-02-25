package core.network.client;

import static org.junit.jupiter.api.Assertions.*;

import core.network.ConnectionListener;
import core.network.messages.c2s.InputMessage;
import core.utils.Vector2;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    assertDoesNotThrow(() -> client.initialize(TEST_HOST, TEST_PORT, "TestPlayer"));
  }

  /** Validates that client is not connected after initialization with valid parameters. */
  @Test
  public void test_initializeWithValidParams() {
    client.initialize("localhost", TEST_PORT, "Player1");
    assertFalse(client.isConnected());
  }

  /** Validates that the message dispatcher is available after initialization. */
  @Test
  public void test_dispatcherAvailable() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    assertNotNull(client.dispatcher());
  }

  /** Validates that client is not connected before calling start. */
  @Test
  public void test_notConnectedBeforeStart() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    assertFalse(client.isConnected());
  }

  /** Validates that shutdown can be called multiple times without error. */
  @Test
  public void test_shutdownIdempotent() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    client.shutdown("test");
    client.shutdown("test");
  }

  /** Validates that client ID returns 0 before connection is established. */
  @Test
  public void test_clientIdBeforeConnection() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    assertEquals(0, (short) client.clientId());
  }

  /** Validates that sending a reliable message returns a CompletableFuture. */
  @Test
  public void test_sendReliableReturnsFuture() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    CompletableFuture<Boolean> result = client.sendReliable(null);
    assertNotNull(result);
  }

  /** Validates that sending an unreliable input message does not throw exceptions. */
  @Test
  public void test_sendUnreliableInputSafe() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
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
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    assertDoesNotThrow(() -> client.addConnectionListener(null));
  }

  /** Validates that removing a null connection listener does not throw exceptions. */
  @Test
  public void test_removeNullListenerSafe() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    assertDoesNotThrow(() -> client.removeConnectionListener(null));
  }

  /** Validates that connection listeners can be added and removed without error. */
  @Test
  public void test_addRemoveListener() {
    client.initialize(TEST_HOST, TEST_PORT, "TestPlayer");
    ConnectionListener listener = new TestConnectionListener();
    assertDoesNotThrow(() -> client.addConnectionListener(listener));
    assertDoesNotThrow(() -> client.removeConnectionListener(listener));
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
