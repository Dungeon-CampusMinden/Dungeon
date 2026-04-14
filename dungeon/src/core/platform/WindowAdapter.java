package core.platform;

/**
 * Platform adapter interface for window/display management.
 *
 * <p>WindowAdapter abstracts window-level operations, allowing the engine to control and query
 * window properties independently of the underlying graphics framework or platform.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Querying window dimensions (width and height)
 *   <li>Setting the window title
 *   <li>Managing fullscreen mode (if supported)
 * </ul>
 *
 * <p>All methods except dimension and title operations provide default implementations for
 * graceful degradation on platforms that don't support certain features.
 */
public interface WindowAdapter {

  /**
   * Gets the current width of the window in pixels.
   *
   * @return the window width
   */
  int width();

  /**
   * Gets the current height of the window in pixels.
   *
   * @return the window height
   */
  int height();

  /**
   * Sets the title of the window.
   *
   * @param title the new window title text
   */
  void setTitle(String title);

  /**
   * Checks whether fullscreen mode is supported on this platform.
   *
   * <p>The default implementation returns false. Platforms that support fullscreen mode should
   * override this method to return true.
   *
   * @return true if fullscreen mode is supported, false otherwise
   */
  default boolean supportsFullscreen() {
    return false;
  }

  /**
   * Checks whether the window is currently in fullscreen mode.
   *
   * <p>The default implementation returns false. This method should only return true if the window
   * is actually in fullscreen mode.
   *
   * @return true if the window is in fullscreen mode, false otherwise
   */
  default boolean isFullscreen() {
    return false;
  }

  /**
   * Sets the window fullscreen state.
   *
   * <p>This is a no-op by default. Implementations that support fullscreen mode should override
   * this method to change the fullscreen state.
   *
   * @param fullscreen true to enter fullscreen mode, false to exit fullscreen mode
   */
  default void setFullscreen(boolean fullscreen) {
    // no-op by default
  }

  /**
   * Toggles the window fullscreen state.
   *
   * <p>If fullscreen is supported, this method switches between fullscreen and windowed modes.
   * If fullscreen is not supported, this method does nothing.
   */
  default void toggleFullscreen() {
    if (!supportsFullscreen()) {
      return;
    }
    setFullscreen(!isFullscreen());
  }
}
