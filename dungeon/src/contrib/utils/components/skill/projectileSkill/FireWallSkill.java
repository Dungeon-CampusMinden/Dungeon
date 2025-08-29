package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/** Shoots a fire wall (made of fireballs). */
public class FireWallSkill extends DamageProjectileSkill {

  /*
       return new Skill(
           (skillUser) -> {
             // Firewall
             Point heroPos = SkillTools.heroPositionAsPoint();
             Point bossPos =
                 skillUser
                     .fetch(PositionComponent.class)
                     .orElseThrow(
                         () -> MissingComponentException.build(skillUser, PositionComponent.class))
                     .position();
             Vector2 direction = heroPos.vectorTo(bossPos).normalize();
             // Main shoot is directly at the hero
             // every other fireball is offset left and right of the main shoot
             Vector2 right = direction.rotateDeg(90);
             Vector2 left = direction.rotateDeg(-90);
             for (int i = -wallWidth / 2; i < wallWidth / 2; i++) {
               if (i == 0) {
                 launchFireBall(bossPos, heroPos, bossPos, skillUser);
               } else {
                 launchFireBall(
                     bossPos.translate(right.scale(i)),
                     heroPos.translate(right.scale(i)),
                     bossPos,
                     skillUser);
                 launchFireBall(
                     bossPos.translate(left.scale(i)),
                     heroPos.translate(left.scale(i)),
                     bossPos,
                     skillUser);
               }
             }
           },
           AIFactory.FIREBALL_COOL_DOWN * 3);
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
  public FireWallSkill(
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
   * Create a FireWallSkill.
   *
   * @param wallWidth The width of the wall. The wall will be centered on the boss.
   */
  public FireWallSkill(int wallWidth) {
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
