package contrib.utils.components.skill.damageSkill.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectileSkill}.
 *
 * <p>The FireballSkill class extends the functionality of {@link DamageProjectileSkill} to
 * implement the specific behavior of the fireball skill. *
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will deal damage and
 * be removed from the game. It will also be removed from the game if it hits a wall or has reached
 * the maximum distance.
 */
public final class FireballSkill extends DamageProjectileSkill {

  private static final String SKILL_NAME = "fireball";
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float DEFAULT_PROJECTILE_SPEED = 13f;
  private static final int DEFAULT_DAMAGE_AMOUNT = 2;
  private static final float DEFAULT_PROJECTILE_RANGE = 7f;
  private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);

  private static final Consumer<Entity> ON_SPAWN_PLAY_SOUND = entity -> playSound();

  private static final long COOLDOWN = 500;

  /**
   * Create a {@link DamageProjectileSkill} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public FireballSkill(final Supplier<Point> targetSelection) {
    this(
        targetSelection, DEFAULT_PROJECTILE_RANGE, DEFAULT_PROJECTILE_SPEED, DEFAULT_DAMAGE_AMOUNT);
  }

  /**
   * Create a {@link DamageProjectileSkill} that looks like a fireball and will cause fire damage.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @see DamageProjectileSkill
   */
  public FireballSkill(final Supplier<Point> targetSelection, long cooldown) {
    this(
        targetSelection,
        DEFAULT_PROJECTILE_RANGE,
        DEFAULT_PROJECTILE_SPEED,
        DEFAULT_DAMAGE_AMOUNT,
        cooldown);
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
      final Supplier<Point> targetSelection,
      float range,
      float speed,
      int damageAmount,
      long cooldown) {
    super(
        SKILL_NAME,
        cooldown,
        targetSelection,
        damageAmount,
        DAMAGE_TYPE,
        PROJECTILE_TEXTURES,
        speed,
        range,
        HIT_BOX_SIZE,
        DEFAULT_ON_WALL_HIT,
        ON_SPAWN_PLAY_SOUND,
        DEFAULT_BONUS_EFFECT);
  }

  public FireballSkill(
      final Supplier<Point> targetSelection, float range, float speed, int damageAmount) {
    super(
        SKILL_NAME,
        COOLDOWN,
        targetSelection,
        damageAmount,
        DAMAGE_TYPE,
        PROJECTILE_TEXTURES,
        speed,
        range,
        HIT_BOX_SIZE,
        DEFAULT_ON_WALL_HIT,
        ON_SPAWN_PLAY_SOUND,
        DEFAULT_BONUS_EFFECT);
  }

  private static void playSound() {
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
