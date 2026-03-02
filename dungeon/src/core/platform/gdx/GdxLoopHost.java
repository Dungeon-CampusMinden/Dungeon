package core.platform.gdx;

import core.game.GameLoopCore;
import core.game.gdx.GdxGameLoopHost;
import core.platform.GameLoopHost;
import core.sound.player.ISoundPlayer;
import core.ui.StageHandle;
import java.util.Optional;

/** Bridges the libGDX host into the backend-agnostic {@link GameLoopHost} API. */
public final class GdxLoopHost implements GameLoopHost {

  @Override
  public void run(String[] args, GameLoopCore core) {
    // args are irrelevant for libGDX host
    GdxGameLoopHost.run(core);
  }

  @Override
  public Optional<StageHandle> stage() {
    return GdxGameLoopHost.stage();
  }

  @Override
  public ISoundPlayer soundPlayer() {
    return GdxGameLoopHost.soundPlayer();
  }
}
