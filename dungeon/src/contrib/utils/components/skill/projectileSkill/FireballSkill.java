package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.*;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * A fireball projectile skill that deals fire damage on impact.
 *
 * <p>This class extends {@link DamageProjectileSkill} to implement a fireball-specific skill. The
 * projectile will travel toward a target point, deal fire damage on collision with an entity, and
 * be removed if it hits a wall or reaches its maximum range.
 */
public class FireballSkill extends DamageProjectileSkill {

  public static final String SKILL_NAME = "FIREBALL";
  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final long COOLDOWN = 500;
  private static final boolean IS_PIRCING = false;

  /**
   * Creates a fireball skill with default cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public FireballSkill(Supplier<Point> targetSelection, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, COOLDOWN, resourceCost);
  }

  /**
   * Creates a fireball skill with a custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown in milliseconds for the skill.
   * @param resourceCost Resource costs required to use the skill.
   */
  @SafeVarargs
  public FireballSkill(
      Supplier<Point> targetSelection, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, cooldown, SPEED, RANGE, DAMAGE, resourceCost);
  }

  /**
   * Creates a fully customized fireball skill.
   *
   * @param target Function providing the target point.
   * @param cooldown Cooldown in ms.
   * @param speed Travel speed of the projectile.
   * @param range Maximum travel range.
   * @param damageAmount Base damage dealt.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  public FireballSkill(
      Supplier<Point> target,
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
        HIT_BOX_SIZE,
        resourceCost);
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
