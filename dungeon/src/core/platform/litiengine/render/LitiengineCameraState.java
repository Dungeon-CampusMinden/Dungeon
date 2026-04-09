package core.platform.litiengine.render;

import core.camera.CameraMath;
import core.utils.Point;
import java.util.Objects;

/**
 * Shared camera state for the LITIENGINE backend.
 *
 * <p>The platform adapter exposes the state through {@code Platform.camera()}, while
 * the LITIENGINE renderer consumes and updates the same values.
 *
 * <p>Besides zoom, this state now also owns the current follow target and the smoothed
 * actual focus position. This removes the last renderer-local camera smoothing state.
 */
public final class LitiengineCameraState {
  private static final float MIN_ZOOM = 0.25f;
  private static final float MAX_ZOOM = 4.0f;

  private static volatile float zoom = 1.0f;

  /** Current visible camera center in world units. */
  private static volatile Point focusPosition = new Point(0, 0);

  /** Desired follow target in world units. */
  private static volatile Point followTarget = new Point(0, 0);

  /**
   * Tracks whether the focus has already been initialized.
   *
   * <p>This preserves the old first-frame behavior: the first update snaps directly to the target
   * instead of interpolating from the origin.
   */
  private static volatile boolean focusInitialized = false;

  private LitiengineCameraState() {}

  public static float zoom() {
    return zoom;
  }

  public static void zoom(float newZoom) {
    zoom = clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
  }

  public static Point focusPosition() {
    return copy(focusPosition);
  }

  public static void focusPosition(Point newFocusPosition) {
    Objects.requireNonNull(newFocusPosition, "newFocusPosition");
    focusPosition = copy(newFocusPosition);
    focusInitialized = true;
  }

  public static Point followTarget() {
    return copy(followTarget);
  }

  public static void followTarget(Point newFollowTarget) {
    Objects.requireNonNull(newFollowTarget, "newFollowTarget");
    followTarget = copy(newFollowTarget);
  }

  /**
   * Advances the current focus toward the current follow target using backend-neutral camera math.
   *
   * <p>On the first step, the focus snaps directly to the target. Afterwards smoothing is applied
   * via {@link CameraMath#stepTowardsFocus(Point, Point, float)}.
   *
   * @param focusLerp smoothing factor in range {@code [0, 1]}
   * @return updated focus position
   */
  public static Point stepFocus(float focusLerp) {
    if (!focusInitialized) {
      focusPosition = copy(followTarget);
      focusInitialized = true;
      return copy(focusPosition);
    }

    focusPosition = CameraMath.stepTowardsFocus(focusPosition, followTarget, focusLerp);
    return copy(focusPosition);
  }

  /**
   * Resets the follow state to its default values.
   *
   * <p>Mainly useful for tests and explicit lifecycle resets.
   */
  public static void resetFocus() {
    focusPosition = new Point(0, 0);
    followTarget = new Point(0, 0);
    focusInitialized = false;
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  private static Point copy(Point point) {
    return new Point(point.x(), point.y());
  }
}
