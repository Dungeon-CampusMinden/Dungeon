package starter;

import contrib.hud.dialogs.GdxDialogFactoryBootstrap;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import core.System;
import core.game.ECSManagement;
import core.platform.Platform;
import core.platform.gdx.GdxRuntimeAdapter;
import core.platform.gdx.GdxWindowAdapter;
import java.util.function.Supplier;

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

    GdxDialogFactoryBootstrap.init();
  }

  public static void installHudSystems() {
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  private static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
