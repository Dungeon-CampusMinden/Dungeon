package contrib.client;

import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityRenderSystem;
import contrib.debug.systems.DebugGameplaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import contrib.editor.level.LevelEditorSystem;
import core.game.loop.ClientLoopHostInstaller;

/** Installs contrib debug and editor systems for the client loop host. */
public final class ContribDebugClientInstaller implements ClientLoopHostInstaller {

  /** Creates a contrib debug client installer. */
  public ContribDebugClientInstaller() {}

  @Override
  public void installRuntimeSystems() {
    ContribClientSystemInstaller.addIfAbsent(DebugGameplaySystem.class, DebugGameplaySystem::new);
    ContribClientSystemInstaller.addIfAbsent(
        DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    ContribClientSystemInstaller.addIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
    ContribClientSystemInstaller.addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    ContribClientSystemInstaller.addIfAbsent(
        DebugEntityRenderSystem.class, DebugEntityRenderSystem::new);
  }
}
