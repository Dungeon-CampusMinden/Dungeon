package core.platform.gdx.window;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import core.game.WindowEventManager;
import core.utils.logging.DungeonLogger;

/**
 * Bridge between the libGDX/LWJGL3 window-event system and the engine's
 * {@link WindowEventManager}.
 *
 * <p>This utility class creates a {@link Lwjgl3WindowListener} that forwards
 * every native window event to the corresponding {@code WindowEventManager}
 * fire-method, decoupling the rest of the engine from the libGDX back-end.
 *
 * <p>Instances of this class cannot be created; use the static factory
 * method {@link #listener()} instead.
 */
public final class GdxWindowEventsBridge {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GdxWindowEventsBridge.class);

  /** Private constructor – this is a utility class and must not be instantiated. */
  private GdxWindowEventsBridge() {}

  /**
   * Creates a new {@link Lwjgl3WindowListener} that forwards all native
   * LWJGL3 window events to the engine's {@link WindowEventManager}.
   *
   * @return a fully configured {@link Lwjgl3WindowListener}; never {@code null}
   */
  public static Lwjgl3WindowListener listener() {
    return new Lwjgl3WindowListener() {

      /**
       * Called by LWJGL3 after the OS window has been created.
       *
       * @param w the newly created {@link Lwjgl3Window}
       */
      @Override
      public void created(Lwjgl3Window w) {
        WindowEventManager.fireWindowCreated(w);
      }

      /**
       * Called when one or more files are dropped onto the window.
       *
       * @param files absolute paths of the dropped files
       */
      @Override
      public void filesDropped(String[] files) {
        WindowEventManager.fireFilesDropped(files);
      }

      /**
       * Called when the window receives keyboard/input focus.
       */
      @Override
      public void focusGained() {
        WindowEventManager.fireFocusChanged(true);
      }

      /**
       * Called when the window loses keyboard/input focus.
       */
      @Override
      public void focusLost() {
        WindowEventManager.fireFocusChanged(false);
      }

      /**
       * Called when the window is iconified (minimized) or restored.
       *
       * @param isIconified {@code true} if the window was iconified;
       *                    {@code false} if it was restored
       */
      @Override
      public void iconified(boolean isIconified) {
        WindowEventManager.fireIconified(isIconified);
      }

      /**
       * Called when the window is maximized or restored from the maximized state.
       *
       * @param isMaximized {@code true} if the window was maximized;
       *                    {@code false} if it was restored
       */
      @Override
      public void maximized(boolean isMaximized) {
        WindowEventManager.fireMaximized(isMaximized);
      }

      /**
       * Called when the OS requests that the window contents be refreshed
       * (e.g. after the window was exposed from behind another window).
       */
      @Override
      public void refreshRequested() {
        WindowEventManager.fireRefreshRequested();
      }

      /**
       * Called when the user attempts to close the window (e.g. by clicking
       * the close button).
       *
       * <p>If forwarding the event fails for any reason, a warning is logged
       * and the method returns {@code true} so that the default close
       * behavior (exit) still takes place.
       *
       * @return {@code true} if the window should be closed; {@code false} if
       *         a registered listener vetoed the close request
       */
      @Override
      public boolean closeRequested() {
        try {
          return WindowEventManager.fireCloseRequested();
        } catch (Exception e) {
          LOGGER.warn("closeRequested forwarding failed: {}", e.getMessage(), e);
          return true;
        }
      }
    };
  }
}
