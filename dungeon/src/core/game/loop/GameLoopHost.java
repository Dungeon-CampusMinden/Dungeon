package core.game.loop;

import core.game.GameLoopCore;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.ui.StageHandle;
import java.util.Optional;

/**
 * Represents a platform-specific host for managing the game loop.
 *
 * <p>GameLoopHost provides the integration point between the engine-agnostic game loop
 * core and the platform- or framework-specific runtime environment.
 *
 * <p>It is responsible for initializing the game loop, managing platform-specific resources,
 * and optionally integrating UI and audio systems.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Managing the main game loop lifecycle by bridging {@link GameLoopCore} with the
 *   platform runtime.</li>
 *   <li>Providing optional access to a UI stage using {@link StageHandle}, if the platform
 *   includes UI support.</li>
 *   <li>Offering a platform-specific sound player via {@link ISoundPlayer}.</li>
 * </ul>
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

  /** Optional UI stage handle. */
  default Optional<StageHandle> stage() {
    return Optional.empty();
  }

  /** Host-provided sound player (defaults to no-audio). */
  default ISoundPlayer soundPlayer() {
    return new NoSoundPlayer();
  }
}
