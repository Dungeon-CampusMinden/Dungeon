package core.sound.player;

/**
 * Abstract base class for controlling a playing sound instance.
 *
 * <p>Provides real-time control over volume, panning, pitch, and playback state. Each handle is
 * associated with a unique instance ID for tracking purposes. Concrete implementations must provide
 * the {@link #update(float)} method for frame-by-frame updates.
 *
 * <p>Handles are returned by {@link ISoundPlayer#playWithInstance} and can be used to modify
 * playback parameters or register completion callbacks.
 *
 * @see ISoundPlayer#playWithInstance(long, String, float, boolean, float, float, Runnable)
 * @see GdxSoundPlayer
 */
public abstract class PlayHandle {

  /** Indicates whether this sound has finished playing. */
  protected boolean finished = false;

  /** Optional callback to execute when the sound finishes. */
  protected Runnable onFinishedCallback;

  private final long instanceId;

  /**
   * Creates a new PlayHandle instance.
   *
   * <p>It automatically assigns a unique instance ID.
   *
   * @param instanceId the unique instance identifier for this sound
   */
  protected PlayHandle(long instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Returns the globally unique instance identifier for this sound.
   *
   * <p>Used for tracking and managing individual sound instances throughout their lifecycle.
   *
   * @return the unique instance ID
   */
  public long instanceId() {
    return instanceId;
  }

  /**
   * Immediately stops sound playback and triggers the onFinished callback.
   *
   * <p><b>Side effects:</b> Stops audio, marks as finished, runs callback if registered.
   */
  public abstract void stop();

  /**
   * Pauses sound playback without losing position.
   *
   * <p>Call {@link #resume()} to continue from the paused position.
   */
  public abstract void pause();

  /**
   * Resumes playback from a paused state.
   *
   * <p>Has no effect if the sound is not paused.
   */
  public abstract void resume();

  /**
   * Sets the volume of the sound.
   *
   * @param volume the volume level (0.0=silent, 1.0=full; default: 0.5)
   * @throws IllegalArgumentException if volume not in [0.0, 1.0]
   */
  public abstract void volume(float volume);

  /**
   * Sets the stereo pan and volume simultaneously.
   *
   * <p><b>Note:</b> Pan is not supported for stereo audio files; attempting to pan stereo audio may
   * log a warning and have no effect.
   *
   * @param pan the pan position (-1.0=left, 0.0=center, 1.0=right; default: 0.0)
   * @param volume the volume level (0.0=silent, 1.0=full)
   * @throws IllegalArgumentException if pan not in [-1.0, 1.0]
   */
  public abstract void pan(float pan, float volume);

  /**
   * Sets the playback pitch (speed).
   *
   * @param pitch the pitch multiplier (0.5=half speed, 1.0=normal, 2.0=double speed; default: 1.0)
   */
  public abstract void pitch(float pitch);

  /**
   * Checks if the sound is currently playing.
   *
   * <p>Returns false if stopped or finished. Looping audio always return true until explicitly
   * stopped.
   *
   * @return true if actively playing, false if stopped/finished
   */
  public abstract boolean isPlaying();

  /**
   * Registers a callback to execute when the sound finishes playing.
   *
   * <p>Called when a non-looping sound completes naturally or when any sound is stopped via {@link
   * #stop()}.
   *
   * <p><b>Execution:</b> Callback runs immediately if sound already finished, otherwise queued.
   *
   * <p>The callback will be executed when {@link #callFinished()} is invoked by the concrete
   * implementation.
   *
   * @param callback the runnable to execute on finish (null is ignored)
   */
  public void onFinished(Runnable callback) {
    this.onFinishedCallback = callback;
  }

  /**
   * Enables or disables looping for the sound.
   *
   * <p>Looping audio continue playing indefinitely until explicitly stopped.
   *
   * @param looping true to loop indefinitely, false for one-shot playback (default: false)
   */
  public abstract void looping(boolean looping);

  /**
   * Marks this sound as finished and executes the onFinished callback if one is registered.
   *
   * <p>This method should be called by concrete implementations when the sound completes playback
   * naturally or is stopped explicitly. The callback is executed synchronously before returning.
   */
  protected void callFinished() {
    if (onFinishedCallback != null) {
      onFinishedCallback.run();
    }
    finished = true;
  }

  /**
   * Updates the state of this play handle.
   *
   * <p>Called each frame by the sound player. Implementations should check if non-looping sounds
   * have finished and call {@link #callFinished()} when appropriate.
   *
   * @param delta time elapsed since last update in seconds
   */
  abstract void update(float delta);
}
