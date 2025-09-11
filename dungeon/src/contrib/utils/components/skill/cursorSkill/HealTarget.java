package contrib.utils.components.skill.cursorSkill;

import contrib.components.HealthComponent;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.Tuple;
import java.util.function.BiConsumer;

/**
 * A cursor-targeted skill that heals entities at the cursor position.
 *
 * <p>When executed, this skill restores a configurable amount of health points to the first entity
 * found at the cursor location that has a {@link HealthComponent}.
 *
 * <p>Resource costs (such as mana or energy) can be specified and will be consumed when the skill
 * is used. The skill also respects a cooldown before it can be cast again.
 */
public class HealTarget extends CursorSkill {

  /** The name of this skill. */
  public static final String NAME = "HEAL_ON_CURSOR";

  /** The amount of health restored by this skill. */
  private int healAmount;

  private BiConsumer<Entity, Point> heal =
      (entity, point) -> Game.entityAtPoint(point).findFirst().ifPresent(e -> heal(e));

  /**
   * Creates a new {@code HealTarget} skill.
   *
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param healAmount The amount of health restored when executed.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  public HealTarget(long cooldown, int healAmount, Tuple<Resource, Integer>... resourceCost) {
    super(NAME, cooldown, resourceCost);
    this.healAmount = healAmount;
  }

  /**
   * Restores health points to the given entity if it has a {@link HealthComponent}.
   *
   * @param entity The entity to heal.
   */
  private void heal(Entity entity) {
    entity.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount));
  }

  /**
   * Sets the amount of health restored by this skill.
   *
   * @param healAmount The new heal amount.
   */
  public void healAmount(int healAmount) {
    this.healAmount = healAmount;
  }

  /**
   * Returns the amount of health restored by this skill.
   *
   * @return The heal amount.
   */
  public int healAmount() {
    return healAmount;
  }

  /**
   * Executes the healing effect at the specified cursor position.
   *
   * <p>The first entity found at the cursor location that has a {@link HealthComponent} is healed.
   *
   * @param caster The entity using the skill.
   * @param point The cursor position in the game world where the skill is applied.
   */
  @Override
  protected void executeOnCursor(Entity caster, Point point) {
    Game.entityAtPoint(point).findFirst().ifPresent(this::heal);
  }
}
