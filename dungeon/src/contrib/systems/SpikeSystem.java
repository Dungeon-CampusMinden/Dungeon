package contrib.systems;

import contrib.components.SpikyComponent;
import core.Entity;
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
    // (1) traditional: double "fetch" of SpikyComponent.class
    filteredEntityStream(SpikyComponent.class)
        .map(e -> e.fetch(SpikyComponent.class))
        .flatMap(Optional::stream)
        .forEach(SpikyComponent::reduceCoolDown);

    // (2) use streams containing data objects as filter result
    filteredEntityStreamX(SpikyComponent.class)
        .map(SingleComponent::first)
        .forEach(SpikyComponent::reduceCoolDown);

    // (2a) using pattern matching (plus data objects as filter result)
    filteredEntityStreamY(SpikyComponent.class)
        .forEach(this::reduceCoolDown);

    // (3) use streams containing the components we are looking for
    filteredEntityStreamZ(SpikyComponent.class)
        .forEach(SpikyComponent::reduceCoolDown);
  }

  void reduceCoolDown(ComponentTuple t) {
    switch (t) {
      case SingleComponent(Entity e, SpikyComponent c) -> c.reduceCoolDown();
      default ->
          throw new IllegalStateException(
              "Unexpected value: " + t); // should not happen, but required (Java)
    }
  }
}
