package core.platform.client;

import core.platform.WindowAdapter;
import de.gurkenlabs.litiengine.Game;

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
    return false;
  }

  @Override
  public boolean isFullscreen() {
    return false;
  }

  @Override
  public void setFullscreen(boolean fullscreen) {
    // no-op, not supported
  }
}
