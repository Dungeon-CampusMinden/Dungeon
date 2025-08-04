package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;
import core.Entity;
import core.utils.components.MissingComponentException;
import java.util.function.Function;

/**
 * Implementation of a transition between idle and fight mode. Switches to fight mode when the
 * entity was attacked by another entity.
 */
public final class SelfDefendTransition implements Function<Entity, Boolean> {

  @Override
  public Boolean apply(final Entity entity) {
    return entity
        .fetch(HealthComponent.class)
        .map(hc -> hc.currentHealthpoints() < hc.maximalHealthpoints())
        .orElseThrow(() -> MissingComponentException.build(entity, HealthComponent.class));
  }
}
