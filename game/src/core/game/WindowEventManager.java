package core.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages window event listeners for the game window.
 *
 * <p>This manager provides a centralized way to register, remove, and handle various window events
 * like creation, iconification, maximization, focus changes, close requests, file drops, and
 * refresh requests. It acts as a wrapper around the LibGDX window listener system.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Listen for focus changes
 * WindowEventManager.registerFocusChangeListener(focus -> System.out.println("Focus: " + focus));
 *
 * // Prevent window from closing
 * WindowEventManager.registerCloseRequestListener(() -> false);
 *
 * // Handle window creation
 * WindowEventManager.registerWindowCreatedListener(
 *     window -> System.out.println("Window created: " + window));
 *
 * // Track window minimization state
 * WindowEventManager.registerIconificationListener(
 *     iconified -> System.out.println("Window iconified: " + iconified));
 *
 * // Track window maximization state
 * WindowEventManager.registerMaximizationListener(
 *     maximized -> System.out.println("Window maximized: " + maximized));
 * }</pre>
 */
public class WindowEventManager {

  private static final List<WindowCreatedListener> windowCreatedListeners = new Vector<>();
  private static final List<Consumer<Boolean>> windowIconifiedListeners = new Vector<>();
  private static final List<Consumer<Boolean>> windowMaximizedListeners = new Vector<>();
  private static final List<Consumer<Boolean>> windowFocusListeners = new Vector<>();
  private static final List<Supplier<Boolean>> closeRequestedListeners = new Vector<>();
  private static final List<WindowFilesDroppedListener> filesDroppedListeners = new Vector<>();
  private static final List<Runnable> windowRefreshListeners = new Vector<>();

  private static final Lwjgl3WindowListener windowListener = createWindowListener();

  /** Interface for window creation event listeners. */
  @FunctionalInterface
  public interface WindowCreatedListener {
    /**
     * Called when a window is created.
     *
     * @param window The newly created window instance
     */
    void onWindowCreated(Lwjgl3Window window);
  }

  /** Interface for file drop event listeners. */
  @FunctionalInterface
  public interface WindowFilesDroppedListener {
    /**
     * Called when files are dropped onto the window.
     *
     * @param filePaths Array of file paths that were dropped onto the window
     */
    void onFilesDropped(String[] filePaths);
  }

  // Private constructor to prevent instantiation
  private WindowEventManager() {}

  /**
   * Registers a listener for window creation events.
   *
   * @param listener The listener to be called when a new window is created
   */
  public static void registerWindowCreatedListener(WindowCreatedListener listener) {
    register(windowCreatedListeners, listener);
  }

  /**
   * Registers a listener for window iconification events.
   *
   * @param listener The listener to be called when the window is iconified or restored (receives
   *     true when iconified, false when restored)
   */
  public static void registerIconificationListener(Consumer<Boolean> listener) {
    register(windowIconifiedListeners, listener);
  }

  /**
   * Registers a listener for window maximization events.
   *
   * @param listener The listener to be called when the window is maximized or restored (receives
   *     true when maximized, false when restored)
   */
  public static void registerMaximizationListener(Consumer<Boolean> listener) {
    register(windowMaximizedListeners, listener);
  }

  /**
   * Registers a listener for window focus change events.
   *
   * @param listener The listener to be called when the window gains or loses focus (receives true
   *     when focus is gained, false when focus is lost)
   */
  public static void registerFocusChangeListener(Consumer<Boolean> listener) {
    register(windowFocusListeners, listener);
  }

  /**
   * Registers a listener for window close request events.
   *
   * @param listener The listener to be called when window closure is requested (should return true
   *     to allow the window to close, false to prevent closure)
   */
  public static void registerCloseRequestListener(Supplier<Boolean> listener) {
    register(closeRequestedListeners, listener);
  }

  /**
   * Registers a listener for files dropped onto the window.
   *
   * @param listener The listener to be called when files are dropped onto the window
   */
  public static void registerFilesDroppedListener(WindowFilesDroppedListener listener) {
    register(filesDroppedListeners, listener);
  }

  /**
   * Registers a listener for window refresh requests.
   *
   * @param listener The listener to be called when a window refresh is requested
   */
  public static void registerWindowRefreshListener(Runnable listener) {
    register(windowRefreshListeners, listener);
  }

  private static <T> void register(List<T> list, T listener) {
    if (listener != null) list.add(listener);
  }

  /**
   * Removes a window creation event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterWindowCreatedListener(WindowCreatedListener listener) {
    return windowCreatedListeners.remove(listener);
  }

  /**
   * Removes a window iconification event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterIconificationListener(Consumer<Boolean> listener) {
    return windowIconifiedListeners.remove(listener);
  }

  /**
   * Removes a window maximization event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterMaximizationListener(Consumer<Boolean> listener) {
    return windowMaximizedListeners.remove(listener);
  }

  /**
   * Removes a window focus change event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterFocusChangeListener(Consumer<Boolean> listener) {
    return windowFocusListeners.remove(listener);
  }

  /**
   * Removes a window close request event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterCloseRequestListener(Supplier<Boolean> listener) {
    return closeRequestedListeners.remove(listener);
  }

  /**
   * Removes a files dropped event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterFilesDroppedListener(WindowFilesDroppedListener listener) {
    return filesDroppedListeners.remove(listener);
  }

  /**
   * Removes a window refresh request event listener.
   *
   * @param listener The listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public static boolean unregisterWindowRefreshListener(Runnable listener) {
    return windowRefreshListeners.remove(listener);
  }

  /**
   * Clears all registered window event listeners. This should be called when shutting down the
   * application or when window event handling is no longer needed.
   */
  public static void clearAllListeners() {
    windowCreatedListeners.clear();
    windowIconifiedListeners.clear();
    windowMaximizedListeners.clear();
    windowFocusListeners.clear();
    closeRequestedListeners.clear();
    filesDroppedListeners.clear();
    windowRefreshListeners.clear();
  }

  /**
   * Gets the window listener implementation that handles all registered event listeners. This
   * method is intended to be used by the window creation system.
   *
   * @return The window listener implementation
   */
  public static Lwjgl3WindowListener windowListener() {
    return windowListener;
  }

  /**
   * Creates a window listener implementation that dispatches events to all registered listeners.
   *
   * @return A window listener implementation
   */
  private static Lwjgl3WindowListener createWindowListener() {
    return new Lwjgl3WindowListener() {
      @Override
      public void created(Lwjgl3Window window) {
        windowCreatedListeners.forEach(listener -> listener.onWindowCreated(window));
      }

      @Override
      public void iconified(boolean isIconified) {
        windowIconifiedListeners.forEach(listener -> listener.accept(isIconified));
      }

      @Override
      public void maximized(boolean isMaximized) {
        windowMaximizedListeners.forEach(listener -> listener.accept(isMaximized));
      }

      @Override
      public void focusLost() {
        windowFocusListeners.forEach(listener -> listener.accept(false));
      }

      @Override
      public void focusGained() {
        windowFocusListeners.forEach(listener -> listener.accept(true));
      }

      @Override
      public boolean closeRequested() {
        return closeRequestedListeners.stream()
            .allMatch(Supplier::get); // Check if all listeners approve the close request
      }

      @Override
      public void filesDropped(String[] filePaths) {
        filesDroppedListeners.forEach(listener -> listener.onFilesDropped(filePaths));
      }

      @Override
      public void refreshRequested() {
        windowRefreshListeners.forEach(Runnable::run);
      }
    };
  }
}
