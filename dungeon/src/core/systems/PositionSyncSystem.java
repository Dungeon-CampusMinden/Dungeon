package core.systems;

import contrib.components.CollideComponent;
import contrib.systems.PositionSync;
import core.System;
import core.components.PositionComponent;

/**
 * Keeps colliders synchronized with the current entity position and scale.
 *
 * <p>This is required on hosts that do not run the collision system locally, because static
 * colliders would otherwise stay at the origin.
 */
public final class PositionSyncSystem extends System {

  /** Creates a new position sync system. */
  public PositionSyncSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class, CollideComponent.class);
    onEntityAdd = PositionSync::syncPosition;
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(PositionSync::syncPosition);
  }
}
