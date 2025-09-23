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

  /** Name of the Skill. */
  public static final String SKILL_NAME = "FIREBALL";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 7f;
  private static final long COOLDOWN = 500;
  private static final boolean IS_PIERCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;

  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;

  /**
   * Creates a fully customized fireball skill with a custom name.
   *
   * <p>This constructor allows for subclassing and customization of the fireball skill, including
   * its name, target selection, cooldown, speed, range, damage amount, and resource costs.
   *
   * @param name Name of the skill.
   * @param target Function providing the target point.
   * @param cooldown Cooldown in ms.
   * @param speed Travel speed of the projectile.
   * @param range Maximum travel range.
   * @param damageAmount Base damage dealt.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  protected FireballSkill(
      String name,
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
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
  }

  /**
   * Creates a fully customized fireball skill.
   *
   * @param target Function providing the target point.
   * @param cooldown Cooldown in ms.
   * @param speed Travel speed of the projectile.
   * @param range Maximum travel range.
   * @param damageAmount Base damage dealt.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  public FireballSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    this(SKILL_NAME, target, cooldown, speed, range, damageAmount, ignoreFirstWall, resourceCost);
  }

  /**
   * Creates a fireball skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown time (in ms) before the skill can be used again.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public FireballSkill(
      Supplier<Point> targetSelection, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, cooldown, SPEED, RANGE, DAMAGE, IGNORE_FIRST_WALL, resourceCost);
  }

  /**
   * Creates a fireball skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown time (in ms) before the skill can be used again.
   * @param range Maximum travel range.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public FireballSkill(
      Supplier<Point> targetSelection,
      long cooldown,
      float range,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, cooldown, SPEED, range, DAMAGE, ignoreFirstWall, resourceCost);
  }

  /**
   * Creates a fireball skill with default values.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public FireballSkill(Supplier<Point> targetSelection, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, COOLDOWN, resourceCost);
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
