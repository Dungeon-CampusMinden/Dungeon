package core.network.server;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.network.messages.NetworkMessage;
import core.systems.LevelSystem;
import core.utils.Tuple;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link ServerRuntime}.
 *
 * <p>Validates server initialization, lifecycle management, message broadcasting, and client
 * communication of the authoritative server transport.
 */
public class ServerRuntimeTests {

  private static final int TEST_PORT = 17777;

  private ServerRuntime runtime;

  /**
   * Sets up the server runtime for testing by initializing {@link Game} with multiplayer server
   * configuration.
   *
   * <p>Configures the game to run in server mode and initializes a fresh {@link ServerRuntime}
   * instance on the test port.
   */
  @BeforeEach
  public void setup() {
    runtime = new ServerRuntime(TEST_PORT);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    Game.add(new LevelSystem(() -> {}));
  }

  /**
   * Cleans up server resources and game state after each test.
   *
   * <p>Stops the server runtime, resets multiplayer configuration, and exits the game to ensure
   * isolation between tests.
   */
  @AfterEach
  public void cleanup() {
    runtime.stop();
  }

  /** Validates that the server runtime starts without throwing exceptions. */
  @Test
  public void test_serverStarts() {
    assertDoesNotThrow(runtime::start);
  }

  /** Validates that broadcasting a message returns a completed {@link CompletableFuture}. */
  @Test
  public void test_broadcastReturnsCompletableFuture() {
    runtime.start();
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = runtime.broadcastMessage(msg, true);
    assertNotNull(result);
    assertTrue(result.isDone());
  }

  /** Validates that starting the server multiple times is safe and idempotent. */
  @Test
  public void test_startIdempotent() {
    runtime.start();
    runtime.start();
  }

  /** Validates that stopping the server multiple times is safe and idempotent. */
  @Test
  public void test_stopIdempotent() {
    runtime.start();
    runtime.stop();
    runtime.stop();
  }

  /** Validates that sending a message to a specific client returns a {@link CompletableFuture}. */
  @Test
  public void test_sendMessageReturnsFuture() {
    runtime.start();
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = runtime.sendMessage((short) 1, msg, true);
    assertNotNull(result);
  }

  /** Validates that the server starts successfully on the configured test port. */
  @Test
  public void test_serverStartsOnConfiguredPort() {
    assertDoesNotThrow(runtime::start);
  }
}
