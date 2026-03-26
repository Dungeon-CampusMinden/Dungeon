package core.platform.litiengine.render;

/**
 * Shared camera state for the LITIENGINE backend.
 *
 * <p>The platform adapter exposes the zoom through {@code Platform.camera()}, while
 * the LITIENGINE renderer consumes the same value to scale the world view.
 */
public final class LitiengineCameraState {
  private static final float MIN_ZOOM = 0.25f;
  private static final float MAX_ZOOM = 4.0f;

  private static volatile float zoom = 1.0f;

  private LitiengineCameraState() {}

  public static float zoom() {
    return zoom;
  }

  public static void zoom(float newZoom) {
    zoom = clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
