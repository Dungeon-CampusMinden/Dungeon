package contrib.utils.components.skill.selfSkill;

import contrib.components.HealthComponent;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.utils.Tuple;

/**
 * A self-targeted healing skill.
 *
 * <p>This skill restores health points to the caster based on its configured heal amount. It
 * consumes specified resources (e.g., mana, energy) and respects a cooldown before it can be used
 * again.
 */
public class SelfHealSkill extends Skill {

  /** The amount of health restored when the skill is executed. */
  protected int healAmount;

  /**
   * Creates a new self-healing skill.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param healAmount The amount of health restored when executed.
   * @param resourceCost Optional resource costs required to use this skill.
   */
  @SafeVarargs
  public SelfHealSkill(
      String name, long cooldown, int healAmount, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.healAmount = healAmount;
  }

  /**
   * Executes the self-heal skill on the caster.
   *
   * <p>Restores {@link #healAmount()} health points to the caster if they have a {@link
   * HealthComponent}. TODO: add visual feedback (e.g., blink) when self-healing.
   *
   * @param caster The entity using the skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    caster.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount()));
  }

  /**
   * Returns the heal amount of this skill.
   *
   * @return The number of health points restored.
   */
  public int healAmount() {
    return healAmount;
  }

  /**
   * Updates the heal amount of this skill.
   *
   * @param healAmount The new heal amount to set.
   */
  public void healAmount(int healAmount) {
    this.healAmount = healAmount;
  }
}
