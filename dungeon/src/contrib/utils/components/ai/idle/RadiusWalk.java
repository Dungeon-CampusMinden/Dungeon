package contrib.utils.components.ai.idle;

import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.components.PositionComponent;
import core.level.path.TilePath;
import core.level.utils.LevelUtils;
import core.utils.Time;
import java.util.function.Consumer;

/** Implements an idle AI that lets the entity walk in a specific radius from its current position. */
public final class RadiusWalk implements Consumer<Entity> {
  private final float radius;
  private final long breakTimeMs;
  private TilePath path;
  private long waitStartedAtMs = Long.MIN_VALUE;

  /**
   * Finds a point in the radius and then moves there. When the point has been reached, a new point
   * in the radius is searched for from there.
   *
   * @param radius Radius in which a target point is to be searched for.
   * @param breakTimeInSeconds How long to wait (in seconds) before searching a new goal.
   */
  public RadiusWalk(float radius, int breakTimeInSeconds) {
    this.radius = radius;
    this.breakTimeMs = Math.max(0L, breakTimeInSeconds) * 1000L;
  }

  @Override
  public void accept(final Entity entity) {
    if (path == null) {
      path = calculatePath(entity);

      if (!AIUtils.pathFinishedOrLeft(entity, path)) {
        AIUtils.followPath(entity, path);
      }
      return;
    }

    if (AIUtils.pathFinishedOrLeft(entity, path)) {
      handleWaitingForNextPath(entity);
      return;
    }

    AIUtils.followPath(entity, path);
  }

  private void handleWaitingForNextPath(final Entity entity) {
    if (waitStartedAtMs == Long.MIN_VALUE) {
      waitStartedAtMs = Time.nowMs();
    }

    if (Time.sinceMs(waitStartedAtMs) < breakTimeMs) {
      return;
    }

    waitStartedAtMs = Long.MIN_VALUE;
    path = calculatePath(entity);

    if (!AIUtils.pathFinishedOrLeft(entity, path)) {
      AIUtils.followPath(entity, path);
    }
  }

  /**
   * Calculates a new radius path if the entity can be located.
   *
   * @param entity entity that should walk
   * @return new path, or {@code null} if the entity has no position
   */
  private TilePath calculatePath(final Entity entity) {
    if (entity.fetch(PositionComponent.class).isEmpty()) {
      return null;
    }

    return LevelUtils.calculateTilePathToRandomTileInRange(entity, radius);
  }
}
