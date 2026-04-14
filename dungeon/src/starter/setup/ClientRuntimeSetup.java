package starter.setup;

import contrib.debug.controls.DebugControlsSystem;
import contrib.debug.render.EntityDebugRenderSystem;
import contrib.debug.systems.DebugDrawSystem;
import contrib.editor.level.systems.LevelEditorSystem;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.levelHide.LevelHideSystem;
import core.System;
import core.game.ECSManagement;
import core.game.loop.ClientGameLoopHost;
import core.platform.Platform;

import java.util.function.Supplier;

/** Central setup helper for the interactive client runtime. */
public final class ClientRuntimeSetup {
  private ClientRuntimeSetup() {}

  /** Selects the default interactive client loop host. */
  public static void selectClientLoopHost() {
    Platform.loopHost(new ClientGameLoopHost());
  }

  /** Installs HUD systems if they are not already registered. */
  public static void installHudSystems() {
    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  /** Installs optional gameplay extension systems. */
  public static void installGameplayExtensions() {
    addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }

  /** Installs optional debug and editor systems. */
  public static void installDebugSystems() {
    addIfAbsent(DebugControlsSystem.class, DebugControlsSystem::new);
    addIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
    addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    addIfAbsent(EntityDebugRenderSystem.class, EntityDebugRenderSystem::new);
  }

  private static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
