package core.platform.adapters;

import core.utils.Point;

/**
 * An interface representing a generic camera adapter for managing camera behaviors
 * such as zooming, focusing, and following targets.
 */
public interface CameraAdapter {

  /**
   * Checks whether this adapter supports zoom control.
   *
   * @return true if zoom functionality is supported, false otherwise
   */
  default boolean supportsZoom() {
    return false;
  }

  /**
   * Gets the current zoom level.
   *
   * <p>The default zoom level is 1.0. Values greater than 1.0 represent magnification;
   * values less than 1.0 represent reduction.
   *
   * @return the current zoom level
   */
  default float zoom() {
    return 1f;
  }

  /**
   * Sets the zoom level.
   *
   * <p>This is a no-op by default. Implementations that support zoom should override this method.
   *
   * @param zoom the zoom level to set (typically 1.0 for normal, > 1.0 for magnified, < 1.0 for reduced)
   */
  default void zoom(float zoom) {
    // no-op by default
  }

  /**
   * Gets the current camera focus position (center of the camera viewport).
   *
   * <p>The default focus position is (0, 0).
   *
   * @return the current focus position in world coordinates
   */
  default Point focusPosition() {
    return new Point(0, 0);
  }

  /**
   * Checks whether this adapter supports resolving dynamic follow targets.
   *
   * @return true if follow target resolution is supported, false otherwise
   */
  default boolean supportsFollowTargetResolution() {
    return false;
  }

  /**
   * Resolves the follow target position, used for dynamic camera tracking.
   *
   * <p>This method can be used to implement camera following behavior by returning the position
   * that the camera should follow (e.g., player position). Default implementation returns the
   * current focus position.
   *
   * @return the position to follow, or the current focus position if not implemented
   */
  default Point resolveFollowTarget() {
    return focusPosition();
  }
}
