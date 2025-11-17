package core.sound.player;

import core.Entity;
import core.sound.AudioApi;
import core.sound.SoundSpec;
import core.utils.Point;
import java.util.Optional;

/**
 * Interface for sound player implementations, enabling pluggable audio backends.
 *
 * <p>Supports client-side playback via libGDX and server-side no-op. All audio are tracked by
 * unique instance IDs for updates and lifecycle management. Use {@link AudioApi} for high-level
 * sound operations.
 *
 * <p><b>Implementations:</b> {@link GdxSoundPlayer} for clients, {@link NoSoundPlayer} for servers.
 *
 * @see GdxSoundPlayer
 * @see NoSoundPlayer
 * @see IPlayHandle
 * @see AudioApi
 */
public interface ISoundPlayer {

  /**
   * Null-safe update container for modifying playing audio.
   *
   * <p>Any field set to null will not be changed when applying the update. This allows partial
   * updates without needing to know all current values.
   */
  final class SoundUpdate {
    /** The new volume to set, or null to leave unchanged. Range: [0.0, 1.0]. */
    public final Float volume;

    /** The new stereo pan to set, or null to leave unchanged. Range: [-1.0, 1.0]. */
    public final Float pan;

    /** The new pitch to set, or null to leave unchanged. 1.0 is normal speed. */
    public final Float pitch;

    /** The new paused state to set, or null to leave unchanged. */
    public final Boolean paused;

    /** The new looping state to set, or null to leave unchanged. */
    public final Boolean looping;

    private SoundUpdate(Builder b) {
      this.volume = b.volume;
      this.pan = b.pan;
      this.pitch = b.pitch;
      this.paused = b.paused;
      this.looping = b.looping;
    }

    /**
     * Creates a new builder for constructing a SoundUpdate.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
      return new Builder();
    }

    /**
     * Applies this update to the given play handle.
     *
     * @param handle the play handle to update
     */
    public void applyTo(IPlayHandle handle) {
      if (this.volume != null) {
        handle.volume(this.volume);
      }
      if (this.pan != null && this.volume != null) {
        handle.pan(this.pan, this.volume);
      }
      if (this.pitch != null) {
        handle.pitch(this.pitch);
      }
      if (this.paused != null) {
        if (this.paused) {
          handle.pause();
        } else {
          handle.resume();
        }
      }
      if (this.looping != null) {
        handle.looping(this.looping);
      }
    }

    /** Builder for creating {@link SoundUpdate} instances. */
    public static final class Builder {
      private Float volume;
      private Float pan;
      private Float pitch;
      private Boolean paused;
      private Boolean looping;

      /**
       * Sets the volume to update.
       *
       * @param newValue the new volume (0.0 to 1.0), or null to leave unchanged
       * @return this builder for method chaining
       */
      public Builder volume(Float newValue) {
        this.volume = newValue;
        return this;
      }

      /**
       * Sets the stereo pan to update.
       *
       * @param newPan the new pan (-1.0 left to 1.0 right), or null to leave unchanged
       * @param newVolume the new volume (0.0 to 1.0), required if pan is set
       * @return this builder for method chaining
       */
      public Builder pan(Float newPan, Float newVolume) {
        this.pan = newPan;
        this.volume = newVolume;
        return this;
      }

      /**
       * Sets the pitch to update.
       *
       * @param newValue the new pitch (1.0 is normal), or null to leave unchanged
       * @return this builder for method chaining
       */
      public Builder pitch(Float newValue) {
        this.pitch = newValue;
        return this;
      }

      /**
       * Sets the paused state to update.
       *
       * @param newValue the new paused state (true to pause, false to unpause), or null to leave
       *     unchanged
       * @return this builder for method chaining
       */
      public Builder paused(Boolean newValue) {
        this.paused = newValue;
        return this;
      }

      /**
       * Sets the looping state to update.
       *
       * @param newValue the new looping state (true to loop, false for one-shot), or null to leave
       *     unchanged
       * @return this builder for method chaining
       */
      public Builder looping(Boolean newValue) {
        this.looping = newValue;
        return this;
      }

      /**
       * Builds the SoundUpdate instance.
       *
       * @return a new SoundUpdate with the configured values
       */
      public SoundUpdate build() {
        return new SoundUpdate(this);
      }
    }
  }

  /**
   * Plays a sound by ID with specified volume, looping behavior, pitch, and pan, tracking it by the
   * given instance ID.
   *
   * <p>This is the primary method for playing audio. All audio should be requested via {@link
   * AudioApi} which manages instance IDs and entity attachment.
   *
   * @param instanceId globally unique sound instance identifier (obtained from AudioApi)
   * @param soundName the unique sound asset name (file name without extension)
   * @param volume the initial volume (0.0 to 1.0)
   * @param looping true for looping playback, false for one-shot
   * @param pitch the playback pitch (1.0 is normal)
   * @param pan the stereo pan (-1.0 left, 0.0 center, 1.0 right)
   * @param onFinished optional callback to run when playback finishes
   * @return an {@link IPlayHandle} for control, or empty if playback fails
   * @throws IllegalArgumentException if volume is out of range
   * @see AudioApi#playOnEntity(Entity, SoundSpec.Builder)
   * @see AudioApi#playAtPosition(Point, SoundSpec.Builder)
   * @see AudioApi#playGlobal(SoundSpec.Builder)
   */
  Optional<IPlayHandle> playWithInstance(
      long instanceId,
      String soundName,
      float volume,
      boolean looping,
      float pitch,
      float pan,
      Runnable onFinished);

  /**
   * Updates a specific sound instance with new parameters.
   *
   * <p>Only non-null fields in the update are applied. Unknown instance IDs are silently ignored.
   *
   * @param instanceId the unique sound instance identifier
   * @param update container with nullable fields; only non-null fields are applied
   * @return true if instance exists and was updated, false otherwise
   */
  boolean updateSound(long instanceId, SoundUpdate update);

  /**
   * Retrieves the play handle for a specific sound instance.
   *
   * @param instanceId the unique sound instance identifier
   * @return the play handle if found, empty otherwise
   */
  Optional<IPlayHandle> get(long instanceId);

  /**
   * Stops and removes a specific sound instance.
   *
   * @param instanceId the unique sound instance identifier to stop
   * @return true if instance was found and stopped, false otherwise
   */
  boolean stopByInstance(long instanceId);

  /**
   * Updates the sound player state, typically called each frame.
   *
   * <p>Manages sound lifecycle, such as removing finished non-looping audio and triggering
   * onFinished callbacks.
   *
   * @param delta time elapsed since last update in seconds
   */
  void update(float delta);

  /**
   * Immediately stops all currently playing audio.
   *
   * <p>Triggers onFinished callbacks for all stopped audio.
   *
   * @see IPlayHandle#stop()
   */
  void stopAll();

  /**
   * Disposes of the sound player and releases all resources.
   *
   * <p>Stops all active audio, unloads all audio assets, and clears internal state. Call on
   * application shutdown. The player is unusable after disposal.
   */
  void dispose();
}
