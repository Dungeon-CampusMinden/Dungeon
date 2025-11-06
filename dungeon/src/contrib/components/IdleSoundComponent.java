package contrib.components;

import contrib.entities.MonsterIdleSound;
import core.Component;

/**
 * Stores a String path to a sound file that can be played by the {@link
 * contrib.systems.IdleSoundSystem}.
 *
 * @param soundEffectId The unique identifier of the sound effect to be played.
 * @see contrib.systems.IdleSoundSystem
 * @see core.sound.player.ISoundPlayer
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
