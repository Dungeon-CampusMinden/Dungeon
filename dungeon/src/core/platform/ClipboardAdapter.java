package core.platform;

/** Backend-specific clipboard access. */
public interface ClipboardAdapter {

  /**
   * Copies the given text into the clipboard if the active backend supports it.
   *
   * @param text text to copy
   */
  default void setContents(String text) {
    // no-op by default
  }

  /**
   * @return true if clipboard access is supported by the active backend
   */
  default boolean isSupported() {
    return false;
  }
}
