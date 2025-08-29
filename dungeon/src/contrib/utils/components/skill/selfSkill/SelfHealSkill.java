package contrib.utils.components.skill.selfSkill;

import contrib.components.HealthComponent;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.utils.Tuple;

/**
 * A skill that allows the caster to restore its own health.
 *
 * <p>When executed, this skill heals the caster by a configurable amount of health points. The
 * skill can only be used if the caster has a {@link HealthComponent}. After execution, a visual
 * blink effect is triggered to indicate healing.
 *
 * <p>Resource costs (such as mana or energy) can be configured and will be consumed when the skill
 * is used. The skill is subject to a cooldown period before it can be cast again.
 */
public class SelfHealSkill extends Skill {

  /** Name of this skill. */
  public static final String NAME = "SELF HEAL";

  /** The amount of health restored when the skill is executed. */
  protected int healAmount;

  /**
   * Creates a new self-healing skill.
   *
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param healAmount The amount of health restored when executed.
   * @param resourceCost Optional resource costs required to use this skill.
   */
  @SafeVarargs
  public SelfHealSkill(long cooldown, int healAmount, Tuple<Resource, Integer>... resourceCost) {
    super(NAME, cooldown, resourceCost);
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
    caster
        .fetch(HealthComponent.class)
        .ifPresent(
            hc -> {
              hc.restoreHealthpoints(healAmount());
              SkillTools.blink(caster, 0x00FF00FF, 120, 4);
            });
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
