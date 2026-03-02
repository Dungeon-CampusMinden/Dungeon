package core.platform;

import core.game.GameLoopCore;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.ui.StageHandle;
import java.util.Optional;

/**
 * Backend-specific host that drives the engine-agnostic {@link GameLoopCore}.
 *
 * <p>This is the last missing piece to make {@code core.game.GameLoop} independent
 * of a concrete engine implementation (libGDX, LITIENGINE, ...).
 */
public interface GameLoopHost {

  /**
   * Start the host loop and drive the given core loop.
   *
   * @param args optional command-line args (some engines need them for init)
   * @param core the engine-agnostic core loop
   */
  void run(String[] args, GameLoopCore core);

  /** Convenience overload for hosts that don't need args. */
  default void run(GameLoopCore core) {
    run(new String[0], core);
  }

  /** Optional UI stage handle (e.g. libGDX Stage). */
  default Optional<StageHandle> stage() {
    return Optional.empty();
  }

  /** Host-provided sound player (defaults to no-audio). */
  default ISoundPlayer soundPlayer() {
    return new NoSoundPlayer();
  }
}
