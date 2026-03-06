package starter;

import core.platform.Platform;
import core.platform.gdx.GdxRuntimeAdapter;
import core.platform.gdx.GdxWindowAdapter;

/** Explicitly wires the libGDX backend into the platform abstraction. */
public final class GdxPlatformBootstrap {
  private GdxPlatformBootstrap() {}

  public static void init() {
    Platform.window(new GdxWindowAdapter());
    Platform.runtime(new GdxRuntimeAdapter());
    Platform.resources(new core.platform.gdx.GdxResourcesAdapter());
    Platform.render(new core.platform.gdx.GdxRenderAdapter());
    Platform.pathfinding(new core.platform.gdx.GdxPathfindingAdapter());
    Platform.cursor(new core.platform.gdx.input.GdxCursorAdapter());
  }
}
