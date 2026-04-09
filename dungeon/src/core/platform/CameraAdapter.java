package core.platform;

import core.utils.Point;

/**
 * Backend-specific camera access for gameplay/debug helpers.
 *
 * <p>This interface intentionally stays small. For now it exposes zoom and the current
 * focus position / camera center in world units.
 */
public interface CameraAdapter {

  /**
   * Returns whether the active backend supports zoom control.
   *
   * @return {@code true} if zoom can be read/written, otherwise {@code false}
   */
  default boolean supportsZoom() {
    return false;
  }

  /**
   * Returns the current zoom factor.
   *
   * @return current zoom factor; default is {@code 1f}
   */
  default float zoom() {
    return 1f;
  }

  /**
   * Updates the current zoom factor.
   *
   * @param zoom new zoom factor
   */
  default void zoom(float zoom) {
    // no-op by default
  }

  /**
   * Returns whether the active backend exposes its current focus position.
   *
   * @return {@code true} if the focus position can be read/written
   */
  default boolean supportsFocusPosition() {
    return false;
  }

  /**
   * Returns the current focus position / camera center in world units.
   *
   * @return current focus position; default is the origin
   */
  default Point focusPosition() {
    return new Point(0, 0);
  }

  /**
   * Updates the current focus position / camera center.
   *
   * @param focusPosition new focus position
   */
  default void focusPosition(Point focusPosition) {
    // no-op by default
  }

  /**
   * Returns whether the active backend can resolve a camera follow target from the current game
   * state.
   *
   * @return {@code true} if follow-target resolution is supported
   */
  default boolean supportsFollowTargetResolution() {
    return false;
  }

  /**
   * Resolves the current camera follow target in world units.
   *
   * <p>Backends that do not support this may simply fall back to the current focus position.
   *
   * @return resolved camera follow target
   */
  default Point resolveFollowTarget() {
    return focusPosition();
  }
}
