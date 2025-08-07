package contrib.utils.components.skill;

import contrib.utils.components.health.DamageType;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectile}.
 *
 * <p>The BowSkill class extends the functionality of {@link DamageProjectile} to implement the
 * specific behavior of the bow skill. *
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will deal damage and
 * be removed from the game. It will also be removed from the game if it hits a wall or has reached
 * the maximum distance.
 *
 * <p>To use the BowSkill, the player needs bow and arrow in their inventory.
 */
public class BowSkill extends DamageProjectile {

  private static final String SKILL_NAME = "Bow";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/bow");
  private static final float DEFAULT_PROJECTILE_SPEED = 13f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 2;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);

  public BowSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection, DEFAULT_PROJECTILE_RANGE, DEFAULT_PROJECTILE_SPEED, DEFAULT_DAMAGE_AMOUNT);
  }

  public BowSkill(
      final Supplier<Point> targetSelection, float range, float speed, int damageAmount) {
    super(
        SKILL_NAME,
        PROJECTILE_TEXTURES,
        speed,
        damageAmount,
        DAMAGE_TYPE,
        HIT_BOX_SIZE,
        targetSelection,
        range);
  }
}
