package core.camera;

import core.utils.Point;
import java.util.Objects;

/**
 * A centralized state holder for camera parameters.
 *
 * <p>This class maintains the current camera zoom level, focus position, and follow target in a
 * thread-safe manner.
 *
 * <p>It provides methods to query and update these values, as well as to advance the focus position
 * toward the follow target using configurable smoothing.
 */
public final class CameraState {
  private static final float MIN_ZOOM = 0.25f;
  private static final float MAX_ZOOM = 4.0f;

  private static volatile float zoom = 1.0f;
  private static volatile Point focusPosition = new Point(0, 0);
  private static volatile Point followTarget = new Point(0, 0);
  private static volatile boolean focusInitialized = false;

  private CameraState() {}

  /**
   * Gets the current camera zoom level.
   *
   * @return the zoom factor (between 0.25 and 4.0)
   */
  public static float zoom() {
    return zoom;
  }

  /**
   * Sets the camera zoom level.
   *
   * @param newZoom the desired zoom factor; values outside the range [0.25, 4.0] are clamped
   */
  public static void zoom(float newZoom) {
    zoom = Math.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
  }

  /**
   * Gets the current camera focus position.
   *
   * @return a copy of the current focus position
   */
  public static Point focusPosition() {
    return copy(focusPosition);
  }

  /**
   * Sets the follow target position.
   *
   * @param newFollowTarget the new follow target (must not be null)
   * @throws NullPointerException if the newFollowTarget is null
   */
  public static void followTarget(Point newFollowTarget) {
    Objects.requireNonNull(newFollowTarget, "newFollowTarget");
    followTarget = copy(newFollowTarget);
  }

  /**
   * Seeds the camera focus with an initial position.
   *
   * <p>Both the focus position and follow target are set to the provided value, and the focus is
   * marked as initialized.
   *
   * @param seededFocus the initial focus position (must not be null)
   * @throws NullPointerException if seededFocus is null
   */
  public static void seedFocus(Point seededFocus) {
    Objects.requireNonNull(seededFocus, "seededFocus");
    followTarget = copy(seededFocus);
    focusPosition = copy(seededFocus);
    focusInitialized = true;
  }

  /**
   * Advances the current focus toward the current follow target using backend-neutral camera math.
   *
   * <p>On the first step, the focus snaps directly to the target. Afterward smoothing is applied
   * via {@link CameraMath#stepTowardsFocus(Point, Point, float)}.
   *
   * @param focusLerpFactor smoothing factor in range {@code [0, 1]}
   * @return updated focus position
   */
  public static Point stepFocus(float focusLerpFactor) {
    if (!focusInitialized) {
      Point target = copy(followTarget);
      focusPosition = target;
      focusInitialized = true;
      return target;
    }

    Point current = copy(focusPosition);
    Point target = copy(followTarget);
    Point newPosition = CameraMath.stepTowardsFocus(current, target, focusLerpFactor);
    focusPosition = newPosition;
    return copy(newPosition);
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

  private static Point copy(Point point) {
    return new Point(point.x(), point.y());
  }
}

