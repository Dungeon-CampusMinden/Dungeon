package contrib.utils.components.skill.damageSkill.projectile;

import contrib.utils.components.health.DamageType;
import core.utils.Point;
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

  private static final String SKILL_NAME = "Bow";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/bow");
  private static final float DEFAULT_PROJECTILE_SPEED = 13f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 2;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);

  private static final long BOW_COOLDOWN = 500;

  /**
   * Create a {@link DamageProjectileSkill} that looks like an arrow and will cause physical damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public BowSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection, DEFAULT_PROJECTILE_RANGE, DEFAULT_PROJECTILE_SPEED, DEFAULT_DAMAGE_AMOUNT);
  }

  /**
   * Creates a new BowSkill with the specified target selection, range, speed, and damage amount.
   * The target selection is a function used to select the point where the projectile should fly to.
   * The range is the maximum distance the projectile can travel. The speed is the speed at which
   * the projectile travels. The damage amount is the amount of damage the projectile will deal upon
   * impact.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @param range The maximum distance the projectile can travel.
   * @param speed The speed at which the projectile travels.
   * @param damageAmount The amount of damage the projectile will deal upon impact.
   */
  public BowSkill(
      final Supplier<Point> targetSelection, float range, float speed, int damageAmount) {
    super(
        SKILL_NAME,
        BOW_COOLDOWN,
        targetSelection,
        damageAmount,
        DAMAGE_TYPE,
        PROJECTILE_TEXTURES,
        speed,
        range,
        HIT_BOX_SIZE,
        DEFAULT_ON_WALL_HIT,
        DEFAULT_ON_SPAWN,
        DEFAULT_BONUS_EFFECT);
  }
}
