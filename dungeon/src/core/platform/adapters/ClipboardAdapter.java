package core.platform.adapters;

/**
 * Platform adapter interface for clipboard access.
 *
 * <p>ClipboardAdapter defines an abstraction for reading and writing to the system clipboard.
 * It provides default no-op implementations, allowing implementations to selectively support
 * clipboard functionality based on the target platform.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Writing text content to the clipboard
 *   <li>Reporting whether clipboard access is supported on the platform
 * </ul>
 *
 * <p>Implementations that do not support clipboard functionality can leave methods unchanged,
 * and they will gracefully degrade to no-ops.
 */
public interface ClipboardAdapter {

  /**
   * Sets the text content of the system clipboard.
   *
   * <p>This is a no-op by default. Implementations that support clipboard access should override
   * this method to write the text to the system clipboard.
   *
   * @param text the text content to copy to the clipboard
   */
  default void setContents(String text) {
    // no-op by default
  }

  /**
   * Checks whether clipboard functionality is supported on this platform.
   *
   * <p>Returns false by default. Implementations that support clipboard access should override
   * this method to return true.
   *
   * @return true if clipboard operations are supported, false otherwise
   */
  default boolean isSupported() {
    return false;
  }
}
