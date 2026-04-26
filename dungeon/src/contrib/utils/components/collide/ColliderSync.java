package contrib.utils.components.collide;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.PositionComponent;

/** Synchronizes a collider with the entity position and scale. */
public final class ColliderSync {

  private ColliderSync() {}

  /**
   * Syncs the collider of the given entity with its current position and scale.
   *
   * @param entity the entity to sync
   */
  public static void sync(Entity entity) {
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
