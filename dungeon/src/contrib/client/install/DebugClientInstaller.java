package contrib.client.install;

import contrib.client.DefaultClientLoopHostFactory;
import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityRenderSystem;
import contrib.debug.systems.DebugGameplaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import contrib.editor.level.LevelEditorSystem;
import core.game.loop.ClientLoopHostInstaller;

/**
 * A specialized client loop host installer that registers debug systems for runtime testing
 * and development purposes.
 *
 * <p>The {@code DebugClientInstaller} class contributes a set of systems enabling enhanced
 * debugging functionality, such as gameplay debugging, rendering effects debugging, level editing,
 * debug drawing, and entity rendering insights. These systems are added to the client system
 * execution pipeline if they are absent.
 *
 * <p>This class implements {@link ClientLoopHostInstaller} and overrides its
 * {@link #installRuntimeSystems()} method to register the following systems:
 *
 * <ul>
 *   <li>{@link DebugGameplaySystem}: Implements various debug functionalities for gameplay testing.</li>
 *   <li>{@link DebugRenderEffectsSystem}: Provides debug support for rendering effects analysis.</li>
 *   <li>{@link LevelEditorSystem}: Adds support for runtime level editing and testing.</li>
 *   <li>{@link DebugDrawSystem}: Enables debug visualizations such as bounding boxes and debug overlays.</li>
 *   <li>{@link DebugEntityRenderSystem}: Offers detailed rendering insights into debug entities.</li>
 * </ul>
 *
 * <p>This installer is registered explicitly through {@link DefaultClientLoopHostFactory}.
 */
public final class DebugClientInstaller implements ClientLoopHostInstaller {

  /** Creates a debug client installer. */
  public DebugClientInstaller() {}

  @Override
  public void installRuntimeSystems() {
    ClientLoopHostInstaller.addSystemIfAbsent(DebugGameplaySystem.class, DebugGameplaySystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(
      DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(
      DebugEntityRenderSystem.class, DebugEntityRenderSystem::new);
  }
}
