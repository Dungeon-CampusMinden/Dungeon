package contrib.utils.components.skill.projectileSkill;

import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import java.util.function.Supplier;

/** Shoots a fire wall (made of fireballs). */
public class FireballWallSkill extends FireballSkill {

  private static final String SKILL_NAME = "FIREWALL";
  private static final boolean IGNORE_FIRST_WALL = false;

  private final int wallWidth;

  /**
   * Create a new customized {@link DamageProjectileSkill}.
   *
   * @param target Function providing the target point.
   * @param cooldown The cooldown time (in ms) before the skill can be used again.
   * @param speed The travel speed of the projectile.
   * @param range The maximum range the projectile can travel.
   * @param damageAmount The base damage dealt by the projectile.
   * @param wallWidth Width of the firewall
   */
  public FireballWallSkill(
      Supplier<Point> target,
      long cooldown,
      float speed,
      float range,
      int damageAmount,
      int wallWidth) {
    super(SKILL_NAME, target, cooldown, speed, range, damageAmount, IGNORE_FIRST_WALL);
    this.wallWidth = wallWidth;
  }

  @Override
  protected void executeSkill(Entity caster) {
    Point targetPos = end(caster);
    Point startPos = start(caster);
    Vector2 direction = targetPos.vectorTo(startPos).normalize();
    Vector2 right = direction.rotateDeg(90);
    Vector2 left = direction.rotateDeg(-90);
    for (int i = -wallWidth / 2; i < wallWidth / 2; i++) {
      if (i == 0) {
        shootProjectile(caster, startPos, targetPos);
      } else {
        shootProjectile(
            caster, startPos.translate(right.scale(i)), targetPos.translate(right.scale(i)));
        shootProjectile(
            caster, startPos.translate(left.scale(i)), targetPos.translate(left.scale(i)));
      }
    }
  }
}
