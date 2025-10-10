package contrib.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;

/**
 * This system syncs the position of entities with other components that rely on mid-frame position
 * updates.
 *
 * <p>Currently updating components:
 *
 * <ul>
 *   <li>{@link CollideComponent} - Syncs the collider's position with the entity's position.
 * </ul>
 */
public class PositionSyncSystem extends System {

  /** Creates a new PositionSyncSystem. */
  public PositionSyncSystem() {
    super(PositionComponent.class);
  }

  /**
   * Act on all entities with a PositionComponent to sync their positions with relevant components.
   */
  @Override
  public void execute() {
    filteredEntityStream(PositionComponent.class).forEach(PositionSyncSystem::syncPosition);
  }

  /**
   * Sync the position of the given entity with its relevant components.
   *
   * @param e The entity to sync the position for.
   */
  public static void syncPosition(Entity e) {
    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    // CollideComponent
    e.fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.collider().position(pc.position());
            });
  }

  /**
   * Static helper method to execute the position sync system, ensuring all position changes are
   * synchronized.
   */
  public static void doSync() {
    // Sync position changes
    PositionSyncSystem pss = (PositionSyncSystem) Game.systems().get(PositionSyncSystem.class);
    if (pss != null) {
      pss.execute();
    }
  }
}
