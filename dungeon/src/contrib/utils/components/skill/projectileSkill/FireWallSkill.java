package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/** Shoots a fire wall (made of fireballs). */
public class FireWallSkill extends DamageProjectileSkill {

  /** Name of the Skill. */
  public static final String NAME = "Firewall";

  private static final IPath TEXTURE = new SimpleIPath("skills/fireball");
  private static final IPath SOUND = new SimpleIPath("sounds/fireball.wav");
  private static final long COOLDOWN = 1500;
  private static final float SPEED = 4.5f;
  private static final int DAMAGE = 2;
  private static final float RANGE = 25f;

  /** Size of the gap between two fireballs. */
  private static final float GAP_SIZE = 2.5f;

  private final int wallWidth;

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param texture The visual texture used for the projectile.
   * @param end A supplier providing the endpoint (target location) of the projectile.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param pircing Whether the projectile pierces through targets (true) or is destroyed on impact
   *     (false).
   * @param damageAmount The base damage dealt by the projectile.
   * @param damageType The type of damage inflicted by the projectile.
   * @param hitBoxSize The hitbox size of the projectile used for collision detection.
   * @param wallWidth Width of the firewall
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  @SafeVarargs
  public FireWallSkill(
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      int wallWidth,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        NAME,
        cooldown,
        texture,
        end,
        speed,
        range,
        pircing,
        damageAmount,
        damageType,
        hitBoxSize,
        resourceCost);
    this.wallWidth = wallWidth;
  }

  /**
   * Create a FireWallSkill.
   *
   * @param target Supplier to get the target Point of the projectiles.
   * @param wallWidth The width of the wall. The wall will be centered on the caster.
   */
  public FireWallSkill(Supplier<Point> target, int wallWidth) {
    this(
        COOLDOWN,
        TEXTURE,
        target,
        SPEED,
        RANGE,
        false,
        DAMAGE,
        DamageType.FIRE,
        DEFAULT_HITBOX_SIZE,
        wallWidth);
  }

  /**
   * Called when the fireball projectile spawns in the game world.
   *
   * <p>Plays a fireball sound effect with a random pitch for variation.
   *
   * @param caster The entity casting the fireball.
   * @param projectile The projectile entity spawned.
   */
  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(SOUND.pathString()));

    // Generate a random pitch between minPitch and maxPitch
    float minPitch = 2f;
    float maxPitch = 3f;
    float randomPitch = MathUtils.random(minPitch, maxPitch);

    // Play the sound with adjusted pitch and low volume
    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, randomPitch);
    soundEffect.setVolume(soundId, 0.05f);
  }
}
