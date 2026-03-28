package contrib.utils.components.ai.idle;

import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.components.PositionComponent;
import core.level.path.TilePath;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Time;
import core.utils.components.MissingComponentException;
import java.util.function.Consumer;

/** Implements an idle AI that lets the entity walk in a specific radius from a fixed point. */
public final class StaticRadiusWalk implements Consumer<Entity> {
  private final float radius;
  private final long breakTimeMs;
  private TilePath path;
  private long waitStartedAtMs = Long.MIN_VALUE;
  private Point center;

  /**
   * Finds a point in the radius of the fixed center point and then moves there. When the point has
   * been reached, a new point in the radius is searched for from the center.
   *
   * @param radius Radius in which a target point is to be searched for.
   * @param breakTimeInSeconds How long to wait (in seconds) before searching a new goal.
   */
  public StaticRadiusWalk(float radius, int breakTimeInSeconds) {
    this.radius = radius;
    this.breakTimeMs = Math.max(0L, breakTimeInSeconds) * 1000L;
  }

  @Override
  public void accept(final Entity entity) {
    if (path == null || AIUtils.pathFinishedOrLeft(entity, path)) {
      handleWaitingForNextPath(entity);
      return;
    }

    AIUtils.followPath(entity, path);
  }

  private void handleWaitingForNextPath(final Entity entity) {
    if (center == null) {
      PositionComponent pc =
        entity
          .fetch(PositionComponent.class)
          .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

      if (pc.position().equals(PositionComponent.ILLEGAL_POSITION)) {
        return;
      }
      center = pc.position();
    }

    if (waitStartedAtMs == Long.MIN_VALUE) {
      waitStartedAtMs = Time.nowMs();
    }

    if (Time.sinceMs(waitStartedAtMs) < breakTimeMs) {
      return;
    }

    PositionComponent pc =
      entity
        .fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    if (pc.position().equals(PositionComponent.ILLEGAL_POSITION)) {
      return;
    }

    waitStartedAtMs = Long.MIN_VALUE;

    Point currentPosition = pc.position();
    Point newEndTile = LevelUtils.randomAccessibleTileInRangeAsPoint(center, radius).orElse(center);
    path = LevelUtils.calculateTilePath(currentPosition, newEndTile);

    if (!AIUtils.pathFinishedOrLeft(entity, path)) {
      AIUtils.followPath(entity, path);
    }
  }
}
