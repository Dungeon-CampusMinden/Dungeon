package core.platform.client.adapters;

import core.camera.CameraViewport;
import core.platform.adapters.CursorAdapter;
import core.utils.Point;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.geom.Point2D;

/**
 * An implementation of the {@link CursorAdapter} interface, providing the cursor's position on the
 * screen and within the game world. This class is designed to interact with the game's input and
 * rendering systems to calculate the required coordinates.
 */
public final class ClientCursorAdapter implements CursorAdapter {

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

    return CameraViewport.screenToWorld(new Point((float) p.getX(), (float) p.getY()));
  }
}
