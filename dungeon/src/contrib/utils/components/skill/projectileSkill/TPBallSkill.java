package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * A teleporting projectile skill that deals minor damage and teleports hit entities.
 *
 * <p>The TPBallSkill class extends {@link DamageProjectileSkill} to implement a skill where the
 * projectile flies toward a target, damages the first hit entity, and teleports it to a specified
 * point. The projectile is removed if it hits a wall or reaches its maximum range.
 */
public class TPBallSkill extends DamageProjectileSkill {
  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float PROJECTILE_SPEED = 6.0f;
  private static final int DAMAGE_AMOUNT = 1;
  private static final DamageType DAMAGE_TYPE = DamageType.MAGIC;
  private static final float PROJECTILE_RANGE = 7f;
  private static final long COOLDOWN = 2000;
  private static final boolean IS_PIERCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;

  /** Name of the Skill. */
  public static final String SKILL_NAME = "TPBall";

  private final Supplier<Point> tpTarget;

  /**
   * Creates a TPBallSkill with full custom parameters.
   *
   * @param target Supplier providing the projectile's target point.
   * @param tpTarget Supplier providing the teleportation destination point.
   * @param cooldown Skill cooldown in milliseconds.
   * @param speed Projectile travel speed.
   * @param range Maximum projectile range.
   * @param damageAmount Damage dealt on impact.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resources required to cast the skill.
   */
  @SafeVarargs
  public TPBallSkill(
      Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldown,
        TEXTURE,
        target,
        speed,
        range,
        IS_PIERCING,
        damageAmount,
        DAMAGE_TYPE,
        ignoreFirstWall,
        resourceCost);
    this.tpTarget = tpTarget;
    tintColor(0xFF00FFFF);
  }

  /**
   * Creates a TPBallSkill using all default values for cooldown, speed, range, and damage.
   *
   * @param target Supplier providing the projectile's target point.
   * @param tpTarget Supplier providing the teleportation destination point.
   * @param resourceCost Resources required to cast the skill.
   */
  @SafeVarargs
  public TPBallSkill(
      Supplier<Point> target, Supplier<Point> tpTarget, Tuple<Resource, Integer>... resourceCost) {
    this(
        target,
        tpTarget,
        COOLDOWN,
        PROJECTILE_RANGE,
        PROJECTILE_SPEED,
        DAMAGE_AMOUNT,
        IGNORE_FIRST_WALL,
        resourceCost);
  }

  /**
   * Creates a TPBallSkill with custom cooldown and default speed, range, and damage.
   *
   * @param target Supplier providing the projectile's target point.
   * @param tpTarget Supplier providing the teleportation destination point.
   * @param cooldown Skill cooldown in milliseconds.
   * @param resourceCost Resources required to cast the skill.
   */
  @SafeVarargs
  public TPBallSkill(
      Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      Tuple<Resource, Integer>... resourceCost) {
    this(
        target,
        tpTarget,
        cooldown,
        PROJECTILE_RANGE,
        PROJECTILE_SPEED,
        DAMAGE_AMOUNT,
        IGNORE_FIRST_WALL,
        resourceCost);
  }

  /**
   * Creates a TPBallSkill with custom cooldown, range, and speed, but default damage.
   *
   * @param target Supplier providing the projectile's target point.
   * @param tpTarget Supplier providing the teleportation destination point.
   * @param cooldown Skill cooldown in milliseconds.
   * @param range Maximum projectile range.
   * @param speed Projectile travel speed.
   * @param resourceCost Resources required to cast the skill.
   */
  @SafeVarargs
  public TPBallSkill(
      final Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      float range,
      float speed,
      Tuple<Resource, Integer>... resourceCost) {
    this(target, tpTarget, cooldown, range, speed, DAMAGE_AMOUNT, IGNORE_FIRST_WALL, resourceCost);
  }

  @Override
  protected void additionalEffectAfterDamage(
      Entity caster, Entity projectile, Entity target, Direction direction) {
    EntityUtils.teleportEntityTo(caster, tpTarget.get());
  }

  /**
   * Called when the projectile spawns in the game world.
   *
   * <p>Plays the teleport ball sound effect with a random pitch for variation.
   *
   * @param caster The entity casting the projectile.
   * @param projectile The projectile entity spawned.
   */
  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(PROJECTILE_SOUND.pathString()));

    float minPitch = 2f;
    float maxPitch = 3f;
    float randomPitch = MathUtils.random(minPitch, maxPitch);

    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, randomPitch);
    soundEffect.setVolume(soundId, 0.05f);
  }
}
