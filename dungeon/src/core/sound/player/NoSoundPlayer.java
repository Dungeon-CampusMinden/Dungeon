package core.sound.player;

import core.utils.logging.DungeonLogger;
import java.util.Optional;

/**
 * No-operation implementation of {@link ISoundPlayer} for environments without audio support.
 * Ignores all playback requests, suitable for headless servers or testing. Logs play requests for
 * debugging; can be extended to send network messages in multiplayer setups.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ISoundPlayer player = new NoSoundPlayer();
 * player.play("fireball", 0.8f, false); // Logs and returns empty
 * player.update(deltaTime); // No-op
 * player.dispose(); // Logs disposal
 * }</pre>
 *
 * @see ISoundPlayer
 * @see GdxSoundPlayer
 */
public class NoSoundPlayer implements ISoundPlayer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(NoSoundPlayer.class);

  /**
   * Ignores the play request and logs it. Always returns empty, as no sound is played.
   *
   * @param id the sound ID (ignored)
   * @param volume the volume (ignored)
   * @param looping the looping flag (ignored)
   * @return always empty
   */
  @Override
  public Optional<IPlayHandle> play(String id, float volume, boolean looping) {
    LOGGER.debug(
        "NoSoundPlayer: Ignoring play request for {} at volume {} looping {}", id, volume, looping);
    return Optional.empty();
  }

  /**
   * Does nothing, as there are no sounds to update.
   *
   * @param delta time delta (ignored)
   */
  @Override
  public void update(float delta) {
    // No-op
  }

  /** Logs disposal and performs no cleanup. */
  @Override
  public void dispose() {
    LOGGER.info("NoSoundPlayer: Disposing (no-op)");
  }
}
