package core.sound.player;

/**
 * Abstract base class for sound playback handle implementations.
 *
 * <p>Provides common functionality for tracking sound playback state, managing callbacks, and
 * handling instance IDs. Concrete implementations must provide the {@link #update(float)} method
 * for frame-by-frame updates.
 *
 * @see IPlayHandle
 * @see GdxSoundPlayer
 */
public abstract class AbstractPlayHandle implements IPlayHandle {

  /** Indicates whether this sound has finished playing. */
  protected boolean finished = false;

  /** Optional callback to execute when the sound finishes. */
  protected Runnable onFinishedCallback;

  private final long instanceId;

  /**
   * Creates a new AbstractPlayHandle instance.
   *
   * <p>It automatically assigns a unique instance ID.
   */
  protected AbstractPlayHandle(long instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The callback will be executed when {@link #callFinished()} is invoked by the concrete
   * implementation.
   */
  @Override
  public void onFinished(Runnable callback) {
    this.onFinishedCallback = callback;
  }

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
   * {@inheritDoc}
   *
   * @return the globally unique instance identifier for this sound
   */
  @Override
  public long instanceId() {
    return instanceId;
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
