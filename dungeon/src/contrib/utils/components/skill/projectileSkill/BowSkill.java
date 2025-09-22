package contrib.utils.components.skill.projectileSkill;

import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectileSkill}.
 *
 * <p>The BowSkill class extends the functionality of {@link DamageProjectileSkill} to implement the
 * specific behavior of the bow skill. *
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will deal damage and
 * be removed from the game. It will also be removed from the game if it hits a wall or has reached
 * the maximum distance.
 *
 * <p>To use the BowSkill, the player needs bow and arrow in their inventory.
 */
public class BowSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String SKILL_NAME = "BOW_SKILL";

  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("items/weapon/wooden_arrow.png");
  private static final float DEFAULT_PROJECTILE_SPEED = 13f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 2;
  private static final boolean IS_PIRCING = false;
  private static final boolean IGNORE_FIRST_WALL = false;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final Tuple<Resource, Integer> COST = new Tuple<>(Resource.ARROW, 1);
  private static final long BOW_COOLDOWN = 500;
  private double stickInWallProbability = 0.1;

  /**
   * Create a new Bow Skill.
   *
   * @param target Supplier that gives the target Point for the arrow.
   * @param cooldown cooldown between two arrows
   * @param speed speed of the arrow
   * @param range range of the arrow
   * @param damageAmount damage of the arrow; will be Physical
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost resource cost of the arrow
   */
  @SafeVarargs
  public BowSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldown,
        PROJECTILE_TEXTURES,
        target,
        speed,
        range,
        IS_PIRCING,
        damageAmount,
        DAMAGE_TYPE,
        ignoreFirstWall,
        resourceCost);
  }

  /**
   * Create a {@link DamageProjectileSkill} that looks like an arrow and will cause physical damage.
   *
   * <p>This will consume one arrow from the inventory.
   *
   * @param targetSelection A Supplier used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public BowSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection,
        BOW_COOLDOWN,
        DEFAULT_PROJECTILE_SPEED,
        DEFAULT_PROJECTILE_RANGE,
        DEFAULT_DAMAGE_AMOUNT,
        IGNORE_FIRST_WALL,
        COST);
  }

  /**
   * Create a {@link DamageProjectileSkill} that looks like an arrow and will cause physical damage.
   *
   * <p>This variant does NOT require or consume any resource and is intended for automated entities
   * such as the projectileLaunchingSentry.
   *
   * @param target A Supplier used to select the point where the projectile should fly to.
   * @param cooldown cooldown between two arrows.
   * @param range range of the arrow.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @see DamageProjectileSkill
   */
  public BowSkill(
      final Supplier<Point> target, long cooldown, float range, boolean ignoreFirstWall) {
    super(
        SKILL_NAME,
        cooldown,
        PROJECTILE_TEXTURES,
        target,
        DEFAULT_PROJECTILE_SPEED,
        range,
        IS_PIRCING,
        DEFAULT_DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        ignoreFirstWall);
  }

  @Override
  protected void onWallHit(Entity caster, Entity projectile) {
    if (RANDOM.nextDouble() < stickInWallProbability) {
      projectile
          .fetch(PositionComponent.class)
          .ifPresent(
              projectilePos ->
                  new ItemWoodenArrow()
                      .drop(projectilePos.position())
                      .flatMap(arrow -> arrow.fetch(PositionComponent.class))
                      .ifPresent(arrowPos -> arrowPos.rotation(projectilePos.rotation())));
    }
    Game.remove(projectile);
  }

  /**
   * Returns the probability that a projectile will stick in a wall.
   *
   * @return the probability in the range {@code [0.0, 1.0]}
   */
  public double stickInWallProbability() {
    return stickInWallProbability;
  }

  /**
   * Sets the probability that a projectile will stick in a wall.
   *
   * @param probability the probability in the range {@code [0.0, 1.0]}
   * @throws IllegalArgumentException if {@code probability} is outside the valid range
   */
  public void stickInWallProbability(double probability) {
    if (probability < 0.0 || probability > 1.0) {
      throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
    }
    this.stickInWallProbability = probability;
  }
}
