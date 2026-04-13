package core.platform.litiengine.input;

import core.platform.CursorAdapter;
import core.camera.CameraState;
import core.camera.CameraViewportState;
import core.utils.Point;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.geom.Point2D;

public final class LitiengineCursorAdapter implements CursorAdapter {

  @Override
  public int screenX() {
    Point2D p = Input.mouse().getLocation();
    return p == null ? 0 : (int) Math.round(p.getX());
  }

  @Override
  public int screenY() {
    Point2D p = Input.mouse().getLocation();
    return p == null ? 0 : (int) Math.round(p.getY());
  }

  @Override
  public Point world() {
    Point2D p = Input.mouse().getLocation();
    if (p == null) {
      return new Point(0, 0);
    }

    return CameraViewportState.screenToWorld(
      new Point((float) p.getX(), (float) p.getY()),
      CameraState.focusPosition(),
      widthSafe(),
      heightSafe());
  }

  private static int widthSafe() {
    try {
      return Game.window().getWidth();
    } catch (Exception ignored) {
      return 1280;
    }
  }

  private static int heightSafe() {
    try {
      return Game.window().getHeight();
    } catch (Exception ignored) {
      return 720;
    }
  }
}
