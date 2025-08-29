package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

public class FireConeSkill extends DamageProjectileSkill {

  /*
     return new Skill(
     (skillUser) -> {
       Point heroPos = EntityUtils.getHeroPosition();
       if (heroPos == null) {
         return;
       }
       Point bossPos =
           skillUser
               .fetch(PositionComponent.class)
               .orElseThrow(
                   () -> MissingComponentException.build(skillUser, PositionComponent.class))
               .position();
       Vector2 direction = bossPos.vectorTo(heroPos).normalize();

       // Function to calculate the fireball target position
       Function<Integer, Point> calculateFireballTarget =
           (angle) -> {
             Vector2 offset =
                 direction.rotateDeg(angle).scale(bossPos.vectorTo(heroPos).length());
             return bossPos.translate(offset);
           };

       Consumer<Integer> launchFireBallWithDegree =
           (degreeValue) ->
               launchFireBall(
                   bossPos,
                   calculateFireballTarget.apply(degreeValue),
                   bossPos,
                   skillUser,
                   FIREBALL_MAX_RANGE,
                   fireballSpeed,
                   fireballDamage);

       // Launch fireballs
       launchFireBallWithDegree.accept(degree);
       launchFireBallWithDegree.accept(-degree);
       launchFireBallWithDegree.accept(0);

       // Schedule another round of fireballs
       EventScheduler.scheduleAction(
           () -> {
             launchFireBallWithDegree.accept(degree - 5);
             launchFireBallWithDegree.accept(-(degree - 5));
             launchFireBallWithDegree.accept(0);
           },
           delayMillis);
     },
     AIFactory.FIREBALL_COOL_DOWN * 2);
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
  public FireConeSkill(
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

  public FireConeSkill(int degree, int delayMillis, float fireballSpeed, int fireballDamage) {
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
