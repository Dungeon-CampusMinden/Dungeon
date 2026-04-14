package starter.setup;

import contrib.debug.systems.DebugRenderEffectsSystem;
import contrib.debug.systems.EntityDebugRenderSystem;
import contrib.debug.systems.DebugDrawSystem;
import contrib.editor.level.systems.LevelEditorSystem;
import contrib.hud.dialogs.DialogBackendInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.levelHide.LevelHideSystem;
import core.System;
import core.game.ECSManagement;
import java.util.function.Supplier;

/**
 * Provides utility methods to set up the runtime environment for client-side systems,
 * including HUD systems, gameplay extensions, and debug systems.
 * This class cannot be instantiated.
 */
public final class ClientRuntimeSetup {
  private ClientRuntimeSetup() {}

  /** Installs HUD systems if they are not already registered. */
  public static void installHudSystems() {
    DialogBackendInstaller.install();

    addIfAbsent(HudSystem.class, HudSystem::new);
    addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }

  /** Installs optional gameplay extension systems. */
  public static void installGameplayExtensions() {
    addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }

  /** Installs optional debug and editor systems. */
  public static void installDebugSystems() {
    addIfAbsent(DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
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
