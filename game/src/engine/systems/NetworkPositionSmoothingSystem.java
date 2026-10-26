package engine.systems;

import com.badlogic.gdx.Gdx;
import engine.Entity;
import engine.System;
import engine.components.NetworkPositionComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.systems.PositionSync;

/**
 * Smooths client-side entity positions toward authoritative network snapshot targets.
 *
 * <p>This system is visual/client-side only. The server remains authoritative and large corrections
 * are snapped immediately to avoid sliding through teleports, respawns, and level transitions.
 */
public final class NetworkPositionSmoothingSystem extends System {

  /** Share of the remaining distance covered per reference frame of 1/60 s. */
  private static final float POSITION_LERP = 0.55f;

  private static final float REFERENCE_FRAME_SECONDS = 1f / 60f;
  private static final float MAX_FRAME_SECONDS = 0.25f;
  private static final double SETTLE_DISTANCE_SQUARED = 0.0001;
  private static final double SNAP_DISTANCE_SQUARED = 4.0;

  /** Creates a new client-side network position smoothing system. */
  public NetworkPositionSmoothingSystem() {
    super(AuthoritativeSide.CLIENT, PositionComponent.class, NetworkPositionComponent.class);
  }

  @Override
  public void execute() {
    float lerp = frameLerp();
    filteredEntityStream(PositionComponent.class, NetworkPositionComponent.class)
        .forEach(entity -> smoothPosition(entity, lerp));
  }

  /**
   * Scales the per-frame lerp by the real frame time, so entities catch up equally fast at any
   * frame rate.
   *
   * @return the share of the remaining distance to cover in this frame
   */
  private static float frameLerp() {
    float delta = Gdx.graphics == null ? 0f : Gdx.graphics.getDeltaTime();
    if (delta <= 0f) return POSITION_LERP;
    float frames = Math.min(delta, MAX_FRAME_SECONDS) / REFERENCE_FRAME_SECONDS;
    return (float) (1 - Math.pow(1 - POSITION_LERP, frames));
  }

  private void smoothPosition(Entity entity, float lerp) {
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
            current.x() + (target.x() - current.x()) * lerp,
            current.y() + (target.y() - current.y()) * lerp);
    position.position(smoothed);
    PositionSync.syncPosition(entity);
  }
}
