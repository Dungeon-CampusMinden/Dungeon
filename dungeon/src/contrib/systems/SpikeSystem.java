package contrib.systems;

import contrib.components.SpikyComponent;
import core.System;
import java.util.Optional;

/**
 * Reduces the current cooldown for each {@link SpikyComponent}.
 *
 * <p>The cooldown reduction is based on the elapsed time since the last execution, making the
 * behavior independent of the configured frame rate.
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
    final float deltaSeconds = deltaTime();
    if (deltaSeconds <= 0f) {
      return;
    }

    filteredEntityStream(SpikyComponent.class)
      .map(e -> e.fetch(SpikyComponent.class))
      .flatMap(Optional::stream)
      .forEach(spiky -> spiky.reduceCoolDown(deltaSeconds));
  }
}
