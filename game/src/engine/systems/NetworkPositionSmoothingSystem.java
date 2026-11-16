package engine.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PositionComponent;
import engine.utils.FrameSmoothing;
import engine.utils.Point;

/**
 * Smooths how a multiplayer client draws entities whose positions come from network snapshots.
 *
 * <p>A snapshot sets the {@link PositionComponent#position() position} at once: the position is the
 * state of the simulation, and the server is authoritative for it. This system only moves the
 * {@link PositionComponent#renderPosition() render position} toward it. Large corrections are
 * snapped immediately to avoid sliding through teleports, respawns, and level transitions.
 */
public final class NetworkPositionSmoothingSystem extends System {

  /** Share of the remaining distance covered per world tick. */
  private static final float POSITION_LERP = 0.55f;

  private static final double SETTLE_DISTANCE_SQUARED = 0.0001;
  private static final double SNAP_DISTANCE_SQUARED = 4.0;

  /** Creates a new client-side network position smoothing system. */
  public NetworkPositionSmoothingSystem() {
    super(AuthoritativeSide.CLIENT, PositionComponent.class);
  }

  @Override
  public void execute() {
    // Without a server the game loop places the render positions between world ticks itself.
    if (!Game.isMultiplayerClient()) return;
    float lerp = FrameSmoothing.share(POSITION_LERP);
    filteredEntityStream().forEach(entity -> smoothPosition(entity, lerp));
  }

  private void smoothPosition(Entity entity, float lerp) {
    PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();

    Point current = position.renderPosition();
    Point target = position.position();
    if (current.equals(target)) return;
    double distanceSquared = current.distanceSquared(target);
    if (distanceSquared <= SETTLE_DISTANCE_SQUARED || distanceSquared >= SNAP_DISTANCE_SQUARED) {
      position.renderPosition(target);
      return;
    }

    position.renderPosition(
        new Point(
            current.x() + (target.x() - current.x()) * lerp,
            current.y() + (target.y() - current.y()) * lerp));
  }
}
