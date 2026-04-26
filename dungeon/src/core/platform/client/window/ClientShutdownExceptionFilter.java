package core.platform.client.window;

import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.Game;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Filters a known client shutdown race that can otherwise create a misleading crash.txt.
 *
 * <p>The client engine clears its static window reference during termination before the main update
 * loop is guaranteed to have stopped. If that happens while the render component is drawing the
 * cursor, the engine's default handler writes a crash report although the game is already shutting
 * down.
 */
public final class ClientShutdownExceptionFilter implements UncaughtExceptionHandler {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(ClientShutdownExceptionFilter.class);
  private static final String MAIN_UPDATE_LOOP_THREAD = "Main Update Loop";
  private static final String RENDER_COMPONENT_CLASS =
      "de.gurkenlabs.litiengine.graphics.RenderComponent";
  private static final String RENDER_GRAPHICS_METHOD = "renderGraphics";
  private static final String WINDOW_CURSOR_CALL = "de.gurkenlabs.litiengine.GameWindow.cursor()";
  private static final String GAME_WINDOW_CALL = "de.gurkenlabs.litiengine.Game.window()";

  private final UncaughtExceptionHandler delegate;

  /** Installs the shutdown exception filter around the current default exception handler. */
  public static void install() {
    Thread.UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
    if (current instanceof ClientShutdownExceptionFilter) {
      return;
    }

    Game.setUncaughtExceptionHandler(new ClientShutdownExceptionFilter(current));
  }

  /**
   * Creates a new exception filter.
   *
   * @param delegate handler for all non-filtered exceptions
   */
  ClientShutdownExceptionFilter(final UncaughtExceptionHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public void uncaughtException(final Thread thread, final Throwable throwable) {
    if (isBenignShutdownRenderRace(thread, throwable)) {
      LOGGER.info("Ignored client shutdown render race: {}", throwable.getMessage());
      return;
    }

    if (delegate != null && delegate != this) {
      delegate.uncaughtException(thread, throwable);
    }
  }

  /**
   * Checks whether an exception is the known cursor render race during shutdown.
   *
   * @param thread thread that threw the exception
   * @param throwable thrown exception
   * @return true if this is the known benign shutdown race
   */
  static boolean isBenignShutdownRenderRace(final Thread thread, final Throwable throwable) {
    if (thread == null || throwable == null) {
      return false;
    }

    if (!MAIN_UPDATE_LOOP_THREAD.equals(thread.getName())
        || !(throwable instanceof NullPointerException)) {
      return false;
    }

    String message = throwable.getMessage();
    return message != null
        && message.contains(WINDOW_CURSOR_CALL)
        && message.contains(GAME_WINDOW_CALL)
        && hasRenderGraphicsFrame(throwable);
  }

  private static boolean hasRenderGraphicsFrame(final Throwable throwable) {
    for (StackTraceElement frame : throwable.getStackTrace()) {
      if (RENDER_COMPONENT_CLASS.equals(frame.getClassName())
          && RENDER_GRAPHICS_METHOD.equals(frame.getMethodName())) {
        return true;
      }
    }

    return false;
  }
}
