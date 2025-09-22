package contrib.utils.components.skill.projectileSkill;

import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;

/** Launches a fireball in every direction around the boss. Sort of like a fire spin attack. */
public class FireballStormSkill extends FireballSkill {

  private static final String SKILL_NAME = "FIRESTORM";

  private static final Supplier<Point> TARGET =
      () -> new Point(0, 0); // Target is not used in this skill
  private static final boolean IGNORE_FIRST_WALL = false;

  private final int delayBetweenFireballs;
  private final int totalFireballs;

  /**
   * Create a new customized {@link DamageProjectileSkill}.
   *
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param damageAmount The base damage dealt by the projectile.
   * @param totalFireBalls The total number of fireballs to shoot.
   * @param delayBetweenFireballs The delay between each fireball.
   */
  public FireballStormSkill(
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      int totalFireBalls,
      int delayBetweenFireballs) {
    super(SKILL_NAME, TARGET, cooldown, speed, range, damageAmount, IGNORE_FIRST_WALL);
    this.delayBetweenFireballs = delayBetweenFireballs;
    this.totalFireballs = totalFireBalls;
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point casterPosition = start(caster);

    for (int i = 0; i < totalFireballs; i++) {
      final int degree = i * 360 / totalFireballs;
      EventScheduler.scheduleAction(
          () -> {
            Vector2 direction = Direction.UP.rotateDeg(degree);
            Point target = casterPosition.translate(direction.scale(this.range() * 0.5f));
            shootProjectile(caster, casterPosition, target);
          },
          (long) i * delayBetweenFireballs);
    }
  }
}
