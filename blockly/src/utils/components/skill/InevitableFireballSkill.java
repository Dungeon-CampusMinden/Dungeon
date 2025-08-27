package utils.components.skill;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.entities.HeroFactory;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.damageSkill.projectile.DamageProjectileSkill;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectileSkill}.
 *
 * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the intended
 * target will get frozen in place and the projectile will fly towards the target. The target will
 * not be able to move or dodge the projectile.
 */
public class InevitableFireballSkill extends DamageProjectileSkill {
  private static final String SKILL_NAME = "inevitable_fireball";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float DEFAULT_PROJECTILE_SPEED = 5.0f;
  private static final int DEFAULT_DAMAGE_AMOUNT = Integer.MAX_VALUE;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final int COOLDOWN = 750;

  /**
   * Create a {@link DamageProjectileSkill} that looks like a fireball and will cause fire damage.
   *
   * <p>This skill is inevitable, meaning that it will always hit the target. Once fired, the
   * intended target will get frozen in place and the projectile will fly towards the target. The
   * target will not be able to move or dodge the projectile.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public InevitableFireballSkill(final Supplier<Point> targetSelection) {
    super(
        SKILL_NAME,
        COOLDOWN,
        targetSelection,
        DEFAULT_DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        PROJECTILE_TEXTURES,
        DEFAULT_PROJECTILE_SPEED,
        DEFAULT_PROJECTILE_RANGE,
        HIT_BOX_SIZE,
        DamageProjectileSkill.DEFAULT_ON_WALL_HIT,
        (projectile) -> {
          playSound();
          // Set the velocity to zero to freeze the entity (hero only)
          Game.hero()
              .flatMap(hero -> hero.fetch(VelocityComponent.class))
              .ifPresent(
                  velocityComponent -> {
                    velocityComponent.maxSpeed(0);
                  });
          // Centers the hero on the tile, so the Blockly step looks completed, and the hero doesn't
          // freeze on the corner of the red zone
          Game.hero()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .ifPresent(PositionComponent::centerPositionOnTile);
        },
        (projectile, entity) -> {
          // Set the velocity back to the original value (hero only)
          if (!entity.isPresent(PlayerComponent.class)) return;
          Vector2 defaultHeroSpeed = HeroFactory.defaultHeroSpeed();
          entity
              .fetch(VelocityComponent.class)
              .ifPresent(
                  velocityComponent -> {
                    velocityComponent.maxSpeed(Client.MOVEMENT_FORCE.x());
                  });
        });
  }

  private static void playSound() {
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
