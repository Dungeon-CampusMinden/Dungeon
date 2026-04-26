package core.platform.client.adapters;

import core.platform.adapters.WindowAdapter;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.DisplayMode;

/**
 * An implementation of the {@link WindowAdapter} interface that provides a concrete adapter
 * for managing window operations in a game context.
 *
 * <p>This class interacts with the {@code Game.window()} method to perform window-related operations,
 * such as retrieving window dimensions or setting the window title. For unsupported operations
 * like fullscreen toggling, the implementation provides safe no-op behavior.
 */
public final class ClientWindowAdapter implements WindowAdapter {

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
    return true;
  }

  @Override
  public boolean isFullscreen() {
    try {
      return Game.config().graphics().getDisplayMode() == DisplayMode.FULLSCREEN;
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public void setFullscreen(boolean fullscreen) {
    try {
      Game.config()
        .graphics()
        .setDisplayMode(fullscreen ? DisplayMode.FULLSCREEN : DisplayMode.WINDOWED);
      Game.config().save();
    } catch (Exception ignored) {
      // fail-safe
    }
  }
}
