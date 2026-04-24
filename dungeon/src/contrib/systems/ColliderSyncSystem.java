package contrib.systems;

import contrib.components.CollideComponent;
import contrib.utils.components.collide.ColliderSync;
import core.System;
import core.components.PositionComponent;

/**
 * Keeps colliders synchronized with the current entity position and scale.
 *
 * <p>This is required on hosts that do not run the collision system locally, because static
 * colliders would otherwise stay at the origin.
 */
public final class ColliderSyncSystem extends System {

  /** Creates a new collider sync system. */
  public ColliderSyncSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class, CollideComponent.class);
    onEntityAdd = ColliderSync::sync;
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(ColliderSync::sync);
  }
}
