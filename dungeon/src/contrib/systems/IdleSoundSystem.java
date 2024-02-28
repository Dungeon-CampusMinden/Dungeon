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

  @Override
  public void execute() {
    entityStream()
        .filter(IdleSoundSystem::onlyKeepNearbyEntities)
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

  private static boolean onlyKeepNearbyEntities(Entity entity) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return false;
    }

    PositionComponent heroPositionComponent = hero.fetch(PositionComponent.class).orElse(null);
    PositionComponent entityPositionComponent = entity.fetch(PositionComponent.class).orElse(null);

    if (heroPositionComponent == null || entityPositionComponent == null) {
      return false;
    }

    Point heroPosition = heroPositionComponent.position();
    Point entityPosition = entityPositionComponent.position();

    float distance = Point.calculateDistance(heroPosition, entityPosition);

    return distance < DISTANCE_THRESHOLD;
  }
}
