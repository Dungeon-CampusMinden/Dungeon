package core.platform;

import java.util.Objects;

/** Global access to platform backends (window, input, audio, ...). */
public final class Platform {
  private static WindowAdapter window = new NullWindowAdapter();
  private static RuntimeAdapter runtime = new NullRuntimeAdapter();
  private static ResourcesAdapter resources = new core.platform.classpath.ClasspathResourcesAdapter();
  private static RenderAdapter render = new NullRenderAdapter();
  private static PathfindingAdapter pathfinding = new NullPathfindingAdapter();
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
    resources = java.util.Objects.requireNonNull(adapter);
  }

  public static RenderAdapter render() { // NEW
    return render;
  }

  public static void render(RenderAdapter adapter) { // NEW
    render = Objects.requireNonNull(adapter);
  }

  public static PathfindingAdapter pathfinding() {
    return pathfinding;
  }

  public static void pathfinding(PathfindingAdapter adapter) {
    pathfinding = Objects.requireNonNull(adapter);
  }

  public static void loopHost(GameLoopHost host) {
    loopHost = host;
  }

  public static GameLoopHost loopHost() {
    return loopHost;
  }
}
