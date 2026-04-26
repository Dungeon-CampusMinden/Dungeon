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
 * The {@code ClientWindowEventsBridge} class provides functionality to bridge and manage
 * the window close events for the client game window.
 *
 * <p>This class ensures that a custom hook is installed to handle the window close operation in a controlled manner.
 *
 * <p>It integrates with the host control of the client game window and replaces any
 * default or existing window close behavior with a custom one.
 *
 * <p>The process checks for the availability of the game window and its associated host control before proceeding
 * to install the custom behavior.
 *
 * <p>This class is designed to be used as a static utility with no instantiation allowed.
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
