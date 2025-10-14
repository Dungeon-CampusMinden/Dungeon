package skills;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * An energy pellet projectile skill that deals damage on impact.
 *
 * <p>This class extends {@link DamageProjectileSkill} to implement an energy pellet-specific skill.
 * The projectile will travel toward a target point, deal damage on collision with an entity, and be
 * removed if it hits a wall or reaches its maximum range.
 *
 * <p>This projectile is intended to be used in combination with an energyPellet- Catcher and
 * Launcher.
 */
public class EnergyPelletSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "ENERGY_PELLET";

  private static final IPath TEXTURE = new SimpleIPath("skills/energy_pellet");
  private static final float SPEED = 13f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 7f;
  private static final long COOLDOWN = 500;
  private static final boolean IS_PIERCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;

  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;

  /**
   * Creates a fully customized energy pellet skill with a custom name.
   *
   * <p>This constructor allows for subclassing and customization of the energy pellet skill,
   * including its name, target selection, cooldown, speed, range, damage amount, and resource
   * costs.
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
  protected EnergyPelletSkill(
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
   * Creates a fully customized energy pellet skill.
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
  public EnergyPelletSkill(
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
   * Creates an energy pellet skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param cooldown Cooldown time (in ms) before the skill can be used again.
   * @param range Maximum travel range.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      Supplier<Point> targetSelection,
      long cooldown,
      float range,
      Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, cooldown, SPEED, range, DAMAGE, IGNORE_FIRST_WALL, resourceCost);
  }

  /**
   * Creates an energy pellet skill with default values and custom cooldown.
   *
   * @param targetSelection Function providing the target point where the fireball should fly.
   * @param resourceCost Resource costs (e.g., mana, energy) required to use the skill.
   */
  @SafeVarargs
  public EnergyPelletSkill(
      Supplier<Point> targetSelection, Tuple<Resource, Integer>... resourceCost) {
    this(targetSelection, COOLDOWN, RANGE, resourceCost);
  }
}
