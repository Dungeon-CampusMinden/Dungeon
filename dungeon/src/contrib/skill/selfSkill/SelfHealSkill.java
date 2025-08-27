package contrib.skill.selfSkill;

import contrib.components.HealthComponent;
import contrib.skill.Resource;
import contrib.skill.Skill;
import core.Entity;
import core.utils.Tuple;

public class SelfHealSkill extends Skill {

  protected int healAmount;

  public SelfHealSkill(
      String name, long cooldown, int healAmount, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.healAmount = healAmount;
  }

  @Override
  protected void executeSkill(Entity caster) {
    caster.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount()));
    // TODO: add visual feedback (e.g., blink) when self-healing
  }

  public int healAmount() {
    return healAmount;
  }

  public void healAmount(int healAmount) {
    this.healAmount = healAmount;
  }
}
