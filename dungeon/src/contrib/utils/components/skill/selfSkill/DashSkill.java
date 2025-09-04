package contrib.utils.components.skill.selfSkill;

import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.components.VelocityComponent;
import core.utils.Tuple;

/**
 * A skill that temporarily increases the caster's maximum movement speed.
 *
 * <p>When activated, the caster's {@link VelocityComponent#maxSpeed()} is multiplied by the given
 * scalar for a limited duration. After the duration ends, the original speed is restored and the
 * caster briefly blinks to signal the effect wearing off.
 *
 * <p>The skill is automatically put on cooldown for at least {@code duration + 1} ticks, ensuring
 * that the effect cannot overlap with itself.
 */
public class DashSkill extends Skill {
  /** Name of the skill. */
  public static final String NAME = "Sprint";

  protected float scalar;
  protected int duration;

  /**
   * Creates a new dash skill with the given parameters.
   *
   * @param scalar the factor by which the caster's maximum speed is multiplied (e.g. {@code 1.5f}
   *     increases speed by 50%)
   * @param cooldown the cooldown time (in ticks) before the skill can be reused; will be at least
   *     {@code duration + 1} to prevent overlap
   * @param duration the time (in ticks) that the increased speed lasts
   * @param resources the resources and their required amounts, provided as {@link Tuple}s
   */
  public DashSkill(
      float scalar, int cooldown, int duration, Tuple<Resource, Integer>... resources) {
    super(NAME, Math.max(cooldown, duration + 1), resources);
    this.duration = duration;
    this.scalar = scalar;
  }

  @Override
  protected void executeSkill(Entity caster) {
    caster
        .fetch(VelocityComponent.class)
        .ifPresent(
            vc -> {
              float oldMaxSpeed = vc.maxSpeed();
              vc.maxSpeed(vc.maxSpeed() * scalar);
              EventScheduler.scheduleAction(
                  new IAction() {
                    @Override
                    public void execute() {
                      vc.maxSpeed(oldMaxSpeed);
                      SkillTools.blink(caster, 0x0000FFFF, duration, 3);
                    }
                  },
                  duration);
            });
  }
}
