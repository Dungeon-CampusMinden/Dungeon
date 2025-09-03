package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
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

  public static final String SKILL_NAME = "BOW";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/bow");
  private static final float DEFAULT_PROJECTILE_SPEED = 13f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 2;
  private static final boolean IS_PIRCING = false;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final Tuple<Resource, Integer> COST = new Tuple<>(Resource.ARROW, 1);
  private static final long BOW_COOLDOWN = 500;

  public BowSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
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
        HIT_BOX_SIZE,
        resourceCost);
  }

  /**
   * Create a {@link DamageProjectileSkill} that looks like an arrow and will cause physical damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public BowSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection,
        BOW_COOLDOWN,
        DEFAULT_PROJECTILE_RANGE,
        DEFAULT_PROJECTILE_SPEED,
        DEFAULT_DAMAGE_AMOUNT,
        COST);
  }
}
