package core.platform.litiengine;

import core.platform.RuntimeAdapter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runtime adapter for the LITIENGINE backend.
 *
 * <p>Stops the LITIENGINE loop before terminating the JVM so the engine doesn't render another
 * frame while its shutdown hook is already nulling global state.
 */
public final class LitiengineRuntimeAdapter implements RuntimeAdapter {
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
          "litiengine-exit");
      exitThread.setDaemon(true);
      exitThread.start();
      return;
    }

    awaitLoopShutdown(loopThread);
    System.exit(Game.EXIT_GAME_CLOSED);
  }

  @Override
  public boolean isHeadless() {
    // LITIENGINE provides a "no GUI mode" flag.
    // If no GUI is shown, we treat it as headless.
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
