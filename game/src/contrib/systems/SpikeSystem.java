package contrib.systems;

import contrib.components.SpikyComponent;
import core.System;
import core.components.PositionComponent;

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
    entityStream().forEach(e -> e.fetch(SpikyComponent.class).orElseThrow().reduceCoolDown());
  }
}
