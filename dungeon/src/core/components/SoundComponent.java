package core.components;

import core.Component;
import core.sound.player.ISoundPlayer;

/**
 * Component for entities that emit sounds. The sound is positional, with volume and pan adjusted
 * based on distance and direction from the listener.
 *
 * @param soundId The identifier of the sound to play.
 * @param baseVolume The base volume of the sound (0.0 to 1.0). (default 0.5)
 * @param looping Whether the sound should loop continuously. (default false)
 * @param pitch The pitch adjustment for the sound (1.0 is normal pitch). (default 1.0)
 * @param maxDistance The maximum distance at which the sound can be heard. (-1 for infinite)
 *     (default -1)
 * @param attenuationFactor The factor controlling how quickly the sound attenuates with distance.
 *     (default 1.0)
 * @param onFinish A callback to execute when the sound finishes playing. (default no-op)
 */
public record SoundComponent(
    String soundId,
    float baseVolume,
    boolean looping,
    float pitch,
    float maxDistance,
    float attenuationFactor,
    Runnable onFinish)
    implements Component {

  private static final float DEFAULT_BASE_VOLUME = ISoundPlayer.DEFAULT_VOLUME;
  private static final boolean DEFAULT_LOOPING = false;
  private static final float DEFAULT_PITCH = 1.0f;
  private static final float DEFAULT_MAX_DISTANCE = -1f; // Infinite distance
  private static final float DEFAULT_ATTENUATION_FACTOR = 1.0f;
  private static final Runnable DEFAULT_ON_FINISH = () -> {};

  /**
   * Creates a SoundComponent with default parameters.
   *
   * @param soundId The identifier of the sound to play.
   */
  public SoundComponent(String soundId) {
    this(
        soundId,
        DEFAULT_BASE_VOLUME,
        DEFAULT_LOOPING,
        DEFAULT_PITCH,
        DEFAULT_MAX_DISTANCE,
        DEFAULT_ATTENUATION_FACTOR,
        DEFAULT_ON_FINISH);
  }

  /**
   * Creates a SoundComponent with specified soundId and baseVolume, other parameters default.
   *
   * @param soundId The identifier of the sound to play.
   * @param baseVolume The base volume of the sound (0.0 to 1.0).
   */
  public SoundComponent(String soundId, float baseVolume) {
    this(
        soundId,
        baseVolume,
        DEFAULT_LOOPING,
        DEFAULT_PITCH,
        DEFAULT_MAX_DISTANCE,
        DEFAULT_ATTENUATION_FACTOR,
        DEFAULT_ON_FINISH);
  }

  /**
   * Creates a SoundComponent with specified soundId, baseVolume, onFinish, other parameters
   * default.
   *
   * @param soundId The identifier of the sound to play.
   * @param baseVolume The base volume of the sound (0.0 to 1
   * @param onFinish A callback to execute when the sound finishes playing.
   */
  public SoundComponent(String soundId, float baseVolume, Runnable onFinish) {
    this(
        soundId,
        baseVolume,
        DEFAULT_LOOPING,
        DEFAULT_PITCH,
        DEFAULT_MAX_DISTANCE,
        DEFAULT_ATTENUATION_FACTOR,
        onFinish);
  }

  /**
   * Creates a SoundComponent with specified soundId, baseVolume and pitch, other parameters
   * default.
   *
   * @param soundId The identifier of the sound to play.
   * @param baseVolume The base volume of the sound (0.0 to 1.0).
   * @param pitch The pitch adjustment for the sound (1.0 is normal pitch).
   */
  public SoundComponent(String soundId, float baseVolume, float pitch) {
    this(
        soundId,
        baseVolume,
        DEFAULT_LOOPING,
        pitch,
        DEFAULT_MAX_DISTANCE,
        DEFAULT_ATTENUATION_FACTOR,
        DEFAULT_ON_FINISH);
  }
}
