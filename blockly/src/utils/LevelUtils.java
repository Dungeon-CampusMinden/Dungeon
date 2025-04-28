package utils;

import components.BlockFireBallComponent;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.util.Arrays;
import java.util.List;

/** Utility class for level-related operations. */
public class LevelUtils {
  /**
   * Checks if Coordinate A can see Coordinate B in the given direction.
   *
   * @param coordinateA The first Coordinate.
   * @param CoordinateB The second Coordinate.
   * @param direction The direction in which to check visibility.
   * @return True if the two points are in line of sight, false otherwise.
   */
  public static boolean canSee(
      Coordinate coordinateA, Coordinate CoordinateB, Direction direction) {
    if (coordinateA.equals(CoordinateB)) return true;

    Tile firstTile = Game.currentLevel().tileAt(coordinateA);
    Tile currentTile = Game.currentLevel().tileAt(coordinateA);
    Tile targetTile = Game.currentLevel().tileAt(CoordinateB);
    if (targetTile == null || !targetTile.canSeeThrough()) return false;
    while (!targetTile.equals(currentTile)) {
      if (currentTile == null || !currentTile.canSeeThrough()) return false;

      if (!currentTile.equals(firstTile)
          && !currentTile.equals(targetTile)
          && Game.entityAtTile(currentTile)
              .anyMatch(e -> e.isPresent(BlockFireBallComponent.class))) {
        return false; // if there is a blockFireball in the way, we can't see through
      }

      currentTile =
          currentTile
              .level()
              .tileAt(currentTile.position().add(new Point(direction.x(), direction.y())));
    }
    return true;
  }

  /**
   * Checks if a given coordinate is walkable.
   *
   * @param coord The coordinate to check.
   * @return true if the tile at the given coordinate is accessible, false otherwise.
   */
  public static boolean isWalkable(Coordinate coord) {
    Tile tile = Game.tileAT(coord);
    return tile != null && tile.isAccessible();
  }

  /**
   * Retrieves a list of walkable neighboring coordinates for a given coordinate.
   *
   * @param coord The coordinate for which to find walkable neighbors.
   * @return A list of walkable neighboring coordinates. Returns an empty list if the given
   *     coordinate is not walkable.
   */
  public static List<Coordinate> walkableNeighbors(Coordinate coord) {
    return Arrays.stream(Direction.values())
        .filter(direction -> direction != Direction.HERE)
        .map(direction -> coord.add(new Coordinate(direction.x(), direction.y())))
        .filter(LevelUtils::isWalkable)
        .toList();
  }
}
