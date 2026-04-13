package core.game.loop;

import core.game.GameLoopCore;
import core.sound.player.ISoundPlayer;
import core.ui.StageHandle;
import core.platform.litiengine.ui.LitiengineStageHandle;
import java.util.Optional;

/** Bridges the LITIENGINE host into the backend-agnostic {@link GameLoopHost} API. */
public final class ClientGameLoopHost implements GameLoopHost {

  @Override
  public ISoundPlayer soundPlayer() {
    return ClientLoopRuntime.soundPlayer();
  }

  @Override
  public void run(String[] args, GameLoopCore core) {
    ClientLoopRuntime.run(args, core);
  }

  @Override
  public Optional<StageHandle> stage() {
    return Optional.of(new LitiengineStageHandle());
  }
}
