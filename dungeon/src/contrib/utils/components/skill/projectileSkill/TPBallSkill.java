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
 * Subclass of {@link DamageProjectileSkill}.
 *
 * <p>The TPBallSkill class extends the functionality of {@link DamageProjectileSkill} to implement
 * the specific behavior of a teleporting ball skill.
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will teleport the
 * entity to a random or specific location. It will also be removed from the game if it hits a wall
 * or has reached the maximum distance.
 */
public class TPBallSkill extends DamageProjectileSkill {
  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float PROJECTILE_SPEED = 6.0f;
  private static final int DAMAGE_AMOUNT = 1;
  private static final DamageType DAMAGE_TYPE = DamageType.MAGIC;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final float PROJECTILE_RANGE = 7f;
  private static final long COOLDOWN = 2000;
  public static final String SKILL_NAME = "TPBall";
  public static final boolean IS_PIRCING = false;

  public TPBallSkill(
      Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldown,
        TEXTURE,
        target,
        speed,
        range,
        IS_PIRCING,
        damageAmount,
        DAMAGE_TYPE,
        (projectile, target1, direction) -> EntityUtils.teleportEntityTo(target1, tpTarget.get()),
        HIT_BOX_SIZE,
        resourceCost);
    tintColor(0xFF00FFFF);
  }

  /** Uses all default values: cooldown, range, speed, and damage. */
  public TPBallSkill(
      Supplier<Point> target, Supplier<Point> tpTarget, Tuple<Resource, Integer>... resourceCost) {
    this(
        target,
        tpTarget,
        COOLDOWN,
        PROJECTILE_RANGE,
        PROJECTILE_SPEED,
        DAMAGE_AMOUNT,
        resourceCost);
  }

  /** Uses default speed, range, and damage but custom cooldown. */
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
        resourceCost);
  }

  /** Uses default damage, but allows custom cooldown, range, and speed. */
  public TPBallSkill(
      final Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      float range,
      float speed,
      Tuple<Resource, Integer>... resourceCost) {
    this(target, tpTarget, cooldown, range, speed, DAMAGE_AMOUNT, resourceCost);
  }

  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
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
