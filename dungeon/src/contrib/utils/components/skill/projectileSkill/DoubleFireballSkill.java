package contrib.utils.components.skill.projectileSkill;

import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;

/**
 * An enchantment version of a normal attack. Shoots two fireballs at the hero. One directly at the
 * hero and one is trying to predict the hero's movement.
 */
public class DoubleFireballSkill extends FireballSkill {

  private static final String SKILL_NAME = "DOUBLE_FIREBALL";

  private static final int DELAY_BETWEEN_FIREBALLS = 50;
  private static final boolean IGNORE_FIRST_WALL = false;

  /**
   * Create a new customized {@link DamageProjectileSkill}.
   *
   * @param target Function providing the target point.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param damageAmount The base damage dealt by the projectile.
   */
  public DoubleFireballSkill(
      Supplier<Point> target, long cooldown, float speed, float range, int damageAmount) {
    super(SKILL_NAME, target, cooldown, speed, range, damageAmount, IGNORE_FIRST_WALL);
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point targetPosition = end(caster);
    shootProjectile(caster, start(caster), end(caster));
    EventScheduler.scheduleAction(
        () -> {
          Point newTargetPosition = end(caster);
          Vector2 targetDirection = targetPosition.vectorTo(newTargetPosition).normalize();
          targetDirection =
              targetDirection.scale((float) (start(caster).distance(targetPosition)) * 2);
          Point predictedTargetPostion = newTargetPosition.translate(targetDirection);
          shootProjectile(caster, start(caster), predictedTargetPostion);
        },
        DELAY_BETWEEN_FIREBALLS);
  }
}
