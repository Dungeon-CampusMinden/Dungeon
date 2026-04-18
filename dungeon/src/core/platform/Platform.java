package core.platform;

import core.game.loop.GameLoopHost;
import core.level.path.GridPathfindingAdapter;
import core.platform.defaults.*;
import core.resources.ClasspathResourcesAdapter;

import java.util.Objects;

/**
 * Central platform abstraction service locator.
 *
 * <p>Platform provides a centralized registry for accessing platform-specific adapters that
 * implement core functionality. It uses the Service Locator pattern to decouple the core game
 * engine from specific platform implementations (e.g., LibGDX, Swing, headless).
 *
 * <p>Managed adapters:
 * <ul>
 *   <li>WindowAdapter - Window and display management
 *   <li>RuntimeAdapter - Runtime/system operations
 *   <li>ResourcesAdapter - Resource loading from classpath or other sources
 *   <li>RenderAdapter - Graphics rendering
 *   <li>PathfindingAdapter - Pathfinding algorithms
 *   <li>CursorAdapter - Mouse cursor control
 *   <li>CameraAdapter - Camera operations
 *   <li>ClipboardAdapter - Clipboard access
 *   <li>GameLoopHost - Main game loop management
 * </ul>
 *
 * <p>Default implementations are provided via null-object adapters to ensure graceful degradation
 * in headless or incomplete environments. All setters require non-null values.
 *
 * <p>This class is not instantiable; all members are static.
 */
public final class Platform {
  private static WindowAdapter window = new NullWindowAdapter();
  private static RuntimeAdapter runtime = new NullRuntimeAdapter();
  private static ResourcesAdapter resources = new ClasspathResourcesAdapter();
  private static RenderAdapter render = new NullRenderAdapter();
  private static PathfindingAdapter pathfinding = new GridPathfindingAdapter();
  private static CursorAdapter cursor = new NullCursorAdapter();
  private static CameraAdapter camera = new NullCameraAdapter();
  private static ClipboardAdapter clipboard = new NullClipboardAdapter();
  private static volatile GameLoopHost loopHost;

  private Platform() {}

  /**
   * Gets the window adapter for display and window management.
   *
   * @return the current WindowAdapter implementation
   */
  public static WindowAdapter window() {
    return window;
  }

  /**
   * Sets the window adapter for display and window management.
   *
   * @param adapter the WindowAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void window(WindowAdapter adapter) {
    window = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the runtime adapter for system and runtime operations.
   *
   * @return the current RuntimeAdapter implementation
   */
  public static RuntimeAdapter runtime() {
    return runtime;
  }

  /**
   * Sets the runtime adapter for system and runtime operations.
   *
   * @param adapter the RuntimeAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void runtime(RuntimeAdapter adapter) {
    runtime = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the resource adapter for loading resources from classpath or other sources.
   *
   * @return the current ResourcesAdapter implementation
   */
  public static ResourcesAdapter resources() {
    return resources;
  }

  /**
   * Sets the resource adapter for loading resources from classpath or other sources.
   *
   * @param adapter the ResourcesAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void resources(ResourcesAdapter adapter) {
    resources = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the render adapter for graphics rendering operations.
   *
   * @return the current RenderAdapter implementation
   */
  public static RenderAdapter render() {
    return render;
  }

  /**
   * Sets the render adapter for graphics rendering operations.
   *
   * @param adapter the RenderAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void render(RenderAdapter adapter) {
    render = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the pathfinding adapter for pathfinding algorithm implementations.
   *
   * @return the current PathfindingAdapter implementation
   */
  public static PathfindingAdapter pathfinding() {
    return pathfinding;
  }

  /**
   * Sets the pathfinding adapter for pathfinding algorithm implementations.
   *
   * @param adapter the PathfindingAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void pathfinding(PathfindingAdapter adapter) {
    pathfinding = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the cursor adapter for mouse cursor control.
   *
   * @return the current CursorAdapter implementation
   */
  public static CursorAdapter cursor() {
    return cursor;
  }

  /**
   * Sets the cursor adapter for mouse cursor control.
   *
   * @param adapter the CursorAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void cursor(CursorAdapter adapter) {
    cursor = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the camera adapter for camera operations.
   *
   * @return the current CameraAdapter implementation
   */
  public static CameraAdapter camera() {
    return camera;
  }

  /**
   * Sets the camera adapter for camera operations.
   *
   * @param adapter the CameraAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void camera(CameraAdapter adapter) {
    camera = Objects.requireNonNull(adapter);
  }

  /**
   * Gets the clipboard adapter for clipboard access operations.
   *
   * @return the current ClipboardAdapter implementation
   */
  public static ClipboardAdapter clipboard() {
    return clipboard;
  }

  /**
   * Sets the clipboard adapter for clipboard access operations.
   *
   * @param adapter the ClipboardAdapter implementation (must not be null)
   * @throws NullPointerException if the adapter is null
   */
  public static void clipboard(ClipboardAdapter adapter) {
    clipboard = Objects.requireNonNull(adapter);
  }

  /**
   * Sets the game loop host for main game loop management.
   *
   * <p>This setter allows null values, in contrast to other setters in this class.
   *
   * @param host the GameLoopHost implementation, or null to clear the current host
   */
  public static void loopHost(GameLoopHost host) {
    loopHost = host;
  }

  /**
   * Gets the game loop host for main game loop management.
   *
   * @return the current GameLoopHost implementation, or null if not set
   */
  public static GameLoopHost loopHost() {
    return loopHost;
  }
}
