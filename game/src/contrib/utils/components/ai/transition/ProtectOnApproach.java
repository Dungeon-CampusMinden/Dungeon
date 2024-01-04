package contrib.utils.components.ai.transition;

import core.Entity;
import core.level.utils.LevelUtils;
import java.util.function.Function;

/**
 * Implements an AI that protects an entity if the hero is in the given range.
 *
 * <p>Entity will stay in fight mode once entered.
 */
public final class ProtectOnApproach implements Function<Entity, Boolean> {

  private final float range;
  private final Entity toProtect;
  private boolean isInFight = false;

  /**
   * @param range The range in which the hero can come to the protected entity. If the hero is
   *     closer than this range to the protected entity, the protecting entity switches to fight
   *     mode.
   * @param toProtect The entity which will be protected.
   */
  public ProtectOnApproach(float range, final Entity toProtect) {
    this.range = range;
    this.toProtect = toProtect;
  }

  /**
   * If protecting entity isn't in fight mode yet, check if player is in range of the protected
   * entity.
   *
   * @param entity Entity that is protecting.
   * @return true when the entity is in fight mode, else false.
   */
  @Override
  public Boolean apply(final Entity entity) {
    if (isInFight) return true;

    isInFight = LevelUtils.playerInRange(toProtect, range);

    return isInFight;
  }
}
