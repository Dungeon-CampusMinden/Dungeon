package contrib.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.PositionComponent;

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
public class PositionSync {

  /** Creates a new PositionSyncSystem. */
  public PositionSync() {}

  /**
   * Sync the position of the given entity with its relevant components.
   *
   * @param e The entity to sync the position for.
   */
  public static void syncPosition(Entity e) {
    e.fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              // CollideComponent
              e.fetch(CollideComponent.class)
                  .ifPresent(
                      cc -> {
                        cc.collider().position(pc.position());
                        cc.collider().scale(pc.scale());
                      });
            });
  }
}
