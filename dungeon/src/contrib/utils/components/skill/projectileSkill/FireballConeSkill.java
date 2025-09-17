package contrib.utils.components.skill.projectileSkill;

import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Shoots a fire cone towards the hero. The fire cone consists of six fireballs.
 *
 * <ul>
 *   <li>One fireball directly at the hero.
 *   <li>Two fireballs to the left and right of the hero. (X degrees)
 *   <li>One delayed fireball directly at the hero. With updated hero position.
 *   <li>Two delayed fireballs left and right offset to that previous fireball. (X-5 degrees)
 * </ul>
 */
public class FireballConeSkill extends FireballSkill {

  private static final String SKILL_NAME = "FIRECONE";
  private static final boolean IGNORE_FIRST_WALL = false;

  private final int delayMillis;
  private final int degree;

  /**
   * Create a new customized {@link DamageProjectileSkill}.
   *
   * @param target Function providing the target point.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param damageAmount The base damage dealt by the projectile.
   * @param degree The degree of the fire cone.
   * @param delayMillis The delay between the first and second round of fireballs.
   */
  public FireballConeSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      int degree,
      int delayMillis) {
    super(SKILL_NAME, target, cooldown, speed, range, damageAmount, IGNORE_FIRST_WALL);
    this.degree = degree;
    this.delayMillis = delayMillis;
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point targetPos = end(caster);
    if (targetPos == null) {
      return;
    }
    Point casterPos = start(caster);
    Vector2 direction = casterPos.vectorTo(targetPos).normalize();

    // Function to calculate the fireball target position
    Function<Integer, Point> calculateFireballTarget =
        (angle) -> {
          Vector2 offset = direction.rotateDeg(angle).scale(casterPos.vectorTo(targetPos).length());
          return casterPos.translate(offset);
        };

    Consumer<Integer> launchFireBallWithDegree =
        (degreeValue) ->
            shootProjectile(caster, casterPos, calculateFireballTarget.apply(degreeValue));

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
  }
}
