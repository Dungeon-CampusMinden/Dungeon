package contrib.systems;

import contrib.components.StaminaComponent;
import core.Game;
import core.System;

/**
 * A system that restores stamina to all entities with an {@link StaminaComponent}.
 *
 * <p>The restoration amount is calculated based on the stamina regeneration rate defined in each
 * {@code StaminaComponent}. To ensure frame-rate-independent behavior, the per-second restoration
 * value is divided by the current game frame rate.
 */
public class StaminaRestoreSystem extends System {

  /**
   * Creates a new {@code EnergyRestoreSystem}.
   *
   * <p>This system processes all entities that contain an {@link StaminaComponent}.
   */
  public StaminaRestoreSystem() {
    super(StaminaComponent.class);
  }

  /**
   * Executes the stamina restoration for all entities that contain an {@link StaminaComponent}.
   *
   * <p>For each entity, the system restores an amount of stamina equal to:
   *
   * <pre>
   * restorePerSecond / Game.frameRate()
   * </pre>
   *
   * <p>ensuring smooth, frame-rate-independent regeneration.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .flatMap(e -> e.fetch(StaminaComponent.class).stream())
        .forEach(c -> c.restore(c.restorePerSecond() / Game.frameRate()));
  }
}
