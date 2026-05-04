package core.network.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Game;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.network.messages.NetworkMessage;
import core.systems.LevelSystem;
import core.utils.Tuple;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/**
 * Unit tests for {@link ServerRuntime}.
 *
 * <p>Validates server initialization, lifecycle management, message broadcasting, and client
 * communication of the authoritative server transport.
 */
public class ServerRuntimeTests {

  private static final List<ServerRuntime> runtimes = new ArrayList<>();
  private static final ThreadLocal<ServerRuntime> currentRuntime = new ThreadLocal<>();

  /**
   * Generates a unique port that can be bound by both TCP and UDP before returning it.
   *
   * @return a unique, available port number for testing
   */
  private static synchronized int uniquePort() {
    final int maxAttempts = 100;
    int attempts = 0;
    while (attempts++ < maxAttempts) {
      int candidate = availableTcpPort();
      if (isUdpPortAvailable(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException(
        "Unable to find an available port after " + maxAttempts + " attempts");
  }

  private static int availableTcpPort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to reserve an available TCP port", e);
    }
  }

  private static boolean isUdpPortAvailable(int port) {
    try (DatagramSocket socket = new DatagramSocket(null)) {
      socket.bind(new InetSocketAddress(port));
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Stops all runtimes in the runtimes list and clears the list. This can be called to perform
   * batch cleanup of all created runtimes across all tests.
   */
  private static synchronized void stopAllRuntimes() {
    for (ServerRuntime runtime : runtimes) {
      if (runtime != null) {
        try {
          runtime.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    runtimes.clear();
  }

  /**
   * Sets up the server runtime for testing by initializing {@link Game} with multiplayer server
   * configuration.
   *
   * <p>Configures the game to run in server mode and initializes a fresh {@link ServerRuntime}
   * instance on a unique port.
   */
  @BeforeEach
  public void setup() {
    MockNetworkHandler.useLocalNetworkHandler();
    int port = uniquePort();
    ServerRuntime runtime = new ServerRuntime(port);
    runtimes.add(runtime);
    currentRuntime.set(runtime);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    Game.add(new LevelSystem());
  }

  /**
   * Cleans up server resources and game state after each test.
   *
   * <p>Stops the server runtime, resets multiplayer configuration, and exits the game to ensure
   * isolation between tests.
   */
  @AfterEach
  public void cleanup() {
    ServerRuntime runtime = currentRuntime.get();
    if (runtime != null) {
      runtime.stop();
      runtimes.remove(runtime);
      currentRuntime.remove();
    }
  }

  /** Validates that the server runtime starts without throwing exceptions. */
  @Test
  public void test_serverStarts() {
    ServerRuntime runtime = currentRuntime.get();
    assertDoesNotThrow(runtime::start);
  }

  /** Validates that broadcasting a message returns a completed {@link CompletableFuture}. */
  @Test
  public void test_broadcastReturnsCompletableFuture() {
    ServerRuntime runtime = currentRuntime.get();
    runtime.start();
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = runtime.broadcastMessage(msg, true);
    assertNotNull(result);
    assertTrue(result.isDone());
  }

  /** Validates that starting the server multiple times is safe and idempotent. */
  @Test
  public void test_startIdempotent() {
    ServerRuntime runtime = currentRuntime.get();
    runtime.start();
    runtime.start();
  }

  /** Validates that stopping the server multiple times is safe and idempotent. */
  @Test
  public void test_stopIdempotent() {
    ServerRuntime runtime = currentRuntime.get();
    runtime.start();
    runtime.stop();
    runtime.stop();
  }

  /** Validates that sending a message to a specific client returns a {@link CompletableFuture}. */
  @Test
  public void test_sendMessageReturnsFuture() {
    ServerRuntime runtime = currentRuntime.get();
    runtime.start();
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = runtime.sendMessage((short) 1, msg, true);
    assertNotNull(result);
  }
}
