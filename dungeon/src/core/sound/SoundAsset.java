package core.sound;

import core.sound.player.ISoundPlayer;
import java.util.Optional;

/**
 * Represents a sound asset loaded from the game's sound directory. Contains metadata about the
 * sound file, such as its ID, file path, and optional duration. Used by {@link ISoundPlayer}
 * implementations to manage and play sounds.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * SoundAsset asset = new SoundAsset("fireball", "fireball.wav", 2000L);
 * }</pre>
 *
 * @param id unique identifier for the sound, used for playback
 * @param path relative file path to the sound file
 * @param durationMs optional duration in milliseconds; empty if unknown or for streaming music
 * @see ISoundPlayer#play(String, float, boolean)
 */
public record SoundAsset(String id, String path, Optional<Long> durationMs) {

  /**
   * Constructs a SoundAsset without a known duration.
   *
   * @param id unique identifier for the sound
   * @param path relative file path to the sound file
   */
  public SoundAsset(String id, String path) {
    this(id, path, Optional.empty());
  }

  /**
   * Constructs a SoundAsset with a known duration.
   *
   * @param id unique identifier for the sound
   * @param path relative file path to the sound file
   * @param durationMs duration in milliseconds
   */
  public SoundAsset(String id, String path, long durationMs) {
    this(id, path, Optional.of(durationMs));
  }
}
