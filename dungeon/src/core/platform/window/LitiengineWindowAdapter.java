package core.platform.window;

import core.platform.WindowAdapter;
import de.gurkenlabs.litiengine.Game;

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
    return false;
  }

  @Override
  public boolean isFullscreen() {
    return false;
  }

  @Override
  public void setFullscreen(boolean fullscreen) {
    // Temporarily disabled on the LITIENGINE path.
    // Runtime display-mode switching currently appears unstable and can lead
    // to Game.window() becoming unavailable during engine rendering.
  }
}
