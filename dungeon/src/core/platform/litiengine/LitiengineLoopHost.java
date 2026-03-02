package core.platform.litiengine;

import core.game.GameLoopCore;
import core.game.litiengine.LitiengineGameLoopHost;
import core.platform.GameLoopHost;

/** Bridges the LITIENGINE host into the backend-agnostic {@link GameLoopHost} API. */
public final class LitiengineLoopHost implements GameLoopHost {

  @Override
  public void run(String[] args, GameLoopCore core) {
    LitiengineGameLoopHost.run(args, core);
  }
}
