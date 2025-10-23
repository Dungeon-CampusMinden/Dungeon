package core.sound.player;

import java.util.Optional;

/**
 * Interface for sound player implementations, enabling pluggable audio backends. Supports
 * client-side playback via libGDX and server-side simulation (e.g., sending network messages).
 * Manages sound assets, playback, and lifecycle; integrates with {@link IPlayHandle} for control.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ISoundPlayer player = new GdxSoundPlayer(assetManager);
 * Optional<IPlayHandle> handle = player.play("fireball", 0.9f, false);
 * player.update(deltaTime);
 * player.dispose();
 * }</pre>
 *
 * @see GdxSoundPlayer
 * @see NoSoundPlayer
 * @see IPlayHandle
 */
public interface ISoundPlayer {
  /**
   * Plays a sound by ID with specified volume and looping.
   *
   * @param id the unique sound identifier
   * @param volume the initial volume (0.0 to 1.0)
   * @param looping true for looping playback, false for one-shot
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   */
  Optional<IPlayHandle> play(String id, float volume, boolean looping);

  /**
   * Updates the sound player state, typically called each frame. Handles sound lifecycle, such as
   * finishing non-looping sounds.
   *
   * @param delta time elapsed since last update (in seconds)
   */
  void update(float delta);

  /**
   * Disposes of the sound player and releases resources. Should be called on shutdown to clean up
   * audio assets.
   */
  void dispose();
}
