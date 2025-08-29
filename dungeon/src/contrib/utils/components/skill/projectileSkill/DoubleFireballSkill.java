package contrib.utils.components.skill.projectileSkill;

import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;

/**
 * An enchantment version of a normal attack. Shoots two fireballs at the hero. One directly at the
 * hero and one is trying to predict the hero's movement.
 */
public class DoubleFireballSkill extends DamageProjectileSkill {

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
          launchFireBall(bossPos, heroPos, bossPos, skillUser);
          EventScheduler.scheduleAction(
              () -> {
                Point heroPos2 = EntityUtils.getHeroPosition();
                if (heroPos2 == null) {
                  return;
                }
                Vector2 heroDirection = heroPos.vectorTo(heroPos2).normalize();
                heroDirection = heroDirection.scale((float) (bossPos.distance(heroPos)) * 2);
                Point predictedHeroPos = heroPos2.translate(heroDirection);
                launchFireBall(bossPos, predictedHeroPos, bossPos, skillUser);
              },
              50L);
        },
        coolDown);
  }
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
  public DoubleFireballSkill(
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
   * An enchantment version of a normal attack. Shoots two fireballs at the hero. One directly at
   * the hero and one is trying to predict the hero's movement.
   *
   * @param cooldown The cool down of the skill.
   */
  public DoubleFireballSkill(long cooldown) {
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
