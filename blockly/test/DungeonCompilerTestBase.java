import client.Client;
import core.Game;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Base class for integration tests that run against a live Blockly Dungeon session.
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li>{@link #setUpDungeon()} ({@code @BeforeAll}) starts {@link Client#main} on a daemon thread
 *       and blocks until the HTTP server answers on {@link Server#DEFAULT_PORT}.
 *   <li>Each test can call {@link #sendCode(String)} to submit a program and {@link
 *       #waitForCompletion()} to block until the VM finishes.
 *   <li>{@link #stopCodeAfterEach()} ({@code @AfterEach}) requests the current program to stop,
 *       ensuring a clean state for the next test.
 *   <li>{@link #tearDownDungeon()} ({@code @AfterAll}) interrupts the dungeon thread.
 * </ol>
 */
public class DungeonCompilerTestBase {
  private static final String BASE_URL = "http://localhost:" + Server.DEFAULT_PORT;

  /** Shared HTTP client reused for every request. */
  private static final HttpClient HTTP = HttpClient.newHttpClient();

  static @Nullable Thread clientThread;

  // =========================================================================
  // Lifecycle
  // =========================================================================

  /**
   * Starts the dungeon on a daemon thread and waits up to 30 seconds for the HTTP server to become
   * reachable before returning.
   */
  @BeforeAll
  static void setUpDungeon() {
    clientThread =
        new Thread(
            () -> {
              try {
                // Use --sandbox to start on the empty sandbox level with no blocking popups.
                Client.main(new String[] {"--sandbox", "--debug"});
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            },
            "dungeon-test-client");
    clientThread.setDaemon(true);
    clientThread.start();
    awaitServerReady(Duration.ofSeconds(30));
    awaitPlayerPosition(Duration.ofSeconds(5));
  }

  /**
   * Stops the currently running program after each test so the next test starts from a clean state.
   * Does not tear down the dungeon session itself.
   */
  @AfterEach
  void stopCodeAfterEach() throws IOException, InterruptedException {
    stopExecution();
  }

  /** Interrupts the dungeon thread when the whole test class has finished. */
  @AfterAll
  static void tearDownDungeon() {
    if (clientThread != null) {
      clientThread.interrupt();
      clientThread = null;
    }
  }

  // =========================================================================
  // Communication helpers
  // =========================================================================

  /**
   * Submits {@code code} to the running dungeon via {@code POST /code}.
   *
   * <p>The server wraps the snippet in a {@code Main} class, compiles it, and starts the VM. This
   * call blocks only until the server has accepted the request; use {@link #waitForCompletion()} to
   * block until the VM has finished.
   *
   * @param code the Java method body or snippet to run (will be wrapped in a class by the server)
   * @throws IllegalStateException if the server responds with a non-200 status code
   */
  protected static void sendCode(@NotNull String code) throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/code"))
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(code))
            .timeout(Duration.ofSeconds(10))
            .build();
    HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() != 200) {
      throw new IllegalStateException(
          "Code submission failed (HTTP " + resp.statusCode() + "): " + resp.body());
    }
  }

  /**
   * Returns {@code true} if the dungeon is currently executing a program.
   *
   * <p>Queries {@code GET /status}; the server returns {@code "running"} or {@code "completed"}.
   */
  protected static boolean isRunning() throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/status"))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();
    HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    return "running".equalsIgnoreCase(resp.body().trim());
  }

  /**
   * Blocks until the dungeon is no longer running code, or until {@code timeout} elapses.
   *
   * <p>Polls {@link #isRunning()} every 50 ms.
   *
   * @param timeout maximum time to wait
   * @throws AssertionError if the timeout is exceeded without the program finishing
   */
  protected static void waitForCompletion(@NotNull Duration timeout)
      throws IOException, InterruptedException {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (!isRunning()) return;
      Thread.sleep(100);
    }
    throw new AssertionError("Code did not complete within " + timeout);
  }

  /**
   * Waits up to 30 seconds for the current code run to finish.
   *
   * @see #waitForCompletion(Duration)
   */
  protected static void waitForCompletion() throws IOException, InterruptedException {
    waitForCompletion(Duration.ofSeconds(30));
  }

  /**
   * Asks the dungeon to stop the currently running program via {@code POST /code?stop}.
   *
   * <p>Safe to call when no program is running (the server responds with 200 in both cases).
   */
  protected static void stopExecution() throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/code?stop"))
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(5))
            .build();
    HTTP.send(req, HttpResponse.BodyHandlers.ofString());
  }

  /** Returns the hero's current position. */
  protected static @NotNull Point playerPosition() {
    return Game.player()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .map(PositionComponent::position)
        .map(Point::new)
        .orElseThrow(() -> new AssertionError("No player position available"));
  }

  /** Returns the hero's current facing direction. */
  protected static @NotNull Direction playerDirection() {
    return Game.player()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .map(PositionComponent::viewDirection)
        .orElseThrow(() -> new AssertionError("No player direction available"));
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  /**
   * Polls {@code GET /status} with a 500 ms request timeout until the server responds, indicating
   * the dungeon HTTP server and game loop are ready. Retries every 200 ms.
   *
   * @param timeout overall deadline; throws {@link AssertionError} when exceeded
   */
  private static void awaitServerReady(@NotNull Duration timeout) {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      try {
        HttpRequest req =
            HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/status"))
                .GET()
                .timeout(Duration.ofMillis(500))
                .build();
        HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        return; // server answered — dungeon is ready
      } catch (Exception ignored) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError("Interrupted while waiting for dungeon server to start");
        }
      }
    }
    throw new AssertionError("Dungeon server did not start within " + timeout);
  }

  private static @NotNull Point awaitPlayerPosition(@NotNull Duration timeout) {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      try {
        return playerPosition();
      } catch (AssertionError ignored) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError("Interrupted while waiting for the player to spawn");
        }
      }
    }
    throw new AssertionError("Player did not become available within " + timeout);
  }
}
