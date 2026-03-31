package core.platform.litiengine;

import core.game.PreRunConfiguration;
import core.platform.WindowAdapter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.DisplayMode;
import de.gurkenlabs.litiengine.gui.screens.Resolution;

/**
 * LITIENGINE-backed implementation of the platform window abstraction.
 */
public final class LitiengineWindowAdapter implements WindowAdapter {

  @Override
  public int width() {
    try {
      return Game.window() != null ? Game.window().getWidth() : 0;
    } catch (Exception ignored) {
      return 0;
    }
  }

  @Override
  public int height() {
    try {
      return Game.window() != null ? Game.window().getHeight() : 0;
    } catch (Exception ignored) {
      return 0;
    }
  }

  @Override
  public void setTitle(String title) {
    try {
      if (Game.window() != null) {
        Game.window().setTitle(title);
      }
    } catch (Exception ignored) {
      // fail-safe
    }
  }

  @Override
  public boolean supportsFullscreen() {
    try {
      return Game.window() != null && Game.config() != null && Game.config().graphics() != null;
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public boolean isFullscreen() {
    try {
      if (Game.config() == null || Game.config().graphics() == null) {
        return false;
      }

      DisplayMode mode = Game.config().graphics().getDisplayMode();
      return mode == DisplayMode.FULLSCREEN || mode == DisplayMode.BORDERLESS;
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public void setFullscreen(boolean fullscreen) {
    if (!supportsFullscreen()) {
      return;
    }

    try {
      if (fullscreen) {
        Game.config().graphics().setDisplayMode(DisplayMode.BORDERLESS);
      } else {
        Game.config().graphics().setDisplayMode(DisplayMode.WINDOWED);
        Game.window()
          .setResolution(
            Resolution.custom(
              PreRunConfiguration.windowWidth(),
              PreRunConfiguration.windowHeight(),
              "Dungeon Windowed"));
      }
    } catch (Exception ignored) {
      // fail-safe
    }
  }
}
