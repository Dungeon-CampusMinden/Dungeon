package core.platform;

/**
 * Backend-specific camera access for gameplay/debug helpers.
 *
 * <p>This interface intentionally stays very small. For now it only exposes the camera zoom that
 * is currently needed outside backend packages.
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
}
