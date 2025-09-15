package contrib.utils.components.skill.projectileSkill;

import contrib.utils.BurningEffect;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import java.util.function.Supplier;

/**
 * Subclass of {@link contrib.utils.components.skill.projectileSkill.DamageProjectileSkill}.
 *
 * <p>The FireballSkill class extends the functionality of {@link FireballSkill} to implement the
 * specific behavior of the fireball skill.
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will deal damage and
 * be removed from the game. It will also be removed from the game if it hits a wall or has reached
 * the maximum distance.
 */
public final class BurningFireballSkill extends FireballSkill {
  private static final BurningEffect BURNING_EFFECT = new BurningEffect(1f, 1);
  private static final float PROJECTILE_SPEED = 13f;
  private static final long COOLDOWN = 500;
  private static final boolean IGNORE_FIRST_WALL = false;

  /**
   * The range of the projectile. If the projectile has traveled this distance, it will be removed
   * from the game.
   */
  public static float PROJECTILE_RANGE = 7f;

  /** The amount of damage the fireball will deal. */
  public static int DAMAGE_AMOUNT = 2;

  /**
   * Create a {@link FireballSkill} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   */
  public BurningFireballSkill(final Supplier<Point> targetSelection) {
    super(
        targetSelection,
        COOLDOWN,
        PROJECTILE_SPEED,
        PROJECTILE_RANGE,
        DAMAGE_AMOUNT,
        IGNORE_FIRST_WALL);
    this.name = "Burning Fireball";
  }

  @Override
  protected void additionalEffectAfterDamage(
      Entity caster, Entity projectile, Entity target, Direction direction) {
    BURNING_EFFECT.applyBurning(target);
  }

  @Override
  public int tintColor() {
    return 0xFF9999FF; // orange
  }
}
