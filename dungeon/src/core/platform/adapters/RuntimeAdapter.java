package core.platform.adapters;

/**
 * Platform adapter interface for runtime and system operations.
 *
 * <p>RuntimeAdapter abstracts runtime-level operations, allowing the engine to query and control
 * runtime behavior without coupling to specific platform implementations.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Requesting application exit/shutdown
 *   <li>Detecting whether the application is running in headless mode
 * </ul>
 */
public interface RuntimeAdapter {

  /**
   * Requests the application to exit/shutdown.
   *
   * <p>This method signals that the application should terminate. The actual shutdown behavior
   * depends on the implementation and may be asynchronous (i.e., immediate exit is not guaranteed).
   */
  void requestExit();

  /**
   * Checks whether the application is running in headless mode.
   *
   * <p>Headless mode indicates the absence of a display/graphics output (e.g., running on a
   * server or in a test environment). In headless mode, certain UI and rendering operations
   * may not be available.
   *
   * @return true if running in headless mode, false otherwise
   */
  boolean isHeadless();
}
