package engine.game;

import engine.Game;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Idempotent shutdown bridge for a dedicated server process. */
public final class ServerLifecycle {
  private final AtomicBoolean exitRequested = new AtomicBoolean();
  private final Runnable afterExit;

  private ServerLifecycle(final Runnable afterExit) {
    this.afterExit = afterExit;
  }

  /**
   * Installs direct-JVM and, when applicable, managed-parent shutdown handling.
   *
   * @param shutdownReason reason used when the process or its managing parent stops the server
   * @param afterExit callback invoked after the game shutdown path finishes
   * @return lifecycle handle for authoritative in-game shutdown requests
   */
  public static ServerLifecycle install(final String shutdownReason, final Runnable afterExit) {
    Objects.requireNonNull(shutdownReason, "shutdownReason");
    ServerLifecycle lifecycle = new ServerLifecycle(Objects.requireNonNull(afterExit, "afterExit"));
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(() -> lifecycle.requestExit(shutdownReason), "dedicated-server-shutdown"));
    if (Boolean.getBoolean(ServerProcess.MANAGED_PROPERTY)) {
      lifecycle.startManagedStopMonitor(shutdownReason);
    }
    return lifecycle;
  }

  /**
   * Installs a lifecycle bridge that needs no post-exit notification.
   *
   * @param shutdownReason reason used when the process or its managing parent stops the server
   * @return lifecycle handle for authoritative in-game shutdown requests
   */
  public static ServerLifecycle install(final String shutdownReason) {
    return install(shutdownReason, () -> {});
  }

  /**
   * Routes the first shutdown request through the complete game exit path.
   *
   * @param reason reason passed to the game shutdown path
   */
  public void requestExit(final String reason) {
    if (!exitRequested.compareAndSet(false, true)) {
      return;
    }
    try {
      Game.exit(reason);
    } finally {
      afterExit.run();
    }
  }

  private void startManagedStopMonitor(final String shutdownReason) {
    Thread.ofPlatform()
        .daemon()
        .name("managed-server-stop")
        .start(
            () -> {
              try {
                System.in.read();
              } catch (IOException exception) {
                // A broken management pipe has the same lifecycle meaning as EOF.
              }
              requestExit(shutdownReason);
            });
  }
}
