package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.window.GameWindow;

/**
 * Utility class for thread-related operations.
 * Provides methods to check if the current thread is the main thread.
 */
public class ThreadUtils {

  private ThreadUtils() {}

  /**
   * Checks if the current thread is the main thread.
   * Throws an IllegalStateException if the current thread is not the main thread.
   *
   * @throws IllegalStateException if the current thread is not the main thread
   */
  public static void checkMainThread() {
    if (!isMainThread()) {
      throw new IllegalStateException(
          "This method can only be called from the main thread (aka. Render-/Input-Thread)");
    }
  }

  /**
   * Determines if the current thread is the main thread.
   *
   * @return true if the current thread is the main thread, false otherwise
   */
  public static boolean isMainThread() {
    return Thread.currentThread() == GameWindow.MAIN_THREAD;
  }

}
