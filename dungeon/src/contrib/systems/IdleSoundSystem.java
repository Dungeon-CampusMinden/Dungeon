package contrib.systems;

import contrib.components.IdleSoundComponent;
import contrib.utils.SoundPlayer;
import core.System;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import java.util.Random;

/**
 * Works on Entities that contain the {@link IdleSoundComponent} and plays the stored sound effect
 * randomly.
 *
 * <p>Use this if you want to add some white noise monster sounds to your game.
 *
 * <p>Note: The chance that the sound is played is very low, so it shouldn't be too much noise.
 */
public final class IdleSoundSystem extends System {

  private static final Random RANDOM = new Random();

  /** Create a new {@link IdleSoundSystem}. */
  public IdleSoundSystem() {
    super(IdleSoundComponent.class);
  }

  @Override
  public void execute() {
    entityStream()
        .forEach(
            e ->
                playSound(
                    e.fetch(IdleSoundComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(e, IdleSoundComponent.class))));
  }

  private void playSound(final IdleSoundComponent component) {
    float chanceToPlaySound = 0.001f;
    if (RANDOM.nextFloat(0f, 1f) < chanceToPlaySound) {
      SoundPlayer.playSound(new SimpleIPath(component.soundEffect().pathString()), false, 0.35f);
    }
  }
}
