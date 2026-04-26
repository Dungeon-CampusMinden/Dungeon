package contrib.client.install;

import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityRenderSystem;
import contrib.debug.systems.DebugGameplaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import core.game.systems.SystemRegistration;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * A specialized client loop host installer that registers debug systems for runtime testing
 * and development purposes.
 *
 * <p>The {@code DebugClientInstaller} class contributes a set of systems enabling enhanced
 * debugging functionality, such as gameplay debugging, rendering effects debugging,
 * debug drawing, and entity rendering insights.
 *
 * <p>These systems are added to the client system execution pipeline if they are absent.
 *
 * <p>This class implements {@link ClientLoopHostInstaller} and overrides its
 * {@link #installRuntimeSystems()} method to register the following systems:
 *
 * <ul>
 *   <li>{@link DebugGameplaySystem}: Implements various debug functionalities for gameplay testing.</li>
 *   <li>{@link DebugRenderEffectsSystem}: Provides debug support for rendering effects analysis.</li>
 *   <li>{@link DebugDrawSystem}: Enables debug visualizations such as bounding boxes and debug overlays.</li>
 *   <li>{@link DebugEntityRenderSystem}: Offers detailed rendering insights into debug entities.</li>
 * </ul>
 */
public final class DebugClientInstaller implements ClientLoopHostInstaller {

  /** Creates a debug client installer. */
  public DebugClientInstaller() {}

  @Override
  public void installRuntimeSystems() {
    SystemRegistration.addIfAbsent(DebugGameplaySystem.class, DebugGameplaySystem::new);
    SystemRegistration.addIfAbsent(DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    SystemRegistration.addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    SystemRegistration.addIfAbsent(DebugEntityRenderSystem.class, DebugEntityRenderSystem::new);
  }
}
