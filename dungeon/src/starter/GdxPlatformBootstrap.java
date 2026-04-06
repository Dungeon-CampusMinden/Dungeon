package starter;

import contrib.modules.interaction.InteractionSelection;
import contrib.platform.gdx.hud.dialogs.GdxDialogFactoryBootstrap;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.platform.gdx.interaction.GdxInteractionSelectionUi;
import core.System;
import core.game.ECSManagement;
import core.platform.Platform;
import core.platform.awt.AwtClipboardAdapter;
import core.platform.gdx.GdxCameraAdapter;
import core.platform.gdx.GdxLoopHost;
import core.platform.gdx.GdxRenderAdapter;
import core.platform.gdx.GdxResourcesAdapter;
import core.platform.gdx.GdxRuntimeAdapter;
import core.platform.gdx.GdxWindowAdapter;
import core.platform.gdx.input.GdxCursorAdapter;
import core.platform.gdx.systems.DebugDrawSystem;
import core.platform.gdx.systems.Debugger;
import core.platform.gdx.systems.GdxCameraSystem;
import core.platform.grid.GridPathfindingAdapter;
import java.util.function.Supplier;

/** Explicitly wires the libGDX backend into the platform abstraction. */
public final class GdxPlatformBootstrap {
  private GdxPlatformBootstrap() {}

  public static void init() {
    Platform.window(new GdxWindowAdapter());
    Platform.runtime(new GdxRuntimeAdapter());
    Platform.resources(new GdxResourcesAdapter());
    Platform.render(new GdxRenderAdapter());
    Platform.pathfinding(new GridPathfindingAdapter());
    Platform.cursor(new GdxCursorAdapter());
    Platform.camera(new GdxCameraAdapter());
    Platform.clipboard(new AwtClipboardAdapter());
    Platform.loopHost(new GdxLoopHost());
    GdxDialogFactoryBootstrap.init();
    InteractionSelection.install(GdxInteractionSelectionUi.INSTANCE);
  }

  public static void installHudSystems() {
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  /** Installs the libGDX debugger if it is not already present. */
  public static void installDebugger() {
    addIfAbsent(Debugger.class, Debugger::new);
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
