package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** Launches a fireball in every direction around the boss. Sort of like a fire spin attack. */
public class FireStormSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "Firestorm";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 25f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final long COOLDOWN = 500;
  private static final boolean IS_PIRCING = false;

  private int delayBetweenFireballs;
  private int totalFireballs;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param pircing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param hitBoxSize The hitbox size of the projectile used for collision detection.
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  public FireStormSkill(
      long cooldown,
      IPath texture,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      int totalFireBalls,
      int delayBetweenFireballs,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldown,
        texture,
        () -> new Point(0, 0),
        speed,
        range,
        pircing,
        damageAmount,
        damageType,
        hitBoxSize,
        resourceCost);
    this.delayBetweenFireballs = delayBetweenFireballs;
    this.totalFireballs = totalFireBalls;
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point casterPosition = start(caster);

    for (int i = 0; i < totalFireballs; i++) {
      final int degree = i * 360 / totalFireballs;
      EventScheduler.scheduleAction(
          () -> {
            Vector2 direction = Direction.UP.rotateDeg(degree);
            Point target = casterPosition.translate(direction.scale(RANGE * 0.5f));
            shootProjectile(caster, casterPosition, target);
          },
          (long) i * delayBetweenFireballs);
    }
  }

  /**
   * Launches a fireball in every direction. Sort of like a fire spin attack.
   *
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   */
  public FireStormSkill(int totalFireBalls, int delayBetweenFireballs) {
    this(
        COOLDOWN,
        TEXTURE,
        SPEED,
        RANGE,
        IS_PIRCING,
        DAMAGE,
        DamageType.FIRE,
        DEFAULT_HITBOX_SIZE,
        totalFireBalls,
        delayBetweenFireballs);
  }

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
