package core.platform;

import core.game.loop.GameLoopHost;
import core.resources.ClasspathResourcesAdapter;

import java.util.Objects;

/** Global access to platform backends (window, input, audio, ...). */
public final class Platform {
  private static WindowAdapter window = new NullWindowAdapter();
  private static RuntimeAdapter runtime = new NullRuntimeAdapter();
  private static ResourcesAdapter resources = new ClasspathResourcesAdapter();
  private static RenderAdapter render = new NullRenderAdapter();
  private static PathfindingAdapter pathfinding = new NullPathfindingAdapter();
  private static CursorAdapter cursor = new NullCursorAdapter();
  private static CameraAdapter camera = new NullCameraAdapter();
  private static ClipboardAdapter clipboard = new NullClipboardAdapter();
  private static volatile GameLoopHost loopHost;

  private Platform() {}

  public static WindowAdapter window() {
    return window;
  }

  public static void window(WindowAdapter adapter) {
    window = Objects.requireNonNull(adapter);
  }

  public static RuntimeAdapter runtime() {
    return runtime;
  }

  public static void runtime(RuntimeAdapter adapter) {
    runtime = Objects.requireNonNull(adapter);
  }

  public static ResourcesAdapter resources() {
    return resources;
  }

  public static void resources(ResourcesAdapter adapter) {
    resources = Objects.requireNonNull(adapter);
  }

  public static RenderAdapter render() {
    return render;
  }

  public static void render(RenderAdapter adapter) {
    render = Objects.requireNonNull(adapter);
  }

  public static PathfindingAdapter pathfinding() {
    return pathfinding;
  }

  public static void pathfinding(PathfindingAdapter adapter) {
    pathfinding = Objects.requireNonNull(adapter);
  }

  public static CursorAdapter cursor() {
    return cursor;
  }

  public static void cursor(CursorAdapter adapter) {
    cursor = Objects.requireNonNull(adapter);
  }

  public static CameraAdapter camera() {
    return camera;
  }

  public static void camera(CameraAdapter adapter) {
    camera = Objects.requireNonNull(adapter);
  }

  public static ClipboardAdapter clipboard() {
    return clipboard;
  }

  public static void clipboard(ClipboardAdapter adapter) {
    clipboard = Objects.requireNonNull(adapter);
  }

  public static void loopHost(GameLoopHost host) {
    loopHost = host;
  }

  public static GameLoopHost loopHost() {
    return loopHost;
  }
}
