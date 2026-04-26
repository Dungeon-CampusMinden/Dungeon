package core.game;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A centralized manager for window-related events, allowing different parts of the application to
 * register listeners for various window events without needing direct access to the windowing
 * system.
 *
 * <p>This class provides static methods to register listeners for events such as window creation,
 * file drops, focus changes, iconification, maximization, refresh requests, and close requests. It
 * also provides methods to fire these events, which will notify all registered listeners.
 *
 * <p>Listeners are stored in thread-safe {@link CopyOnWriteArrayList} collections to allow for
 * concurrent registration and event firing without the need for external synchronization.
 */
public final class WindowEventManager {

  /** Private constructor to prevent instantiation of this utility class. */
  private WindowEventManager() {}

  /** Listeners notified when the window is created. */
  private static final List<Consumer<Object>> WINDOW_CREATED = new CopyOnWriteArrayList<>();

  /** Listeners notified when files are dropped onto the window. */
  private static final List<Consumer<String[]>> FILES_DROPPED = new CopyOnWriteArrayList<>();

  /** Listeners notified when the window focus changes. */
  private static final List<Consumer<Boolean>> FOCUS_CHANGED = new CopyOnWriteArrayList<>();

  /** Listeners notified when the window is iconified or restored. */
  private static final List<Consumer<Boolean>> ICONIFIED = new CopyOnWriteArrayList<>();

  /** Listeners notified when the window is maximized or restored. */
  private static final List<Consumer<Boolean>> MAXIMIZED = new CopyOnWriteArrayList<>();

  /** Listeners notified when a window refresh is requested. */
  private static final List<Runnable> REFRESH_REQUESTED = new CopyOnWriteArrayList<>();

  /**
   * Listeners consulted when a close request is received. Each supplier returns {@code true} to
   * allow closing, or {@code false} to prevent it.
   */
  private static final List<BooleanSupplier> CLOSE_REQUESTED = new CopyOnWriteArrayList<>();

  /**
   * Registers a listener called when the window is created.
   *
   * @param l the listener to register; ignored if {@code null}
   */
  public static void registerWindowCreatedListener(Consumer<Object> l) {
    if (l != null) WINDOW_CREATED.add(l);
  }

  /**
   * Registers a listener called when files are dropped onto the window.
   *
   * @param l the listener to register; ignored if {@code null}
   */
  public static void registerFilesDroppedListener(Consumer<String[]> l) {
    if (l != null) FILES_DROPPED.add(l);
  }

  /**
   * Registers a listener called when the window focus changes.
   *
   * @param l the listener to register, receiving {@code true} if the window gained focus and {@code
   *     false} if it lost focus; ignored if {@code null}
   */
  public static void registerFocusChangedListener(Consumer<Boolean> l) {
    if (l != null) FOCUS_CHANGED.add(l);
  }

  /**
   * Registers a listener called when the window is iconified or restored.
   *
   * @param l the listener to register, receiving {@code true} when iconified and {@code false} when
   *     restored; ignored if {@code null}
   */
  public static void registerIconifiedListener(Consumer<Boolean> l) {
    if (l != null) ICONIFIED.add(l);
  }

  /**
   * Registers a listener called when the window is maximized or restored.
   *
   * @param l the listener to register, receiving {@code true} when maximized and {@code false} when
   *     restored; ignored if {@code null}
   */
  public static void registerMaximizedListener(Consumer<Boolean> l) {
    if (l != null) MAXIMIZED.add(l);
  }

  /**
   * Registers a listener called when a window refresh is requested.
   *
   * @param l the listener to register; ignored if {@code null}
   */
  public static void registerWindowRefreshListener(Runnable l) {
    if (l != null) REFRESH_REQUESTED.add(l);
  }

  /**
   * Registers a listener consulted when the window receives a close request.
   *
   * <p>The supplier should return {@code true} to allow the window to close, or {@code false} to
   * prevent it. If any registered supplier returns {@code false}, the close operation is canceled.
   *
   * @param l the listener to register; ignored if {@code null}
   */
  public static void registerCloseRequestListener(BooleanSupplier l) {
    if (l != null) CLOSE_REQUESTED.add(l);
  }

  /**
   * Fires the window-created event, notifying all registered listeners with the given window
   * object.
   *
   * @param window the window object that was created
   */
  public static void fireWindowCreated(Object window) {
    WINDOW_CREATED.forEach(c -> safe(() -> c.accept(window)));
  }

  /**
   * Fires the files-dropped event, notifying all registered listeners with the given file paths.
   *
   * @param files an array of absolute paths of the files that were dropped onto the window
   */
  public static void fireFilesDropped(String[] files) {
    FILES_DROPPED.forEach(c -> safe(() -> c.accept(files)));
  }

  /**
   * Fires the focus-changed event, notifying all registered listeners.
   *
   * @param focused {@code true} if the window gained focus, {@code false} if it lost focus
   */
  public static void fireFocusChanged(boolean focused) {
    FOCUS_CHANGED.forEach(c -> safe(() -> c.accept(focused)));
  }

  /**
   * Fires the iconified event, notifying all registered listeners.
   *
   * @param iconified {@code true} if the window was iconified, {@code false} if it was restored
   */
  public static void fireIconified(boolean iconified) {
    ICONIFIED.forEach(c -> safe(() -> c.accept(iconified)));
  }

  /**
   * Fires the maximized event, notifying all registered listeners.
   *
   * @param maximized {@code true} if the window was maximized, {@code false} if it was restored
   */
  public static void fireMaximized(boolean maximized) {
    MAXIMIZED.forEach(c -> safe(() -> c.accept(maximized)));
  }

  /** Fires the refresh-requested event, notifying all registered listeners. */
  public static void fireRefreshRequested() {
    REFRESH_REQUESTED.forEach(WindowEventManager::safe);
  }

  /**
   * Fires the close-requested event and collects the votes of all registered listeners.
   *
   * <p>Each listener is asked whether the window should be allowed to close. If all listeners
   * return {@code true}, this method returns {@code true}. If any listener returns {@code false},
   * the result is {@code false}. Exceptions thrown by individual listeners are silently ignored.
   *
   * @return {@code true} if all listeners agree to close the window, {@code false} otherwise
   */
  public static boolean fireCloseRequested() {
    boolean allow = true;
    for (BooleanSupplier s : CLOSE_REQUESTED) {
      try {
        allow &= s.getAsBoolean();
      } catch (Exception ignored) {
      }
    }
    return allow;
  }

  /**
   * Executes the given {@link Runnable} safely, suppressing any exceptions that may be thrown.
   *
   * @param r the runnable to execute
   */
  private static void safe(Runnable r) {
    try {
      r.run();
    } catch (Exception ignored) {
    }
  }
}
