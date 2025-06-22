package utils.components.skill;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.entities.HeroFactory;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.DamageProjectile;
import core.Game;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectile}.
 *
 * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the intended
 * target will get frozen in place and the projectile will fly towards the target. The target will
 * not be able to move or dodge the projectile.
 */
public class InevitableFireballSkill extends DamageProjectile {
  private static final String SKILL_NAME = "inevitable_fireball";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float DEFAULT_PROJECTILE_SPEED = 5.0f;
  private static final int DEFAULT_DAMAGE_AMOUNT = Integer.MAX_VALUE;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = new Vector2(1, 1);

  /**
   * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
   *
   * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the
   * intended target will get frozen in place and the projectile will fly towards the target. The
   * target will not be able to move or dodge the projectile.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectile
   */
  public InevitableFireballSkill(final Supplier<Point> targetSelection) {
    super(
        SKILL_NAME,
        PROJECTILE_TEXTURES,
        DEFAULT_PROJECTILE_SPEED,
        DEFAULT_DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        HIT_BOX_SIZE,
        targetSelection,
        DEFAULT_PROJECTILE_RANGE,
        // If the fireball does not hit the player, still restart the level
        entity -> Client.restart(),
        (projectile, entity) -> {
          // Set the velocity back to the original value (hero only)
          if (!entity.isPresent(PlayerComponent.class)) return;
          Vector2 defaultHeroSpeed = HeroFactory.defaultHeroSpeed();
          entity
              .fetch(VelocityComponent.class)
              .ifPresent(
                  velocityComponent -> {
                    velocityComponent.xVelocity(defaultHeroSpeed.x());
                    velocityComponent.yVelocity(defaultHeroSpeed.y());
                  });
        },
        (projectile) -> {
          // Set the velocity to zero to freeze the entity (hero only)
          Game.hero()
              .flatMap(hero -> hero.fetch(VelocityComponent.class))
              .ifPresent(
                  velocityComponent -> {
                    velocityComponent.xVelocity(0);
                    velocityComponent.yVelocity(0);
                  });
        });
  }

  @Override
  protected void playSound() {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(PROJECTILE_SOUND.pathString()));

    // Generate a random pitch between 1.5f and 2.0f
    float minPitch = 2f;
    float maxPitch = 3f;
    float randomPitch = MathUtils.random(minPitch, maxPitch);

    // Play the sound with the adjusted pitch
    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, randomPitch);

    // Set the volume
    soundEffect.setVolume(soundId, 0.05f);
  }
}
