package core.systems;

import contrib.systems.PositionSync;
import core.Entity;
import core.System;
import core.components.NetworkPositionComponent;
import core.components.PositionComponent;
import core.utils.Point;

/**
 * Smooths client-side entity positions toward authoritative network snapshot targets.
 *
 * <p>This system is visual/client-side only. The server remains authoritative and large corrections
 * are snapped immediately to avoid sliding through teleports, respawns, and level transitions.
 */
public final class NetworkPositionSmoothingSystem extends System {

  private static final float POSITION_LERP = 0.55f;
  private static final double SETTLE_DISTANCE_SQUARED = 0.0001;
  private static final double SNAP_DISTANCE_SQUARED = 4.0;

  /** Creates a new client-side network position smoothing system. */
  public NetworkPositionSmoothingSystem() {
    super(AuthoritativeSide.CLIENT, PositionComponent.class, NetworkPositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(PositionComponent.class, NetworkPositionComponent.class)
        .forEach(this::smoothPosition);
  }

  private void smoothPosition(Entity entity) {
    PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();
    NetworkPositionComponent networkPosition =
        entity.fetch(NetworkPositionComponent.class).orElseThrow();

    Point current = position.position();
    Point target = networkPosition.targetPosition();
    double distanceSquared = current.distanceSquared(target);
    if (distanceSquared <= SETTLE_DISTANCE_SQUARED) {
      position.position(target);
      PositionSync.syncPosition(entity);
      return;
    }
    if (distanceSquared >= SNAP_DISTANCE_SQUARED) {
      position.position(target);
      PositionSync.syncPosition(entity);
      return;
    }

    Point smoothed =
        new Point(
            current.x() + (target.x() - current.x()) * POSITION_LERP,
            current.y() + (target.y() - current.y()) * POSITION_LERP);
    position.position(smoothed);
    PositionSync.syncPosition(entity);
  }
}
