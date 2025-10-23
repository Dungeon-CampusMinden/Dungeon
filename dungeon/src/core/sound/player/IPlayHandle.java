package core.sound.player;

/**
 * Interface for controlling a playing sound instance. Provides methods to manipulate playback,
 * volume, panning, and looping. Returned by {@link ISoundPlayer#play(String, float, boolean)} for
 * real-time sound control.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Optional<IPlayHandle> handle = soundPlayer.play("fireball", 0.8f, false);
 * handle.ifPresent(h -> {
 *     h.volume(0.5f);
 *     h.pan(-0.5f, 0.5f); // Pan left, half volume
 *     h.onFinished(() -> System.out.println("Fireball finished"));
 * });
 * }</pre>
 *
 * @see ISoundPlayer#play(String, float, boolean)
 */
public interface IPlayHandle {
  /** Stops the sound playback immediately. */
  void stop();

  /** Pauses the sound playback. */
  void pause();

  /** Resumes paused sound playback. */
  void resume();

  /**
   * Sets the volume of the sound.
   *
   * @param volume the volume level (0.0 to 1.0)
   */
  void volume(float volume);

  /**
   * Sets the stereo pan and volume.
   *
   * @param pan the pan position (-1.0 left, 0.0 center, 1.0 right)
   * @param volume the volume level (0.0 to 1.0)
   */
  void pan(float pan, float volume);

  /**
   * Checks if the sound is currently playing.
   *
   * @return true if playing, false otherwise
   */
  boolean isPlaying();

  /**
   * Sets a callback to run when the sound finishes playing.
   *
   * @param callback the runnable to execute on finish
   */
  void onFinished(Runnable callback);

  /**
   * Enables or disables looping for the sound.
   *
   * @param looping true to loop, false for one-shot
   */
  void setLooping(boolean looping);
}
