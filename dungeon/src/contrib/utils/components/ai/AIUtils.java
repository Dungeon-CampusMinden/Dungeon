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
import java.util.stream.StreamSupport;

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

    Tile currentTile = Game.tileAtEntity(entity).orElse(null);
    Tile nextTile = findNextTile(path, currentTile);

    // currentTile not in path
    if (nextTile == null) {
      return;
    }

    Vector2 direction = calculateDirection(currentTile, nextTile);

    entity
        .fetch(VelocityComponent.class)
        .ifPresent(vc -> vc.applyForce("MOVEMENT", direction.normalize().scale(vc.maxSpeed())));
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
   * @return true if the entity is on the last tile of the path; false if the entity is not at the
   *     end or if no {@link PositionComponent} is present.
   */
  public static boolean pathFinished(final Entity entity, final GraphPath<Tile> path) {
    return path.getCount() == 0
        || entity
            .fetch(PositionComponent.class)
            .map(pc -> LevelUtils.lastTile(path).equals(Game.tileAt(pc.position()).orElse(null)))
            .orElse(false);
  }

  /**
   * Checks if the entity has left the path.
   *
   * @param entity Entity to be checked.
   * @param path Path to be checked.
   * @return true if the entity's current tile is not part of the given path, or if no {@link
   *     PositionComponent} is present; otherwise false.
   */
  public static boolean pathLeft(final Entity entity, final GraphPath<Tile> path) {
    return entity
        .fetch(PositionComponent.class)
        .map(pc -> !onPath(path, Game.tileAt(pc.position()).orElse(null)))
        .orElse(true);
  }

  /**
   * Finds the next tile in the path after the current tile.
   *
   * @param path The path on which the entity is moving.
   * @param currentTile The tile the entity is currently standing on.
   * @return The next tile in the path after the current tile, or {@code null} if the current tile
   *     is not found or is at the end of the path.
   */
  private static Tile findNextTile(GraphPath<Tile> path, Tile currentTile) {
    return StreamSupport.stream(path.spliterator(), false)
        .dropWhile(t -> !t.equals(currentTile))
        .skip(1)
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks if the current tile is on the given path.
   *
   * @param path The path to be checked.
   * @param currentTile The tile to look for on the path.
   * @return true if the current tile is on the path, otherwise false.
   */
  private static boolean onPath(GraphPath<Tile> path, Tile currentTile) {
    return StreamSupport.stream(path.spliterator(), false).anyMatch(t -> t.equals(currentTile));
  }

  /**
   * Calculates the direction vector from the current tile to the next tile in the path.
   *
   * @param currentTile The tile the entity is currently on.
   * @param nextTile The next tile in the path.
   * @return A direction vector pointing from the current tile to the next tile.
   */
  private static Vector2 calculateDirection(Tile currentTile, Tile nextTile) {
    Vector2 direction = Vector2.ZERO;
    for (Direction dir : currentTile.directionTo(nextTile)) {
      direction = direction.add(dir);
    }
    return direction;
  }
}
