package contrib.utils.components.ai;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.path.TilePath;
import core.utils.Direction;
import core.utils.Vector2;

/**
 * Utility class providing helper methods for AI-controlled entities.
 *
 * <p>Contains methods for path following and path state checks used by AI systems to navigate
 * entities through the game level.
 */
public class AIUtils {

  /**
   * Moves the given entity one step along the provided path by applying a movement force to its
   * {@link VelocityComponent}.
   *
   * <p>The method determines the entity's current tile, finds the next tile on the path, and
   * applies a normalized, speed-scaled force in that direction. If the path is finished, the entity
   * is no longer on the path, or no valid next tile can be determined, no force is applied.
   *
   * @param entity the entity that should follow the path; must have a {@link PositionComponent} and
   *     a {@link VelocityComponent}
   * @param path the tile path the entity should follow
   */
  public static void followPath(final Entity entity, final TilePath path) {
    if (pathFinishedOrLeft(entity, path)) return;

    Tile currentTile = Game.tileAtEntity(entity).orElse(null);
    Tile nextTile = findNextTile(path, currentTile);

    if (nextTile == null) return;

    Vector2 direction = calculateDirection(currentTile, nextTile);

    entity
      .fetch(VelocityComponent.class)
      .ifPresent(vc -> vc.applyForce("MOVEMENT", direction.normalize().scale(vc.maxSpeed())));
  }

  /**
   * Returns {@code true} if the entity has either finished the path or has left the path.
   *
   * <p>This is a convenience combination of {@link #pathFinished(Entity, TilePath)} and
   * {@link #pathLeft(Entity, TilePath)}.
   *
   * @param entity the entity to check
   * @param path the tile path to check against
   * @return {@code true} if the path is {@code null}, empty, the entity reached the last tile, or
   *     the entity's current tile is not part of the path; {@code false} otherwise
   */
  public static boolean pathFinishedOrLeft(final Entity entity, final TilePath path) {
    return pathFinished(entity, path) || pathLeft(entity, path);
  }

  /**
   * Returns {@code true} if the entity has reached the last tile of the path.
   *
   * <p>The path is considered finished when the entity's current tile equals the last tile of the
   * given path. If the path is {@code null} or empty, {@code true} is returned immediately.
   *
   * @param entity the entity to check; must have a {@link PositionComponent}
   * @param path the tile path to check against
   * @return {@code true} if the path is {@code null}, empty, or the entity's current tile equals
   *     the last tile of the path; {@code false} otherwise
   */
  public static boolean pathFinished(final Entity entity, final TilePath path) {
    if (path == null || path.isEmpty()) return true;

    return entity
      .fetch(PositionComponent.class)
      .map(pc -> {
        Tile current = Game.tileAt(pc.position()).orElse(null);
        Tile last = path.last();
        return last != null && last.equals(current);
      })
      .orElse(false);
  }

  /**
   * Returns {@code true} if the entity's current tile is no longer part of the path.
   *
   * <p>This can happen when an entity is displaced or moved off the path. If the path is
   * {@code null} or empty, {@code true} is returned immediately.
   *
   * @param entity the entity to check; must have a {@link PositionComponent}
   * @param path the tile path to check against
   * @return {@code true} if the path is {@code null}, empty, or the entity's current tile is not
   *     on the path; {@code false} otherwise
   */
  public static boolean pathLeft(final Entity entity, final TilePath path) {
    if (path == null || path.isEmpty()) return true;

    return entity
      .fetch(PositionComponent.class)
      .map(pc -> {
        Tile current = Game.tileAt(pc.position()).orElse(null);
        return !onPath(path, current);
      })
      .orElse(true);
  }

  /**
   * Finds the next tile on the path after the given current tile.
   *
   * <p>Iterates through the path and returns the tile immediately following the current tile.
   * Returns {@code null} if the path is {@code null}, the current tile is {@code null}, the path
   * has fewer than two tiles, or the current tile is not found on the path.
   *
   * @param path the tile path to search in
   * @param currentTile the tile to find the successor of
   * @return the next tile on the path, or {@code null} if no successor can be found
   */
  private static Tile findNextTile(final TilePath path, final Tile currentTile) {
    if (path == null || currentTile == null || path.size() < 2) return null;

    for (int i = 0; i < path.size() - 1; i++) {
      if (currentTile.equals(path.get(i))) {
        return path.get(i + 1);
      }
    }
    return null;
  }

  /**
   * Returns {@code true} if the given tile is part of the path.
   *
   * @param path the tile path to check
   * @param currentTile the tile to look for on the path
   * @return {@code true} if the tile is found on the path; {@code false} if the path or tile is
   *     {@code null}, or if the tile is not present on the path
   */
  private static boolean onPath(final TilePath path, final Tile currentTile) {
    if (path == null || currentTile == null) return false;
    for (Tile t : path) {
      if (currentTile.equals(t)) return true;
    }
    return false;
  }

  /**
   * Calculates the movement direction vector from the current tile to the next tile.
   *
   * <p>Sums all directional components returned by {@link Tile#directionTo(Tile)} to produce
   * a combined direction vector. The resulting vector may be diagonal if the tiles are positioned
   * diagonally relative to each other.
   *
   * @param currentTile the tile the entity is currently on
   * @param nextTile the tile the entity should move towards
   * @return a {@link Vector2} representing the combined movement direction
   */
  private static Vector2 calculateDirection(final Tile currentTile, final Tile nextTile) {
    Vector2 direction = Vector2.ZERO;
    for (Direction dir : currentTile.directionTo(nextTile)) {
      direction = direction.add(dir);
    }
    return direction;
  }
}
