package core.platform.litiengine;

import core.game.GameLoopCore;
import core.game.litiengine.LitiengineGameLoopHost;
import core.platform.GameLoopHost;
import core.sound.player.ISoundPlayer;
import core.ui.StageHandle;
import core.platform.litiengine.ui.LitiengineStageHandle;
import java.util.Optional;

/** Bridges the LITIENGINE host into the backend-agnostic {@link GameLoopHost} API. */
public final class LitiengineLoopHost implements GameLoopHost {

  @Override
  public ISoundPlayer soundPlayer() {
    return LitiengineGameLoopHost.soundPlayer();
  }

  @Override
  public void run(String[] args, GameLoopCore core) {
    LitiengineGameLoopHost.run(args, core);
  }

  @Override
  public Optional<StageHandle> stage() {
    return Optional.of(new LitiengineStageHandle());
  }
}
