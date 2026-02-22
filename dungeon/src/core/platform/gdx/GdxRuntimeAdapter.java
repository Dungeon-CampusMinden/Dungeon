package core.platform.gdx;

import com.badlogic.gdx.Gdx;
import core.platform.RuntimeAdapter;

public final class GdxRuntimeAdapter implements RuntimeAdapter {
  @Override
  public void requestExit() {
    if (Gdx.app != null) {
      Gdx.app.exit();
    }
  }

  @Override
  public boolean isHeadless() {
    return Gdx.gl == null || Gdx.graphics == null;
  }

  @Override
  public boolean supportsGdxRendering() {
    // Only true when the libGDX OpenGL + graphics + files context is actually available.
    return Gdx.gl != null && Gdx.graphics != null && Gdx.files != null;
  }
}
