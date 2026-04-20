package contrib.client;

import contrib.modules.levelhide.LevelHideSystem;
import core.game.loop.ClientLoopHostInstaller;

/**
 * Installs gameplay-related runtime systems into the client loop host.
 *
 * <p>This class provides an implementation for the {@link ClientLoopHostInstaller} interface,
 * allowing for the installation of gameplay-specific systems during the client initialization
 * process. Specifically, it ensures that essential runtime systems for managing gameplay
 * functionality are added to the client if they are not already present.
 *
 * <p>The primary responsibility of this installer is to contribute the {@link LevelHideSystem},
 * which maintains the runtime state of hidden or revealed world regions based on the player’s
 * position. This system interacts with other ECS components to dynamically manage visibility
 * states within the game world.
 *
 * <p>This installer is typically registered as part of the client startup process through
 * mechanisms like {@link java.util.ServiceLoader} or by explicitly passing it to a
 * ClientLoopHost constructor.
 */
public final class GameplayClientInstaller implements ClientLoopHostInstaller {

  /** Creates a contrib gameplay client installer. */
  public GameplayClientInstaller() {}

  @Override
  public void installRuntimeSystems() {
    SystemClientInstaller.addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }
}
