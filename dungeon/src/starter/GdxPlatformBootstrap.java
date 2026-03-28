package starter;

import contrib.hud.dialogs.GdxDialogFactoryBootstrap;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import core.System;
import core.game.ECSManagement;
import core.platform.Platform;
import core.platform.gdx.*;
import core.platform.gdx.input.GdxCursorAdapter;
import core.platform.gdx.systems.DebugDrawSystem;
import core.platform.gdx.systems.GdxCameraSystem;

import java.util.function.Supplier;

/** Explicitly wires the libGDX backend into the platform abstraction. */
public final class GdxPlatformBootstrap {
  private GdxPlatformBootstrap() {}

  public static void init() {
    Platform.window(new GdxWindowAdapter());
    Platform.runtime(new GdxRuntimeAdapter());
    Platform.resources(new GdxResourcesAdapter());
    Platform.render(new GdxRenderAdapter());
    Platform.pathfinding(new GdxPathfindingAdapter());
    Platform.cursor(new GdxCursorAdapter());
    Platform.camera(new GdxCameraAdapter());
    Platform.loopHost(new GdxLoopHost());

    GdxDialogFactoryBootstrap.init();
  }

  public static void installHudSystems() {
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  /**
   * Installs libGDX-specific client systems that are intentionally not part of the
   * engine-agnostic core bootstrap.
   */
  public static void installClientSystems() {
    addIfAbsent(GdxCameraSystem.class, GdxCameraSystem::new);
    addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    installHudSystems();
  }

  private static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
