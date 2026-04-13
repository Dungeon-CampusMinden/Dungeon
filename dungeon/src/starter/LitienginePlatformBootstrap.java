package starter;

import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.levelHide.LevelHideSystem;
import core.System;
import core.game.ECSManagement;
import core.resources.CompositeResourcesAdapter;
import core.platform.Platform;
import core.platform.awt.AwtClipboardAdapter;
import core.resources.ClasspathResourcesAdapter;
import core.resources.FileSystemResourcesAdapter;
import core.level.path.GridPathfindingAdapter;
import contrib.hud.dialogs.DialogFactoryBootstrap;
import core.game.loop.LitiengineLoopHost;
import contrib.modules.interaction.ui.OverlayInteractionSelectionUi;
import contrib.debug.systems.LitiengineDebugControlsSystem;
import contrib.debug.systems.LitiengineDebugDrawSystem;
import contrib.debug.systems.LitiengineEntityDebugSystem;
import contrib.editor.level.systems.LitiengineLevelEditorSystem;
import java.util.function.Supplier;

/** Explicitly wires LITIENGINE-specific startup steps into the platform abstraction. */
public final class LitienginePlatformBootstrap {
  private LitienginePlatformBootstrap() {}

  public static void init() {
    Platform.resources(
      new CompositeResourcesAdapter(
        new ClasspathResourcesAdapter(),
        FileSystemResourcesAdapter.autoDetect()));
    Platform.pathfinding(new GridPathfindingAdapter());
    Platform.clipboard(new AwtClipboardAdapter());
    Platform.loopHost(new LitiengineLoopHost());

    // Register a temporary non-Scene2D dialog backend so dialog creation
    // already works on the LITIENGINE path.
    DialogFactoryBootstrap.init();
    InteractionSelection.install(OverlayInteractionSelectionUi.INSTANCE);
  }

  public static void installHudSystems() {
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  public static void installGameplayExtensions() {
    addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }

  /** Installs the LITIENGINE debugger controls if they are not already present. */
  public static void installDebugger() {
    addIfAbsent(LitiengineDebugControlsSystem.class, LitiengineDebugControlsSystem::new);
    addIfAbsent(LitiengineLevelEditorSystem.class, LitiengineLevelEditorSystem::new);
    addIfAbsent(LitiengineDebugDrawSystem.class, LitiengineDebugDrawSystem::new);
    addIfAbsent(LitiengineEntityDebugSystem.class, LitiengineEntityDebugSystem::new);
  }

  private static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
