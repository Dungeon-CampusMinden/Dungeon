package contrib.utils.components.skill.damageSkill.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.components.PlayerComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Subclass of {@link DamageProjectileSkill}.
 *
 * <p>The TPBallSkill class extends the functionality of {@link DamageProjectileSkill} to implement
 * the specific behavior of a teleporting ball skill.
 *
 * <p>The projectile will fly through the dungeon, and if it hits an entity, it will teleport the
 * entity to a random or specific location. It will also be removed from the game if it hits a wall
 * or has reached the maximum distance.
 */
public class TPBallSkill extends DamageProjectileSkill {
  private static final IPath PROJECTILE_TEXTURES = new SimpleIPath("skills/fireball");
  private static final IPath PROJECTILE_SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final float PROJECTILE_SPEED = 6.0f;
  private static final int DAMAGE_AMOUNT = 1;
  private static final DamageType DAMAGE_TYPE = DamageType.MAGIC;
  private static final Vector2 HIT_BOX_SIZE = Vector2.of(1, 1);
  private static final float PROJECTILE_RANGE = 7f;
  private static final Consumer<Entity> ON_SPAWN_PLAY_SOUND = entity -> playSound();
  private static final long COOLDOWN = 2000;

  /**
   * Create a {@link DamageProjectileSkill} that looks like a magic ball and will cause magic damage
   * and teleport the entity to a random or specific location.
   *
   * @param targetSelection A function used to select the point where the projectile should fly to.
   * @param tpTarget A supplier that provides the target point for teleportation.
   * @see DamageProjectileSkill
   */
  public TPBallSkill(final Supplier<Point> targetSelection, Supplier<Point> tpTarget) {
    super(
        "tpball",
        COOLDOWN,
        targetSelection,
        DAMAGE_AMOUNT,
        DAMAGE_TYPE,
        PROJECTILE_TEXTURES,
        PROJECTILE_SPEED,
        PROJECTILE_RANGE,
        HIT_BOX_SIZE,
        DamageProjectileSkill.DEFAULT_ON_WALL_HIT,
        ON_SPAWN_PLAY_SOUND,
        (projectile, entity) -> {
          // only tp hero (for now)
          if (entity.fetch(PlayerComponent.class).isEmpty()) {
            return;
          }
          EntityUtils.teleportEntityTo(entity, tpTarget.get());
        });
    tintColor(0xFF00FFFF);
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
