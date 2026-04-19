package core.game.loop;

import core.game.GameLoop;
import core.platform.client.host.ClientEngineHost;
import core.sound.player.ISoundPlayer;
import core.ui.ClientStageHandle;
import core.ui.StageHandle;
import java.util.Optional;

/**
 * Client-specific implementation of the {@link GameLoopHost} interface.
 *
 * <p>This class provides the necessary client-side integration for the engine-agnostic
 * {@link GameLoop} by delegating to client-specific runtime implementations.
 *
 * <p>It facilitates the orchestration of the game loop, sound player, and UI stage
 * for the client environment.
 */
public final class ClientLoopHost implements GameLoopHost {

  @Override
  public ISoundPlayer soundPlayer() {
    return ClientEngineHost.soundPlayer();
  }

  @Override
  public void run(String[] args, GameLoop core) {
    ClientEngineHost.run(args, core);
  }

  @Override
  public Optional<StageHandle> stage() {
    return Optional.of(new ClientStageHandle());
  }
}
