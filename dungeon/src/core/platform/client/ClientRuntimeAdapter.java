package core.platform.client;

import core.platform.adapters.RuntimeAdapter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code ClientRuntimeAdapter} is an implementation of the {@link RuntimeAdapter} interface
 * that provides runtime access for client-specific operations, including application lifecycle
 * management and determining the graphical context availability.
 *
 * <p>This class offers functionality to request a graceful exit of the application and to determine
 * whether the application is running in a headless mode (without a graphical user interface).
 *
 * <p>The {@code ClientRuntimeAdapter} interacts with the {@code Game} core system to manage the
 * termination process of the application and ensures that proper shutdown actions are performed,
 * including waiting for the main game loop thread to complete if necessary.
 *
 */
public final class ClientRuntimeAdapter implements RuntimeAdapter {
  private static final long EXIT_JOIN_TIMEOUT_MS = 2000L;
  private static final AtomicBoolean EXIT_REQUESTED = new AtomicBoolean(false);

  @Override
  public void requestExit() {
    if (!EXIT_REQUESTED.compareAndSet(false, true)) {
      return;
    }

    final IGameLoop loop = Game.loop();
    final Thread loopThread = loop instanceof Thread thread ? thread : null;

    if (loop == null) {
      System.exit(Game.EXIT_GAME_CLOSED);
      return;
    }

    loop.terminate();

    if (loopThread == null) {
      System.exit(Game.EXIT_GAME_CLOSED);
      return;
    }

    if (loopThread == Thread.currentThread()) {
      Thread exitThread =
        new Thread(
          () -> {
            awaitLoopShutdown(loopThread);
            System.exit(Game.EXIT_GAME_CLOSED);
          },
          "exit-game");
      exitThread.setDaemon(true);
      exitThread.start();
      return;
    }

    awaitLoopShutdown(loopThread);
    System.exit(Game.EXIT_GAME_CLOSED);
  }

  @Override
  public boolean isHeadless() {
    return Game.isInNoGUIMode();
  }

  private static void awaitLoopShutdown(Thread loopThread) {
    try {
      loopThread.join(EXIT_JOIN_TIMEOUT_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
