package contrib.utils.components.skill.selfSkill;

import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.components.VelocityComponent;
import core.utils.Tuple;

public class DashSkill extends Skill {
  public static final String NAME = "Sprint";
  protected float scalar;
  protected int duration;

  /**
   * Creates a new skill with the given parameters.
   *
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
