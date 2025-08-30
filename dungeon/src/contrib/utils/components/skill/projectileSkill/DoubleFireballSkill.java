package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * An enchantment version of a normal attack. Shoots two fireballs at the hero. One directly at the
 * hero and one is trying to predict the hero's movement.
 */
public class DoubleFireballSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "Double Fireball";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float SPEED = 4.5f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 25f;
  private static final int DELAY_BETWEEN_FIREBALLS = 50;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param end A supplier providing the endpoint (target location) of the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param pircing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param hitBoxSize The hitbox size of the projectile used for collision detection.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  public DoubleFireballSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
        cooldown,
        texture,
        end,
        speed,
        range,
        pircing,
        damageAmount,
        damageType,
        hitBoxSize,
        resourceCost);
  }

  /**
   * An enchantment version of a normal attack. Shoots two fireballs at the target. One directly at
   * the target and one is trying to predict the targets movement.
   *
   * @param cooldown The cool down of the skill.
   * @param endPosition Supplier to get the position of the target.
   */
  public DoubleFireballSkill(long cooldown, Supplier<Point> endPosition) {
    this(
        SKILL_NAME,
        cooldown,
        TEXTURE,
        endPosition,
        SPEED,
        RANGE,
        false,
        DAMAGE,
        DamageType.FIRE,
        DEFAULT_HITBOX_SIZE);
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point targetPosition = end(caster);
    shootProjectile(caster, start(caster), end(caster));
    EventScheduler.scheduleAction(
        () -> {
          Point newTargetPosition = end(caster);
          Vector2 targetDirection = targetPosition.vectorTo(newTargetPosition).normalize();
          targetDirection =
              targetDirection.scale((float) (start(caster).distance(targetPosition)) * 2);
          Point predictedTargetPostion = newTargetPosition.translate(targetDirection);
          shootProjectile(caster, start(caster), predictedTargetPostion);
        },
        DELAY_BETWEEN_FIREBALLS);
  }

  /**
   * Called when the fireball projectile spawns in the game world.
   *
   * <p>Plays a fireball sound effect with a random pitch for variation.
   *
   * @param caster The entity casting the fireball.
   * @param projectile The projectile entity spawned.
   */
  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(SOUND.pathString()));

    // Generate a random pitch between minPitch and maxPitch
    float minPitch = 2f;
    float maxPitch = 3f;
    float randomPitch = MathUtils.random(minPitch, maxPitch);

    // Play the sound with adjusted pitch and low volume
    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, randomPitch);
    soundEffect.setVolume(soundId, 0.05f);
  }
}
