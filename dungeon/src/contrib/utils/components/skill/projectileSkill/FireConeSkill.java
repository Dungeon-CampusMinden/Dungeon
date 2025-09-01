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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Shoots a fire cone towards the hero. The fire cone consists of six fireballs.
 *
 * <ul>
 *   <li>One fireball directly at the hero.
 *   <li>Two fireballs to the left and right of the hero. (X degrees)
 *   <li>One delayed fireball directly at the hero. With updated hero position.
 *   <li>Two delayed fireballs left and right offset to that previous fireball. (X-5 degrees)
 * </ul>
 */
public class FireConeSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "Firecone";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final float RANGE = 25f;
  private static final long COOLDOWN = 1000;
  private static final boolean IS_PIRCING = false;
  private final int delayMillis;
  private final int degree;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
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
   * @param degree The degree of the fire cone.
   * @param delayMillis The delay between the first and second round of fireballs.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  @SafeVarargs
  public FireConeSkill(
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      int degree,
      int delayMillis,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
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
    this.degree = degree;
    this.delayMillis = delayMillis;
  }

  /**
   * Shoots a fire cone towards the hero. The fire cone consists of six fireballs.
   *
   * <ul>
   *   <li>One fireball directly at the hero.
   *   <li>Two fireballs to the left and right of the hero. (X degrees)
   *   <li>One delayed fireball directly at the hero. With updated hero position.
   *   <li>Two delayed fireballs left and right offset to that previous fireball. (X-5 degrees)
   * </ul>
   *
   * @param target Supplier to get the target position.
   * @param degree The degree of the fire cone.
   * @param delayMillis The delay between the first and second round of fireballs.
   * @param fireballSpeed The speed of the fireballs.
   * @param fireballDamage The damage of the fireballs.
   */
  public FireConeSkill(
      Supplier<Point> target,
      int degree,
      int delayMillis,
      float fireballSpeed,
      int fireballDamage) {
    this(
        COOLDOWN,
        TEXTURE,
        target,
        fireballSpeed,
        RANGE,
        IS_PIRCING,
        fireballDamage,
        DAMAGE_TYPE,
        DEFAULT_HITBOX_SIZE,
        degree,
        delayMillis);
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point targetPos = end(caster);
    if (targetPos == null) {
      return;
    }
    Point casterPos = start(caster);
    Vector2 direction = casterPos.vectorTo(targetPos).normalize();

    // Function to calculate the fireball target position
    Function<Integer, Point> calculateFireballTarget =
        (angle) -> {
          Vector2 offset = direction.rotateDeg(angle).scale(casterPos.vectorTo(targetPos).length());
          return casterPos.translate(offset);
        };

    Consumer<Integer> launchFireBallWithDegree =
        (degreeValue) ->
            shootProjectile(caster, casterPos, calculateFireballTarget.apply(degreeValue));

    // Launch fireballs
    launchFireBallWithDegree.accept(degree);
    launchFireBallWithDegree.accept(-degree);
    launchFireBallWithDegree.accept(0);

    // Schedule another round of fireballs
    EventScheduler.scheduleAction(
        () -> {
          launchFireBallWithDegree.accept(degree - 5);
          launchFireBallWithDegree.accept(-(degree - 5));
          launchFireBallWithDegree.accept(0);
        },
        delayMillis);
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
