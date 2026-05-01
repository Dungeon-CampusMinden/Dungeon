package core.game.loop;

import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.ui.StageHandle;
import java.util.Optional;

/**
 * Represents a platform-specific host for managing the game loop.
 *
 * <p>GameLoopHost provides the integration point between the engine-agnostic game loop core and the
 * platform- or framework-specific runtime environment.
 *
 * <p>It is responsible for initializing the game loop, managing platform-specific resources, and
 * optionally integrating UI and audio systems.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Managing the main game loop lifecycle by bridging {@link GameLoop} with the platform
 *       runtime.
 *   <li>Providing optional access to a UI stage using {@link StageHandle}, if the platform includes
 *       UI support.
 *   <li>Offering a platform-specific sound player via {@link ISoundPlayer}.
 * </ul>
 */
public interface GameLoopHost {

  /**
   * Starts the host loop and drives the given game loop.
   *
   * <p>This method initiates the platform-specific runtime loop and passes control to the provided
   * engine-agnostic game loop.
   *
   * <p>The host remains responsible for managing platform resources and
   * lifecycle events while delegating the core game logic to the provided {@link GameLoop}.
   *
   * @param args optional command-line arguments that may be required by the platform runtime
   * @param gameLoop the engine-agnostic game loop that drives the core game logic
   */
  void run(String[] args, GameLoop gameLoop);

  /**
   * Retrieves an optional handle to a UI stage.
   *
   * <p>This method provides access to a {@link StageHandle} if the platform or runtime environment
   * supports a UI stage.
   *
   * <p>The returned {@link Optional} will be empty if no stage is available, implying that the
   * platform does not include UI support or the stage has not been initialized.
   *
   * @return an {@link Optional} containing the {@link StageHandle} if a stage is available, or an
   *     empty {@link Optional} if no stage is present
   */
  default Optional<StageHandle> stage() {
    return Optional.empty();
  }

  /**
   * Provides a platform-specific sound player implementation.
   *
   * <p>This method returns an instance of {@link ISoundPlayer} to manage and play audio in the
   * runtime environment.
   *
   * <p>By default, this returns a {@link NoSoundPlayer}, which is a no-op implementation that
   * performs no actual sound playback. Platform-specific implementations may override this with a
   * functional sound player.
   *
   * @return an instance of {@link ISoundPlayer}, which can be used to manage audio playback.
   */
  default ISoundPlayer soundPlayer() {
    return new NoSoundPlayer();
  }
}
