package contrib.utils.components.position;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.PositionComponent;

/**
 * Utility methods for syncing position-dependent component state after a position update.
 *
 * <p>Currently, this updates:
 *
 * <ul>
 *   <li>{@link CollideComponent} by copying the entity position and scale to its collider
 * </ul>
 */
public final class PositionSyncUtils {

  private PositionSyncUtils() {}

  /**
   * Syncs the position of the given entity with its dependent components.
   *
   * @param entity the entity to sync
   */
  public static void syncPosition(Entity entity) {
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc ->
                entity
                    .fetch(CollideComponent.class)
                    .ifPresent(
                        cc -> {
                          cc.collider().position(pc.position());
                          cc.collider().scale(pc.scale());
                        }));
  }
}
