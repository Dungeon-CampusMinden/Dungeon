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

  /** Default volume level for sound playback (0.0 to 1.0). */
  float DEFAULT_VOLUME = 0.5f;

  /** Default pitch for sound playback (1.0 is normal). */
  float DEFAULT_PITCH = 1.0f;

  /** Default pan for sound playback (0.0 is center). */
  float DEFAULT_PAN = 0.0f;

  /**
   * Plays a sound by ID with specified volume, looping behavior, pitch, and pan.
   *
   * @param id the unique sound identifier
   * @param volume the initial volume (0.0 to 1.0)
   * @param looping true for looping playback, false for one-shot
   * @param pitch the playback pitch (1.0 is normal)
   * @param pan the stereo pan (-1.0 left, 0.0 center, 1.0 right)
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   * @throws IllegalArgumentException if volume is out of range
   */
  Optional<IPlayHandle> play(String id, float volume, boolean looping, float pitch, float pan);

  /**
   * Plays a sound by ID with specified volume and looping behavior, using default pitch and pan.
   *
   * <p>Defaults to {@link #DEFAULT_PITCH} and {@link #DEFAULT_PAN}.
   *
   * @param id the unique sound identifier
   * @param volume the initial volume (0.0 to 1.0)
   * @param looping true for looping playback, false for one-shot
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   * @throws IllegalArgumentException if volume is out of range
   */
  default Optional<IPlayHandle> play(String id, float volume, boolean looping) {
    return play(id, volume, looping, DEFAULT_PITCH, DEFAULT_PAN);
  }

  /**
   * Plays a sound by ID with specified volume with default non-looping behavior.
   *
   * <p>Defaults to non-looping playback and uses {@link #DEFAULT_PITCH}, {@link #DEFAULT_PAN}.
   *
   * @param id the unique sound identifier
   * @param volume the initial volume (0.0 to 1.0)
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   * @throws IllegalArgumentException if volume is out of range
   */
  default Optional<IPlayHandle> play(String id, float volume) {
    return play(id, volume, false);
  }

  /**
   * Plays a sound by ID with default volume and non-looping behavior.
   *
   * <p>Defaults to {@link #DEFAULT_VOLUME}, non-looping playback, and uses {@link #DEFAULT_PITCH},
   * {@link #DEFAULT_PAN}.
   *
   * @param id the unique sound identifier
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   * @see #DEFAULT_VOLUME
   */
  default Optional<IPlayHandle> play(String id) {
    return play(id, DEFAULT_VOLUME, false);
  }

  /**
   * Updates the sound player state, typically called each frame. Handles sound lifecycle, such as
   * finishing non-looping sounds.
   *
   * @param delta time elapsed since last update
   */
  void update(float delta);

  /**
   * Stops all currently playing sounds immediately.
   *
   * @see IPlayHandle#stop()
   */
  void stopAll();

  /**
   * Disposes of the sound player and releases resources. Should be called on shutdown to clean up
   * audio assets.
   */
  void dispose();
}
