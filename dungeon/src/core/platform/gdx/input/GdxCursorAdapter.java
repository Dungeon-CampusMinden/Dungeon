package core.platform.gdx.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import core.platform.CursorAdapter;
import core.platform.Platform;
import core.platform.gdx.systems.GdxCameraSystem;
import core.utils.Point;

public final class GdxCursorAdapter implements CursorAdapter {
  @Override
  public int screenX() {
    return Gdx.input.getX();
  }

  @Override
  public int screenY() {
    return Gdx.input.getY();
  }

  @Override
  public Point world() {
    if (!Platform.runtime().supportsGdxRendering()) {
      return new Point(0, 0);
    }
    Vector3 v = new Vector3(screenX(), screenY(), 0);
    GdxCameraSystem.camera().unproject(v);
    return new Point(v.x, v.y);
  }
}
