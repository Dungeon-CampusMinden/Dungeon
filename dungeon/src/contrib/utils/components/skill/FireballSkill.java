package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.components.health.DamageType;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
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

  public static final float PROJECTILE_SPEED = 15.0f;
  public static final int DAMAGE_AMOUNT = 2;
  public static final float PROJECTILE_RANGE = 7f;
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Point HIT_BOX_SIZE = new Point(1, 1);

  /**
   * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectile
   */
  public FireballSkill(final Supplier<Point> targetSelection) {
    super(
        "fireball",
        PROJECTILE_TEXTURES,
        PROJECTILE_SPEED,
        DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        HIT_BOX_SIZE,
        targetSelection,
        PROJECTILE_RANGE);
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
        "fireball",
        PROJECTILE_TEXTURES,
        speed,
        damageAmount,
        DAMAGE_TYPE,
        HIT_BOX_SIZE,
        targetSelection,
        range);
  }

  @Override
  protected Sound playSound() {
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
    return soundEffect;
  }
}
