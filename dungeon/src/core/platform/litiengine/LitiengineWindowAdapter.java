package core.platform.litiengine;

import core.platform.WindowAdapter;

/**
 * Window adapter for the LITIENGINE backend.
 *
 * <p>Delegates to {@code de.gurkenlabs.litiengine.Game.window()}.
 */
public final class LitiengineWindowAdapter implements WindowAdapter {

  @Override
  public int width() {
    var window = de.gurkenlabs.litiengine.Game.window();
    return window != null ? window.getWidth() : 0;
  }

  @Override
  public int height() {
    var window = de.gurkenlabs.litiengine.Game.window();
    return window != null ? window.getHeight() : 0;
  }

  @Override
  public void setTitle(String title) {
    var window = de.gurkenlabs.litiengine.Game.window();
    if (window != null) {
      window.setTitle(title);
    }
  }
}
