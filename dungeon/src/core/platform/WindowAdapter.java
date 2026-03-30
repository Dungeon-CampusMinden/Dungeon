package core.platform;

/**
 * Minimal window API that the core can depend on without referencing a concrete engine.
 *
 * <p>This is intentionally tiny for the first step: only what core.Game currently needs.
 */
public interface WindowAdapter {
  int width();

  int height();

  /** Update window title if supported by the active backend. */
  void setTitle(String title);

  /**
   * @return true if the active backend supports switching between windowed and fullscreen mode
   */
  default boolean supportsFullscreen() {
    return false;
  }

  /**
   * @return true if the current window is in fullscreen mode; false otherwise
   */
  default boolean isFullscreen() {
    return false;
  }

  /**
   * Switches the backend window into fullscreen or windowed mode if supported.
   *
   * @param fullscreen true for fullscreen, false for windowed mode
   */
  default void setFullscreen(boolean fullscreen) {
    // no-op by default
  }

  /** Toggles fullscreen mode if supported by the active backend. */
  default void toggleFullscreen() {
    if (!supportsFullscreen()) {
      return;
    }
    setFullscreen(!isFullscreen());
  }
}
