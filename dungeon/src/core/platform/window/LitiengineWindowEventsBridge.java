package core.platform.window;

import core.game.WindowEventManager;
import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.Game;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public final class LitiengineWindowEventsBridge {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LitiengineWindowEventsBridge.class);
  private static volatile boolean installed = false;

  private LitiengineWindowEventsBridge() {}

  public static void install() {
    if (installed) return;
    installed = true;

    try {
      final Container host = Game.window().getHostControl();
      if (host instanceof JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        replaceCloseHook(frame);
        return;
      }
      if (host instanceof Window window) {
        replaceCloseHook(window);
        return;
      }

      LOGGER.warn("LitiengineWindowEventsBridge: host control is not a Window/JFrame: {}", host.getClass().getName());
    } catch (Exception e) {
      LOGGER.warn("LitiengineWindowEventsBridge.install failed: {}", e.getMessage(), e);
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
      } catch (Exception _) {}

      if (allow) {
        try {
          core.Game.exit("Game closed");
        } catch (Exception ex) {
          LOGGER.warn("core.Game.exit failed, falling back to System.exit: {}", ex.getMessage(), ex);
          System.exit(0);
        }
      }
    }
  }
}
