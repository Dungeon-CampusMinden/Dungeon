package feature.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import engine.Entity;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.utils.LevelUtils;
import engine.utils.Point;
import engine.utils.components.MissingComponentException;
import feature.ai.AIUtils;
import java.util.function.Consumer;

/**
 * A simple patrol behavior for a sentry entity.
 *
 * <p>The entity patrols back and forth between two fixed {@link Point}s.
 *
 * <p>This class only handles movement; it does not include any combat logic.
 */
public final class SentryPatrolWalk implements Consumer<Entity> {
  private final Point pointA;
  private final Point pointB;
  private final boolean canEnterWalls;

  private boolean toB = true; // true = moving towards B, false = towards A
  private GraphPath<Tile> currentPath;

  /**
   * Creates a new {@code SentryPatrolWalk}.
   *
   * @param pointA the first patrol point.
   * @param pointB the second patrol point.
   * @param canEnterWalls whether the sentry can move inside walls.
   */
  public SentryPatrolWalk(Point pointA, Point pointB, boolean canEnterWalls) {
    this.pointA = pointA;
    this.pointB = pointB;
    this.canEnterWalls = canEnterWalls;
  }

  @Override
  public void accept(Entity entity) {
    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    // path not finished
    if (currentPath != null && !AIUtils.pathFinished(entity, currentPath)) {
      if (AIUtils.pathLeft(entity, currentPath)) {
        pathCalculator(position.position(), getTargetPoint());
      }
      AIUtils.followPath(entity, currentPath);
      return;
    }

    // path finished --> change direction
    if (currentPath != null && AIUtils.pathFinished(entity, currentPath)) {
      toB = !toB;
      currentPath = null;
    }

    // new path
    if (currentPath == null) {
      pathCalculator(position.position(), getTargetPoint());
    }
  }

  private Point getTargetPoint() {
    return toB ? pointB : pointA;
  }

  private void pathCalculator(Point from, Point to) {
    if (canEnterWalls) {
      currentPath = LevelUtils.calculatePathInsideWall(from, to);
    } else {
      currentPath = LevelUtils.calculatePath(from, to);
    }
  }
}
