package contrib.systems;

import contrib.components.ManaComponent;
import core.System;

/**
 * A system that restores mana to all entities with a {@link ManaComponent}.
 *
 * <p>The restoration is based on the mana regeneration rate defined in each {@code ManaComponent}.
 * The amount restored per execution is calculated from the elapsed time since this system was
 * last executed.
 */
public class ManaRestoreSystem extends System {

  /**
   * Creates a new {@code ManaRestoreSystem}.
   *
   * <p>This system operates on entities that have a {@link ManaComponent}.
   */
  public ManaRestoreSystem() {
    super(ManaComponent.class);
  }

  /**
   * Executes the mana restoration for all entities that contain a {@link ManaComponent}.
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
      .flatMap(e -> e.fetch(ManaComponent.class).stream())
      .forEach(m -> m.restore(m.restorePerSecond() * deltaSeconds));
  }
}
