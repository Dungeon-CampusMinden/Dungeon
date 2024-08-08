package entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.DamageProjectile;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.BurningEffect;
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
public final class BurningFireballSkill extends DamageProjectile {
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float PROJECTILE_SPEED = 15.0f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Point HIT_BOX_SIZE = new Point(1, 1);
  private static final BurningEffect BURNING_EFFECT = new BurningEffect(1f, 1);

  /**
   * The range of the projectile. If the projectile has traveled this distance, it will be removed
   * from the game.
   */
  public static float PROJECTILE_RANGE = 7f;

  /** Whether the burning effect is unlocked. */
  public static boolean UNLOCKED = false;

  /** The amount of damage the fireball will deal. */
  public static int DAMAGE_AMOUNT = 2;

  /**
   * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectile
   */
  public BurningFireballSkill(final Supplier<Point> targetSelection) {
    super(
        "burningfireball",
        PROJECTILE_TEXTURES,
        PROJECTILE_SPEED,
        DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        HIT_BOX_SIZE,
        targetSelection,
        PROJECTILE_RANGE,
        DamageProjectile.DEFAULT_ON_WALL_HIT,
        (projectile, entity) -> {
          if (UNLOCKED) BURNING_EFFECT.applyBurning(entity);
        });
  }

  @Override
  public int tintColor() {
    return UNLOCKED ? 0xFF9999FF : -1;
  }

  @Override
  protected void playSound() {
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
  }
}
