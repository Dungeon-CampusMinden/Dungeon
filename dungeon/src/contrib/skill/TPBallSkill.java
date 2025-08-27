package contrib.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.components.PlayerComponent;
import core.components.PositionComponent;
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
public class TPBallSkill {
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
   * @param tpTarget A supplier that provides the target point for teleportation.
   * @see DamageProjectileSkill
   */
  public static DamageProjectileSkill tpBallSkill(
      final Entity owner,
      final Supplier<Point> target,
      Supplier<Point> tpTarget,
      long cooldown,
      float range,
      float speed,
      int damageAmount) {
    DamageProjectileSkill skill =
        new DamageProjectileSkill(
            "tp",
            cooldown,
            () -> owner.fetch(PositionComponent.class).get().position(),
            target,
            PROJECTILE_TEXTURES,
            speed,
            range,
            HIT_BOX_SIZE,
            ProjectileSkill.DEFAULT_ON_WALL_HIT,
            ON_SPAWN_PLAY_SOUND,
            ProjectileSkill.DEFAULT_ON_TARGET_REACHED,
            ProjectileSkill.DEFAULT_ON_COLLIDE_LEAVE,
            owner,
            damageAmount,
            DAMAGE_TYPE,
            (projectile, entity) -> {
              // only tp hero (for now)
              EntityUtils.teleportEntityTo(entity, tpTarget.get());
            });
    skill.tintColor(0xFF00FFFF);
    return skill;
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
