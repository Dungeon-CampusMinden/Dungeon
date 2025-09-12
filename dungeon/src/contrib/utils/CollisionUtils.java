package contrib.utils;

import core.Game;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Arrays;
import java.util.List;

public class CollisionUtils {

  public static final float TOP_OFFSET = 0.0001f;

  public static boolean isCollidingWithLevel(
      Point pos, Vector2 offset, Vector2 size, boolean canEnterPits) {
    List<Point> corners =
        Arrays.asList(
            pos.translate(offset), // bottom-left
            pos.translate(offset.x() + size.x() - TOP_OFFSET, offset.y()), // bottom-right
            pos.translate(offset.x(), offset.y() + size.y() - TOP_OFFSET), // top-left
            pos.translate(offset.x() + size.x() - TOP_OFFSET, offset.y() + size.y()) // top-right
            );
    return corners.stream()
        .anyMatch(p -> !tileIsAccessible(Game.tileAt(p).orElse(null), canEnterPits));
  }

  /**
   * Helper method to determine if a tile can be entered by the entity.
   *
   * <p>Considers both whether the tile is accessible and whether the entity is allowed to enter pit
   * tiles.
   *
   * @param tile the tile to check for accessibility
   * @param canEnterPitTiles whether the entity can enter pit tiles
   * @return true if tile is accessible or a pit tile that can be entered, false otherwise
   */
  public static boolean tileIsAccessible(Tile tile, boolean canEnterPitTiles) {
    return tile != null
        && (tile.isAccessible()
            || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT)));
  }

  /**
   * Checks whether the path between two points is completely accessible by stepping along the
   * vector between them in small increments.
   *
   * <p>This method simulates movement from the starting point to the target by walking small steps
   * along the direction vector. At each step, it checks whether the tile is accessible or can be
   * entered (e.g., if it's a pit and the entity is allowed to enter pits).
   *
   * <p>This ensures that no wall or inaccessible tile is skipped due to large velocity steps,
   * especially important when moving diagonally or at high speeds.
   *
   * @param from the starting point
   * @param to the target point
   * @param canEnterPitTiles whether the entity is allowed to walk into pit tiles
   * @return true if the entire path from start to target is clear; false if a tile in between is
   *     blocked
   */
  boolean isPathClearByStepping(Point from, Point to, boolean canEnterPitTiles) {
    Vector2 direction = from.vectorTo(to);
    double distance = direction.length();

    if (distance == 0f) return true;

    // Choose a small step size to ensure all intermediate tiles are checked (including diagonals)
    Vector2 step = direction.normalize().scale(0.1f);
    Point current = from;

    // Step from start to end and check each tile along the way
    for (float traveled = 0; traveled <= distance; traveled += step.length()) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (!tileIsAccessible(tile, canEnterPitTiles)) {
        return false;
      }
      current = current.translate(step);
    }

    // Ensure that the final destination tile is also checked
    return tileIsAccessible(Game.tileAt(to).orElse(null), canEnterPitTiles);
  }
}
