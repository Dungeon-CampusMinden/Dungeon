package contrib.systems;

import contrib.components.StaminaComponent;
import core.System;

/**
 * A system that restores stamina to all entities with a {@link StaminaComponent}.
 *
 * <p>The restoration amount is calculated from the elapsed time since this system was last
 * executed, making the regeneration independent of the configured frame rate.
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
   * Executes the stamina restoration for all entities that contain a {@link StaminaComponent}.
   *
   * <p>For each entity, the system restores:
   *
   * <pre>
   * restorePerSecond * deltaTime()
   * </pre>
   *
   * <p>This ensures host-agnostic, time-based regeneration.
   */
  @Override
  public void execute() {
    final float deltaSeconds = deltaTime();
    if (deltaSeconds <= 0f) {
      return;
    }

    filteredEntityStream()
      .flatMap(e -> e.fetch(StaminaComponent.class).stream())
      .forEach(c -> c.restore(c.restorePerSecond() * deltaSeconds));
  }
}
