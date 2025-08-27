package contrib.utils.components.skill;

import core.Entity;

public abstract class HealSkill extends Skill {

  protected int healAmount;

  public HealSkill(String name, long cooldown, int healAmount) {
    super(name, cooldown);
    this.healAmount = healAmount;
  }

  @Override
  protected void executeSkill(Entity caster) {
    heal(caster);
  }

  protected abstract void heal(Entity caster);
}
