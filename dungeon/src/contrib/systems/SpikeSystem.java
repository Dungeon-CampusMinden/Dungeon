package contrib.systems;

import contrib.components.SpikyComponent;
import core.System;
import core.components.PositionComponent;
import java.util.Optional;

/**
 * Reduces the current cool down for each {@link SpikyComponent} once per frame. Entities with the
 * {@link SpikyComponent} and {@link PositionComponent} will be processed by this system.
 *
 * @see SpikyComponent
 */
public final class SpikeSystem extends System {

  /** Create new SpikeSystem. */
  public SpikeSystem() {
    super(SpikyComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(SpikyComponent.class)
        .map(e -> e.fetch(SpikyComponent.class))
        .flatMap(Optional::stream)
        .forEach(SpikyComponent::reduceCoolDown);
  }
}
