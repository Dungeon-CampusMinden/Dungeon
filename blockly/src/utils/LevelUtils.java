package utils;

import components.BlockFireBallComponent;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;

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
}
