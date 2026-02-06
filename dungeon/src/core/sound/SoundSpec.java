package core.sound;

import java.io.Serial;
import java.io.Serializable;

/**
 * Describes a single sound instance to be played for an entity.
 *
 * <p>Used inside {@link core.components.SoundComponent}. Instances are immutable and created using
 * the {@link Builder} pattern.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * SoundSpec spec = SoundSpec.builder("fireball")
 *     .volume(0.8f)
 *     .pitch(1.2f)
 *     .looping(false)
 *     .build();
 * }</pre>
 *
 * @param instanceId Globally unique instance identifier.
 * @param soundName The name of the sound asset to play (file name without extension).
 * @param baseVolume The base volume level before distance attenuation. Range: [0.0, 1.0].
 * @param looping Whether the sound should loop indefinitely until explicitly stopped.
 * @param pitch The playback pitch multiplier. 1.0 is normal speed, 2.0 is double speed, 0.5 is half
 *     speed.
 * @param pan The stereo pan position. -1.0 is left, 0.0 is center, 1.0 is right.
 * @param maxDistance Maximum distance for sound audibility. -1 means infinite range (no distance
 *     attenuation).
 * @param attenuationFactor Factor for distance-based volume attenuation. 1.0 is default linear
 *     attenuation, higher values increase attenuation rate.
 * @param targetEntityIds Optional target entity IDs for multiplayer clients. Empty array targets
 *     all clients.
 * @see core.components.SoundComponent
 * @see core.sound.AudioApi
 */
