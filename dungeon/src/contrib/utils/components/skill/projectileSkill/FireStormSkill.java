package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/** Launches a fireball in every direction around the boss. Sort of like a fire spin attack. */
public class FireStormSkill extends DamageProjectileSkill {

  /*
    return new Skill(
     (skillUser) -> {
       // Fire Storm
       Point bossPos =
           skillUser
               .fetch(PositionComponent.class)
               .orElseThrow(
                   () -> MissingComponentException.build(skillUser, PositionComponent.class))
               .position();

       for (int i = 0; i < totalFireBalls; i++) {
         final int degree = i * 360 / totalFireBalls;
         EventScheduler.scheduleAction(
             () -> {
               Vector2 direction = Direction.UP.rotateDeg(degree);
               Point target = bossPos.translate(direction.scale(FIREBALL_MAX_RANGE * 0.5f));
               launchFireBall(bossPos, target, bossPos, skillUser);
             },
             (long) i * delayBetweenFireballs);
       }
     },
     AIFactory.FIREBALL_COOL_DOWN * 4);
  */

  /**
   * Create a new {@link DamageProjectileSkill}.
   *
   * @param name The name of the skill.
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
   * @param resourceCost The resource cost (e.g., mana, energy, arrows) required to use this skill.
   */
  public FireStormSkill(
      String name,
      long cooldown,
      IPath texture,
      Supplier<Point> end,
      float speed,
      float range,
      boolean pircing,
      int damageAmount,
      DamageType damageType,
      Vector2 hitBoxSize,
      Tuple<Resource, Integer>... resourceCost) {
    super(
        name,
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
  }

  /**
   * Launches a fireball in every direction. Sort of like a fire spin attack.
   *
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   */
  public FireStormSkill(int totalFireBalls, int delayBetweenFireballs) {
    this(
        "",
        0,
        new SimpleIPath(""),
        new Supplier<Point>() {
          @Override
          public Point get() {
            return new Point(0, 0);
          }
        },
        0,
        0,
        false,
        0,
        DamageType.FIRE,
        Vector2.ZERO);
  }
}
