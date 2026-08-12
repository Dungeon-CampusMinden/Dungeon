package feature.components;

import engine.Component;
import feature.entities.MonsterIdleSound;

/**
 * Stores a String path to a sound file that can be played by the {@link
 * feature.systems.IdleSoundSystem}.
 *
 * @param soundEffectId The unique identifier of the sound effect to be played.
 * @see feature.systems.IdleSoundSystem
 * @see engine.sound.player.ISoundPlayer
 */
public record IdleSoundComponent(String soundEffectId) implements Component {

  /**
   * Create a new {@link IdleSoundComponent} using a {@link MonsterIdleSound} enum value.
   *
   * @param soundEffect The enum value representing the sound effect.
   */
  public IdleSoundComponent(MonsterIdleSound soundEffect) {
    this(soundEffect.soundId());
  }
}
