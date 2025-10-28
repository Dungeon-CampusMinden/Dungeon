package core.network;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import core.network.handler.NettyNetworkHandler;
import core.network.messages.NetworkMessage;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/**
 * Unit tests for {@link NettyNetworkHandler}.
 *
 * <p>Validates the dual-mode network handler initialization, mode-specific behavior, message
 * sending/broadcasting, lifecycle management, and state consistency in both server and client
 * modes.
 */
public class NettyNetworkHandlerTests {

  private static final String TEST_HOST = "127.0.0.1";
  private static final int TEST_PORT = 17777;

  private NettyNetworkHandler handler;

  /** Initializes a fresh {@link NettyNetworkHandler} instance before each test. */
  @BeforeEach
  public void setup() {
    handler = new NettyNetworkHandler();
    MockNetworkHandler.useLocalNetworkHandler();
  }

  /** Shuts down the network handler after each test to ensure clean state. */
  @AfterEach
  public void cleanup() {
    handler.shutdown("test");
  }

  /** Validates that the handler initializes correctly in server mode. */
  @Test
  public void test_initializeServerMode() throws Exception {
    handler.initialize(true, null, TEST_PORT, null);
    assertTrue(handler.isServer());
  }

  /** Validates that the handler initializes correctly in client mode. */
  @Test
  public void test_initializeClientMode() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    assertFalse(handler.isServer());
  }

  /**
   * Validates that broadcasting messages works correctly in server mode.
   *
   * <p>This test requires full {@link Game} initialization to ensure proper network setup.
   */
  @Test
  public void test_broadcastInServerMode() throws Exception {
    handler.initialize(true, null, TEST_PORT, null);

    handler.start();
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    assertDoesNotThrow(() -> handler.broadcast(msg, true));
  }

  /**
   * Validates that attempting to broadcast in client mode throws {@link
   * UnsupportedOperationException}.
   */
  @Test
  public void test_broadcastInClientModeThrows() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    assertThrows(UnsupportedOperationException.class, () -> handler.broadcast(msg, true));
  }

  /** Validates that the assigned client ID is 0 before connection is established. */
  @Test
  public void test_assignedClientId() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    assertEquals(0, (short) handler.assignedClientId());
  }

  /** Validates that calling shutdown multiple times is safe and idempotent. */
  @Test
  public void test_shutdownIdempotent() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    handler.shutdown("test");
    handler.shutdown("test");
  }

  /**
   * Validates that attempting to send a message while disconnected returns a {@link
   * CompletableFuture}.
   */
  @Test
  public void test_sendWhenDisconnected() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = handler.send((short) 1, msg, true);
    assertNotNull(result);
  }

  /** Validates that the message dispatcher is available after initialization. */
  @Test
  public void test_messageDispatcherAvailable() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    assertNotNull(handler.messageDispatcher());
  }

  /** Validates that the handler reports as not connected before establishing a connection. */
  @Test
  public void test_handlerConnectionState() throws Exception {
    handler.initialize(false, TEST_HOST, TEST_PORT, "TestPlayer");
    assertFalse(handler.isConnected());
  }

  /** Validates that the server mode indicator remains consistent across multiple calls. */
  @Test
  public void test_serverModeConsistent() throws Exception {
    handler.initialize(true, null, TEST_PORT, null);
    assertTrue(handler.isServer());
    assertTrue(handler.isServer());
  }

  /** Validates that attempting to broadcast a null message throws {@link NullPointerException}. */
  @Test
  public void test_broadcastNullMessageThrows() throws Exception {
    handler.initialize(true, null, TEST_PORT, null);
    assertThrows(NullPointerException.class, () -> handler.broadcast(null, true));
  }
}
