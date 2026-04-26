package core.platform.client.window;

import core.game.WindowEventManager;
import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.Game;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * The ClientWindowEventsBridge is a utility class designed to integrate custom event handling and
 * control for the client window of the application. Specifically, it modifies how window-closing
 * events are handled by introducing a custom close hook, thereby allowing for more controlled
 * shutdown processes involving external handlers.
 *
 * <p>This class provides functionality to:
 *
 * <ul>
 *   <li>Install a custom close behavior on the application's main window.
 *   <li>Replace existing window listeners tied to the closing event with a custom listener
 *       (CloseHook).
 *   <li>Trigger external close event handlers via the WindowEventManager.
 * </ul>
 *
 * <p>The installation is done in a thread-safe manner and will not be re-executed once installed.
 *
 * <p>Thread Safety: This class ensures thread-safety for its installation operations using the
 * volatile `installed` flag.
 *
 * <p>Usage Notes: The installation method should be called early in the application's lifecycle to
 * ensure the custom window event handling is functional. It is also safe to invoke this method
 * multiple times as it will not reinstall once already configured.
 *
 * <p>Logging: The class uses DungeonLogger to log warnings and errors encountered during the
 * installation process and during the invocation of the custom close handling.
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>DungeonLogger: For logging information and warnings.
 *   <li>WindowEventManager: To trigger external close event handlers.
 *   <li>core.Game.exit: For integrating the game's shutdown process.
 *   <li>System.exit: Used as a fallback mechanism for application closure when an issue occurs in
 *       core.Game.exit.
 * </ul>
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>The custom close handling is only applicable to windows that are instances of `JFrame` or
 *       `Window`.
 *   <li>Any previously attached window listeners matching certain criteria (e.g., class names
 *       starting with "de.gurkenlabs.litiengine.GameWindow$") will be removed and replaced by the
 *       custom CloseHook listener.
 * </ul>
 */
public final class ClientWindowEventsBridge {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(ClientWindowEventsBridge.class);
  private static volatile boolean installed = false;

  private ClientWindowEventsBridge() {}

  /** Installs the custom window close hook once the client game window is available. */
  public static synchronized void install() {
    if (installed) return;

    try {
      if (Game.window() == null) {
        LOGGER.warn(
            "ClientWindowEventsBridge.install skipped: game window is not initialized yet.");
        return;
      }

      final Container host = Game.window().getHostControl();
      switch (host) {
        case null -> {
          LOGGER.warn("ClientWindowEventsBridge.install skipped: host control is null.");
          return;
        }
        case JFrame frame -> {
          frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
          replaceCloseHook(frame);
          installed = true;
          return;
        }
        case Window window -> {
          replaceCloseHook(window);
          installed = true;
          return;
        }
        default -> {}
      }

      LOGGER.warn(
          "ClientWindowEventsBridge: host control is not a Window/JFrame: {}",
          host.getClass().getName());
    } catch (Exception e) {
      LOGGER.warn("ClientWindowEventsBridge.install failed: {}", e.getMessage(), e);
    }
  }

  private static void replaceCloseHook(Window window) {
    for (WindowListener listener : window.getWindowListeners()) {
      if (listener instanceof CloseHook) {
        continue;
      }

      String className = listener.getClass().getName();
      if (className.startsWith("de.gurkenlabs.litiengine.GameWindow$")) {
        window.removeWindowListener(listener);
      }
    }

    window.addWindowListener(new CloseHook());
  }

  private static final class CloseHook extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent e) {
      boolean allow = true;
      try {
        allow = WindowEventManager.fireCloseRequested();
      } catch (Exception _) {
      }

      if (allow) {
        try {
          core.Game.exit("Game closed");
        } catch (Exception ex) {
          LOGGER.warn(
              "core.Game.exit failed, falling back to System.exit: {}", ex.getMessage(), ex);
          System.exit(0);
        }
      }
    }
  }
}
