package contrib.utils.components.ai.transition;

import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.Function;

/**
 * Implementation of a transition between idle and fight mode. Activates fight mode when the hero is
 * within a specified range of the entity.
 */
public final class RangeTransition implements Function<Entity, Boolean> {

  private final float range;
  private boolean stayInFightMode = false;
  private boolean hasBeenInFightMode = false;

  /**
   * Switches to combat mode when the player is within range of the entity.
   *
   * @param range Range of the entity.
   */
  public RangeTransition(float range) {
    this.range = range;
  }

  /**
   * Switches to combat mode when the player is within range of the entity.
   *
   * @param range Range of the entity.
   * @param stayInFightMode Whether the entity should stay in fight mode after the player has left
   *     the range.
   */
  public RangeTransition(float range, boolean stayInFightMode) {
    this.range = range;
    this.stayInFightMode = stayInFightMode;
  }

  @Override
  public Boolean apply(final Entity entity) {
    if (LevelUtils.playerInRange(entity, range)) {
      hasBeenInFightMode = true;
      return true;
    } else {
      return stayInFightMode && hasBeenInFightMode; // Stay in fight mode if the entity has been in
    }
  }
}
