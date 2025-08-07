package contrib.utils.components.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

/** Utility class for AI-related operations like calculating paths. */
public class AIUtils {

  /**
   * Sets the velocity of the passed entity so that it takes the next necessary step to get to the
   * end of the path.
   *
   * @param entity Entity moving on the path.
   * @param path Path on which the entity moves.
   */
  public static void followPath(final Entity entity, final GraphPath<Tile> path) {
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
    Tile nextTile = findNextTile(path, currentTile);

    // currentTile not in path
    if (nextTile == null) {
      return;
    }

    Vector2 direction = Vector2.ZERO;
    for (Direction dir : currentTile.directionTo(nextTile)) {
      direction = direction.add(dir);
    }
    vc.applyForce("MOVEMENT", direction.normalize().scale(vc.maxSpeed()));
  }

  /**
   * Checks if the entity is either on the end of the path or has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path which the entity possibly left or has reached the end of.
   * @return true if the entity is on the end of the path or has left the path, otherwise false.
   */
  public static boolean pathFinishedOrLeft(final Entity entity, final GraphPath<Tile> path) {
    return pathFinished(entity, path) || pathLeft(entity, path);
  }

  /**
   * Checks if the entity is on the end of the path.
   *
   * @param entity Entity to be checked.
   * @param path Path on which the entity possible reached the end.
   * @return true if the entity is on the end of the path, otherwise false.
   */
  public static boolean pathFinished(final Entity entity, final GraphPath<Tile> path) {
    if (path.getCount() == 0) return true;
    return entity
        .fetch(PositionComponent.class)
        .map(pc -> LevelUtils.lastTile(path).equals(Game.tileAT(pc.position())))
        .orElse(false);
  }

  /**
   * Checks if the entity has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path to be checked.
   * @return true if the entity has left the path, otherwise false.
   */
  public static boolean pathLeft(final Entity entity, final GraphPath<Tile> path) {
    return entity
        .fetch(PositionComponent.class)
        .map(pc -> !onPath(path, Game.tileAT(pc.position())))
        .orElse(true);
  }

  private static Tile findNextTile(GraphPath<Tile> path, Tile currentTile) {
    for (int i = 0; i < path.getCount() - 1; i++) {
      if (path.get(i).equals(currentTile)) {
        return path.get(i + 1);
      }
    }
    return null;
  }

  private static boolean onPath(GraphPath<Tile> path, Tile currentTile) {
    for (Tile tile : path) {
      if (tile.equals(currentTile)) {
        return true;
      }
    }
    return false;
  }
}
