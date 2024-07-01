package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.IdleSoundComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
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
  private static final float DISTANCE_THRESHOLD = 10.0f;

  /** Create a new {@link IdleSoundSystem}. */
  public IdleSoundSystem() {
    super(IdleSoundComponent.class);
  }

  private static boolean isEntityNearby(Point heroPos, Entity entity) {
    Point entityPosition =
        entity.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);

    if (heroPos == null || entityPosition == null) {
      return false;
    }

    double distance = heroPos.distance(entityPosition);

    return distance < DISTANCE_THRESHOLD;
  }

  @Override
  public void execute() {
    Point heroPos =
        Game.hero()
            .flatMap(e -> e.fetch(PositionComponent.class).map(PositionComponent::position))
            .orElse(null);
    filteredEntityStream(IdleSoundComponent.class)
        .filter(e -> isEntityNearby(heroPos, e))
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
      Sound soundEffect =
          Gdx.audio.newSound(Gdx.files.internal(component.soundEffect().pathString()));
      long soundID = soundEffect.play();
      soundEffect.setLooping(soundID, false);
      soundEffect.setVolume(soundID, 0.35f);
    }
  }
}
