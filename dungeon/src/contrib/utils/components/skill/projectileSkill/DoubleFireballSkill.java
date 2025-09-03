package contrib.utils.components.skill.projectileSkill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import core.Entity;
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
public class DoubleFireballSkill extends FireballSkill {

  private static final String SKILL_NAME = "DOUBLE_FIREBALL";

  private static final int DELAY_BETWEEN_FIREBALLS = 50;

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
    Supplier<Point> target,
    long cooldown,
    float speed,
    float range,
    int damageAmount) {
    super(SKILL_NAME, target, cooldown, speed, range, damageAmount);
  }

  /**
   * Create a new default {@link DamageProjectileSkill} based on {@link FireballSkill}.
   *
   * @param target Function providing the target point.
   */
  public DoubleFireballSkill(Supplier<Point> target) {
    super(SKILL_NAME, target);
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
