package core.network.server;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import core.game.PreRunConfiguration;
import core.network.messages.NetworkMessage;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link ServerTransport}.
 *
 * <p>Validates Netty-based server transport initialization, channel lifecycle, session management,
 * message broadcasting, and input queue handling.
 */
public class ServerTransportTests {

  private static final int TEST_PORT = 17777;

  private ServerTransport transport;

  /**
   * Sets up the server transport for testing by initializing {@link Game} with multiplayer server
   * configuration.
   *
   * <p>Configures the game to run in server mode and initializes a fresh {@link ServerTransport}
   * instance.
   */
  @BeforeEach
  public void setup() {
    transport = new ServerTransport();
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    Game.run();
  }

  /**
   * Cleans up server transport resources and game state after each test.
   *
   * <p>Stops the transport, resets multiplayer configuration, and exits the game to ensure
   * isolation between tests.
   */
  @AfterEach
  public void cleanup() {
    transport.stop();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(false);
    Game.exit();
  }

  /**
   * Validates that the transport starts successfully on the configured test port and creates both
   * TCP and UDP channels.
   */
  @Test
  public void test_transportStartsOnConfiguredPort() {
    transport.start(TEST_PORT);
    assertNotNull(transport.tcpServerChannel());
    assertNotNull(transport.udpChannel());
  }

  /** Validates that both TCP and UDP channels are active after the transport starts. */
  @Test
  public void test_transportChannelsActive() {
    transport.start(TEST_PORT);
    assertTrue(transport.tcpServerChannel().isActive());
    assertTrue(transport.udpChannel().isActive());
  }

  /** Validates that both TCP and UDP channels are properly closed when the transport stops. */
  @Test
  public void test_stopClosesChannels() {
    transport.start(TEST_PORT);
    transport.stop();
    assertFalse(transport.tcpServerChannel().isActive());
    assertFalse(transport.udpChannel().isActive());
  }

  /** Validates that the session map is empty after transport initialization. */
  @Test
  public void test_sessionsMapEmpty() {
    transport.start(TEST_PORT);
    assertTrue(transport.sessions().isEmpty());
  }

  /** Validates that the client ID to session mapping is empty after transport initialization. */
  @Test
  public void test_clientIdMappingEmpty() {
    transport.start(TEST_PORT);
    assertTrue(transport.clientIdToSessionMap().isEmpty());
  }

  /** Validates that the client ID counter is initialized to a value greater than or equal to 1. */
  @Test
  public void test_clientIdCounterIncrements() {
    transport.start(TEST_PORT);
    int initial = transport.nextClientIdValue();
    assertTrue(initial >= 1);
  }

  /**
   * Validates that broadcasting a message to an empty session map returns a completed {@link
   * CompletableFuture}.
   */
  @Test
  public void test_broadcastEmptySessionMap() {
    transport.start(TEST_PORT);
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = transport.broadcast(msg, true);
    assertNotNull(result);
    assertTrue(result.isDone());
  }

  /** Validates that the input queue is initialized and empty before any messages are received. */
  @Test
  public void test_inputQueueInitialized() {
    assertNotNull(transport.inputQueue());
    assertTrue(transport.inputQueue().isEmpty());
  }

  /**
   * Validates that the transport can be started and stopped multiple times without error,
   * demonstrating idempotent lifecycle management.
   */
  @Test
  public void test_startStopIdempotent() {
    transport.start(TEST_PORT);
    transport.stop();
    transport.start(TEST_PORT);
    transport.stop();
  }

  /**
   * Validates that the message dispatcher is initialized and available after the transport starts.
   */
  @Test
  public void test_dispatcherInitialized() {
    transport.start(TEST_PORT);
    assertNotNull(transport.dispatcher());
  }
}
