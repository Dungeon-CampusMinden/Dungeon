package contrib.utils.components.skill;

import contrib.components.HealthComponent;
import core.Entity;

public class SelfHealSkill extends HealSkill {

  public SelfHealSkill(int healAmount, long cooldown) {
    super("Heal", cooldown, healAmount);
  }

  @Override
  protected void heal(Entity caster) {
    caster.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount));
    // TODO make player blink if he heals itself
  }
}
