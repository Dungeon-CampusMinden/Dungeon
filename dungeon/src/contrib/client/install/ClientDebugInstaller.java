package contrib.client.install;

import contrib.debug.systems.DebugDrawSystem;
import contrib.debug.systems.DebugEntityOverlaySystem;
import contrib.debug.systems.DebugGameplaySystem;
import contrib.debug.systems.DebugRenderEffectsSystem;
import core.game.systems.SystemRegistration;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * A specialized client loop host installer that registers debug systems for runtime testing and
 * development purposes.
 *
 * <p>The {@code ClientDebugInstaller} class contributes a set of systems enabling enhanced
 * debugging functionality, such as gameplay debugging, rendering effects debugging, debug drawing,
 * and entity rendering insights.
 *
 * <p>These systems are added to the client system execution pipeline if they are absent.
 *
 * <p>This class implements {@link ClientLoopHostInstaller} and overrides its {@link
 * #installRuntimeSystems()} method to register the following systems:
 *
 * <ul>
 *   <li>{@link DebugGameplaySystem}: Implements various debug functionalities for gameplay testing.
 *   <li>{@link DebugRenderEffectsSystem}: Provides debug support for rendering effects analysis.
 *   <li>{@link DebugDrawSystem}: Enables debug visualizations such as bounding boxes and debug
 *       overlays.
 *   <li>{@link DebugEntityOverlaySystem}: Offers detailed rendering insights into debug entities.
 * </ul>
 */
public final class ClientDebugInstaller implements ClientLoopHostInstaller {

  /** Creates a debug client installer. */
  public ClientDebugInstaller() {}

  @Override
  public void installRuntimeSystems() {
    SystemRegistration.addIfAbsent(DebugGameplaySystem.class, DebugGameplaySystem::new);
    SystemRegistration.addIfAbsent(DebugRenderEffectsSystem.class, DebugRenderEffectsSystem::new);
    SystemRegistration.addIfAbsent(DebugDrawSystem.class, DebugDrawSystem::new);
    SystemRegistration.addIfAbsent(DebugEntityOverlaySystem.class, DebugEntityOverlaySystem::new);
  }
}
