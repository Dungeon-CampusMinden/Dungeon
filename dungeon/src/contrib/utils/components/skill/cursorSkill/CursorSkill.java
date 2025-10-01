package contrib.utils.components.skill.cursorSkill;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.utils.Point;
import core.utils.Tuple;
import java.util.function.Supplier;

/**
 * A skill that is executed at the position of the cursor.
 *
 * <p>Unlike self-targeted skills, a {@code CursorSkill} allows the caster to trigger an effect at
 * the current cursor location in the game world. The actual behavior must be implemented by
 * subclasses through {@link #executeOnCursor(Entity, Point)}.
 *
 * <p>If this skill is used, in a context where the cursor position is not available (e.g., in a
 * server environment), the supplier for the cursor position can be overridden using {@link
 * #cursorPositionSupplier(Supplier)} to provide an alternative means of determining the target
 * position.
 *
 * <p>Resource costs (such as mana or energy) can be specified and will be consumed when the skill
 * is used. The skill also respects a cooldown before it can be cast again.
 */
public abstract class CursorSkill extends Skill {

  /**
   * A supplier that provides the current cursor position as a {@link Point}.
   *
   * <p>By default, this uses {@link SkillTools#cursorPositionAsPoint()}, but can be overridden
   * using {@link #cursorPositionSupplier(Supplier)} for flexibility in different contexts (e.g.,
   * testing or server-side logic).
   */
  private Supplier<Point> cursorPositionSupplier = SkillTools::cursorPositionAsPoint;

  /**
   * Creates a new cursor-targeted skill with a custom execution behavior.
   *
   * @param name The name of the skill.
   * @param cooldown The cooldown in milliseconds before the skill can be used again.
   * @param resourceCost Optional resource costs (e.g., mana, energy) required to use this skill.
   */
  @SafeVarargs
  public CursorSkill(String name, long cooldown, Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
  }

  /**
   * Executes the cursor skill on the caster.
   *
   * <p>Internally, this method determines the current cursor position via {@link
   * SkillTools#cursorPositionAsPoint()} or a custom supplier if overridden, and delegates the
   * effect to {@link #executeOnCursor(Entity, Point)}.
   *
   * @param caster The entity that uses the skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    executeOnCursor(caster, cursorPositionSupplier.get());
  }

  /**
   * Sets a custom supplier for the cursor position.
   *
   * <p>This allows for flexibility in determining the cursor position, which can be useful for
   * testing or alternative input methods.
   *
   * @param cursorPositionSupplier A supplier that provides the current cursor position as a {@link
   *     Point}.
   */
  public void cursorPositionSupplier(Supplier<Point> cursorPositionSupplier) {
    this.cursorPositionSupplier = cursorPositionSupplier;
  }

  /**
   * Defines the behavior of this cursor-targeted skill.
   *
   * <p>Implementations must specify what happens when the skill is triggered, based on the caster
   * and the cursor's world position.
   *
   * @param caster The entity using the skill.
   * @param point The current cursor position in the game world.
   */
  protected abstract void executeOnCursor(Entity caster, Point point);
}
