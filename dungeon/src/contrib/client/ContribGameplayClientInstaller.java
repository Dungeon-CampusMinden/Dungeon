package contrib.client;

import contrib.modules.levelhide.LevelHideSystem;
import core.game.loop.ClientLoopHostInstaller;

/** Installs optional contrib gameplay systems for the client loop host. */
public final class ContribGameplayClientInstaller implements ClientLoopHostInstaller {

  /** Creates a contrib gameplay client installer. */
  public ContribGameplayClientInstaller() {}

  @Override
  public void installRuntimeSystems() {
    ContribClientSystemInstaller.addIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
  }
}
