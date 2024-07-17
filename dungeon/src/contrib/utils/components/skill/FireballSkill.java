package contrib.utils.components.skill;

import contrib.utils.components.health.DamageType;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.sound.SoundPlayer;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectile}.
 *
 * <p>The FireballSkill class extends the functionality of {@link DamageProjectile} to implement the
 * specific behavior of the fireball skill. *
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will deal damage and
 * be removed from the game. It will also be removed from the game if it hits a wall or has reached
 * the maximum distance.
 */
public final class FireballSkill extends DamageProjectile {

  private static final String SKILL_NAME = "fireball";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float DEFAULT_PROJECTILE_SPEED = 15.0f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 5;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Point HIT_BOX_SIZE = new Point(1, 1);

  /**
   * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectile
   */
  public FireballSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection, DEFAULT_PROJECTILE_RANGE, DEFAULT_PROJECTILE_SPEED, DEFAULT_DAMAGE_AMOUNT);
  }

  /**
   * Creates a new FireballSkill with the specified target selection, range, speed, and damage
   * amount. The target selection is a function used to select the point where the projectile should
   * fly to. The range is the maximum distance the projectile can travel. The speed is the speed at
   * which the projectile travels. The damage amount is the amount of damage the projectile will
   * deal upon impact.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @param range The maximum distance the projectile can travel.
   * @param speed The speed at which the projectile travels.
   * @param damageAmount The amount of damage the projectile will deal upon impact.
   */
  public FireballSkill(
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

  @Override
  protected void playSound() {
    // Generate a projectile sound with .05f volume and a random pitch between 2f and 3f
    SoundPlayer.playSound(PROJECTILE_SOUND, false, 0.05f, 2f, 3f);
  }
}
