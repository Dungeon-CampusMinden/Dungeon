package contrib.utils.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.components.MissingComponentException;

/** Utility class for AI-related operations like calculating paths. */
public final class AIUtils {

  /**
   * Sets the velocity of the passed entity so that it takes the next necessary step to get to the
   * end of the path.
   *
   * @param entity Entity moving on the path.
   * @param path Path on which the entity moves.
   */
  public static void move(final Entity entity, final GraphPath<Tile> path) {
    // entity is already at the end
    if (pathFinishedOrLeft(entity, path)) {
      return;
    }
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));
    Tile currentTile = Game.tileAT(pc.position());
    int i = 0;
    Tile nextTile = null;
    while (nextTile == null && i < path.getCount()) {
      if (path.get(i).equals(currentTile)) {
        nextTile = path.get(i + 1);
      }
      i++;
    }
    // currentTile not in path
    if (nextTile == null) {
      return;
    }

    switch (currentTile.directionTo(nextTile)[0]) {
      case N -> vc.currentYVelocity(vc.yVelocity());
      case S -> vc.currentYVelocity(-vc.yVelocity());
      case E -> vc.currentXVelocity(vc.xVelocity());
      case W -> vc.currentXVelocity(-vc.xVelocity());
    }
    if (currentTile.directionTo(nextTile).length > 1)
      switch (currentTile.directionTo(nextTile)[1]) {
        case N -> vc.currentYVelocity(vc.yVelocity());
        case S -> vc.currentYVelocity(-vc.yVelocity());
        case E -> vc.currentXVelocity(vc.xVelocity());
        case W -> vc.currentXVelocity(-vc.xVelocity());
      }
  }

  /**
   * Checks if the entity is either on the end of the path or has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path which the entity possibly left or has reached the end of.
   * @return true if the entity is on the end of the path or has left the path, otherwise false.
   */
  public static boolean pathFinishedOrLeft(final Entity entity, final GraphPath<Tile> path) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    boolean finished =
        path.getCount() == 0 || LevelUtils.lastTile(path).equals(Game.tileAT(pc.position()));

    boolean onPath = false;
    Tile currentTile = Game.tileAT(pc.position());
    for (Tile tile : path) {
      if (currentTile == tile) {
        onPath = true;
        break;
      }
    }

    return !onPath || finished;
  }

  /**
   * Checks if the entity is on the end of the path.
   *
   * @param entity Entity to be checked.
   * @param path Path on which the entity possible reached the end.
   * @return true if the entity is on the end of the path, otherwise false.
   */
  public static boolean pathFinished(final Entity entity, final GraphPath<Tile> path) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return path.getCount() == 0 || LevelUtils.lastTile(path).equals(Game.tileAT(pc.position()));
  }

  /**
   * Checks if the entity has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path to be checked.
   * @return true if the entity has left the path, otherwise false.
   */
  public static boolean pathLeft(final Entity entity, final GraphPath<Tile> path) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    boolean onPath = false;
    Tile currentTile = Game.tileAT(pc.position());
    for (Tile tile : path) {
      if (currentTile == tile) {
        onPath = true;
        break;
      }
    }
    return !onPath;
  }
}