public record SoundSpec(
    long instanceId,
    String soundName,
    float baseVolume,
    boolean looping,
    float pitch,
    float pan,
    float maxDistance,
    float attenuationFactor,
    int... targetEntityIds)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a SoundSpec with the specified parameters. The targetEntityIds array is defensively
   * copied to ensure immutability.
   *
   * @param instanceId Globally unique instance identifier.
   * @param soundName The name of the sound asset to play (file name without extension).
   * @param baseVolume The base volume level before distance attenuation. Range: [0.0, 1.0].
   * @param looping Whether the sound should loop indefinitely until explicitly stopped.
   * @param pitch The playback pitch multiplier. 1.0 is normal speed, 2.0 is double speed, 0.5 is
   *     half speed.
   * @param pan The stereo pan position. -1.0 is left, 0.0 is center, 1.0 is right.
   * @param maxDistance Maximum distance for sound audibility. -1 means infinite range (no distance
   *     attenuation).
   * @param attenuationFactor Factor for distance-based volume attenuation. 1.0 is default linear
   *     attenuation, higher values increase attenuation rate.
   * @param targetEntityIds Optional target entity IDs for multiplayer clients. Empty array targets
   *     all clients.
   * @throws IllegalArgumentException if soundName is null or blank, or if baseVolume is out of
   *     range
   */
  public SoundSpec {
    if (soundName == null || soundName.isBlank())
      throw new IllegalArgumentException("soundName required");
    if (baseVolume < 0f || baseVolume > 1f)
      throw new IllegalArgumentException("baseVolume must be in range [0.0, 1.0]");

    targetEntityIds = targetEntityIds.clone(); // Defensive copy for immutability
  }

  /** Default volume level for sound playback. Range: [0.0, 1.0]. Default: 0.5 */
  public static final float DEFAULT_VOLUME = 0.5f;

  /** Default pitch for sound playback. 1.0 is normal speed. Default: 1.0 */
  public static final float DEFAULT_PITCH = 1.0f;

  /** Default stereo pan. -1.0=left, 0.0=center, 1.0=right. Default: 0.0 */
  public static final float DEFAULT_PAN = 0.0f;

  /** Default attenuation factor for distance-based volume reduction. Default: 1.0 */
  public static final float DEFAULT_ATTENUATION = 1.0f;

  /**
   * Returns a defensive copy of the target entity IDs.
   *
   * @return the target entity IDs, or an empty array if none were set
   */
  @Override
  public int[] targetEntityIds() {
    return targetEntityIds.clone();
  }

  /**
   * Creates a new builder for constructing a SoundSpec.
   *
   * @param soundId the name of the sound asset to play (required, non-blank)
   * @return a new builder instance
   * @throws IllegalArgumentException if soundId is null or blank
   */
  public static Builder builder(String soundId) {
    return new Builder(soundId);
  }

  /**
   * Builder for creating {@link SoundSpec} instances with fluent API.
   *
   * <p>All parameters except soundName are optional and have sensible defaults. The builder is
   * typically used with method chaining:
   *
   * <pre>{@code
   * SoundSpec spec = SoundSpec.builder("explosion")
   *     .volume(0.9f)
   *     .pitch(0.95f)
   *     .maxDistance(100f)
   *     .build();
   * }</pre>
   */
  public static final class Builder {
    long instanceId; // package-private for AudioApi access
    private final String soundName;
    private float baseVolume = DEFAULT_VOLUME;
    private boolean looping = false;
    private float pitch = DEFAULT_PITCH;
    private float pan = DEFAULT_PAN;
    private float maxDistance = -1f;
    private float attenuationFactor = DEFAULT_ATTENUATION;
    private int[] targetEntityIds = new int[0];

    /**
     * Creates a new builder with the specified sound name.
     *
     * @param soundName the name of the sound asset (required, non-blank)
     * @throws IllegalArgumentException if soundName is null or blank
     */
    public Builder(String soundName) {
      if (soundName == null || soundName.isBlank())
        throw new IllegalArgumentException("soundName required");
      this.soundName = soundName;
    }

    /**
     * Sets the globally unique instance identifier for this sound.
     *
     * <p><b>Note:</b> This is typically set automatically by {@link core.sound.AudioApi} and should
     * not be set manually in most cases.
     *
     * @param id the instance identifier
     * @return this builder for method chaining
     */
    public Builder instanceId(long id) {
      this.instanceId = id;
      return this;
    }

    /**
     * Sets the base volume level before distance attenuation.
     *
     * @param v the volume level (0.0 to 1.0, default: {@value #DEFAULT_VOLUME})
     * @return this builder for method chaining
     */
    public Builder volume(float v) {
      this.baseVolume = v;
      return this;
    }

    /**
     * Sets whether the sound should loop indefinitely.
     *
     * @param l true to loop, false for one-shot playback (default: false)
     * @return this builder for method chaining
     */
    public Builder looping(boolean l) {
      this.looping = l;
      return this;
    }

    /**
     * Sets the playback pitch multiplier.
     *
     * @param p the pitch multiplier (1.0 is normal, 2.0 is double speed, default: {@value
     *     #DEFAULT_PITCH})
     * @return this builder for method chaining
     */
    public Builder pitch(float p) {
      this.pitch = p;
      return this;
    }

    /**
     * Sets the stereo pan position.
     *
     * @param p the pan position (-1.0 left, 0.0 center, 1.0 right, default: {@value #DEFAULT_PAN})
     * @return this builder for method chaining
     */
    public Builder pan(float p) {
      this.pan = p;
      return this;
    }

    /**
     * Sets the maximum distance for sound audibility.
     *
     * @param d the maximum distance in world units, or -1 for infinite range (default: -1)
     * @return this builder for method chaining
     */
    public Builder maxDistance(float d) {
      this.maxDistance = d;
      return this;
    }

    /**
     * Sets the distance attenuation factor.
     *
     * @param a the attenuation factor (1.0 is default linear, higher values increase attenuation
     *     rate, default: {@value #DEFAULT_ATTENUATION})
     * @return this builder for method chaining
     */
    public Builder attenuation(float a) {
      this.attenuationFactor = a;
      return this;
    }

    /**
     * Sets the target entity IDs that should hear this sound.
     *
     * <p>Empty array means all clients.
     *
     * @param ids the entity IDs to target
     * @return this builder for method chaining
     */
    public Builder targets(int... ids) {
      this.targetEntityIds = ids == null ? new int[0] : ids.clone();
      return this;
    }

    /**
     * Builds the immutable SoundSpec instance.
     *
     * @return a new SoundSpec with the configured values
     */
    public SoundSpec build() {
      return new SoundSpec(
          this.instanceId,
          this.soundName,
          this.baseVolume,
          this.looping,
          this.pitch,
          this.pan,
          this.maxDistance,
          this.attenuationFactor,
          this.targetEntityIds);
    }
  }
}
