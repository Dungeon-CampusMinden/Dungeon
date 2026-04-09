package core.platform.litiengine.render;

import core.utils.Point;
import java.util.Objects;

/**
 * Shared camera state for the LITIENGINE backend.
 *
 * <p>The platform adapter exposes the state through {@code Platform.camera()}, while
 * the LITIENGINE renderer consumes and updates the same values.
 */
public final class LitiengineCameraState {
  private static final float MIN_ZOOM = 0.25f;
  private static final float MAX_ZOOM = 4.0f;

  private static volatile float zoom = 1.0f;
  private static volatile Point focusPosition = new Point(0, 0);

  private LitiengineCameraState() {}

  public static float zoom() {
    return zoom;
  }

  public static void zoom(float newZoom) {
    zoom = clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
  }

  public static Point focusPosition() {
    return focusPosition;
  }

  public static void focusPosition(Point newFocusPosition) {
    Objects.requireNonNull(newFocusPosition, "newFocusPosition");
    focusPosition = new Point(newFocusPosition.x(), newFocusPosition.y());
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
