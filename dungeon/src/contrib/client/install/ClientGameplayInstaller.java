package contrib.client.install;

import contrib.client.render.LayeredRenderAdapter;
import contrib.game.LevelContentInstaller;
import contrib.modules.levelhide.LevelHideSystem;
import contrib.systems.ColliderSyncSystem;
import core.game.systems.SystemRegistration;
import core.platform.Platform;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * Installs client-side gameplay systems and services into the client loop host.
 *
 * <p>This class provides implementations for the {@link ClientLoopHostInstaller} interface,
 * focusing on gameplay and level-related systems. It handles the installation of level content,
 * rendering adapters, and world-state synchronization systems.
 *
 * <p>{@code ClientGameplayInstaller} installs the following components:
 *
 * <ul>
 *   <li>{@link LevelContentInstaller}: Sets up level content resources and assets.
 *   <li>{@link LayeredRenderAdapter}: Wraps the existing render adapter to provide
 *       presentation-specific rendering capabilities.
 *   <li>{@link ColliderSyncSystem}: Synchronizes collider states between client and server.
 *   <li>{@link LevelHideSystem}: Maintains the client-side visibility state of hidden or revealed
 *       world regions.
 * </ul>
 */
public final class ClientGameplayInstaller implements ClientLoopHostInstaller {

  /** Creates a gameplay client installer. */
  public ClientGameplayInstaller() {}

  @Override
  public void installPlatformServices() {
    LevelContentInstaller.install();
    if (!(Platform.render() instanceof LayeredRenderAdapter)) {
      Platform.render(new LayeredRenderAdapter(Platform.render()));
    }
  }

  @Override
  public void installRuntimeSystems() {
    SystemRegistration.addIfAbsent(ColliderSyncSystem.class, ColliderSyncSystem::new);
    SystemRegistration.addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }
}
