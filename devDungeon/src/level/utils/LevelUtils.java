package level.utils;

import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.util.Optional;
import level.DevDungeonLevel;

/** Utility class for level-related operations. */
public class LevelUtils {

  /**
   * Changes the visibility of a rectangular area within the level. The area is defined by the top
   * left and bottom right coordinates. If a tile within the specified area is null, it is skipped.
   *
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area.
   * @param visible The visibility status to be set for the area.
   */
  public static void changeVisibilityForArea(
      Coordinate topLeft, Coordinate bottomRight, boolean visible) {
    for (int x = topLeft.x; x <= bottomRight.x; x++) {
      for (int y = bottomRight.y; y <= topLeft.y; y++) {
        Tile tile = Game.tileAT(new Coordinate(x, y));
        if (tile != null) {
          tile.visible(visible);
        }
      }
    }
  }

  /**
   * Checks if a given Tile is within a given area.
   *
   * @param tile The tile to check.
   * @param topLeft The top left coordinate of the area.
   * @param bottomRight The bottom right coordinate of the area. * @return true if the tile is
   *     within the area, false if not.
   */
  public static boolean isTileWithinArea(Tile tile, Coordinate topLeft, Coordinate bottomRight) {
    return tile.coordinate().x >= topLeft.x
        && tile.coordinate().x <= bottomRight.x
        && tile.coordinate().y >= bottomRight.y
        && tile.coordinate().y <= topLeft.y;
  }

  /**
   * Returns a random teleportation target for the current level.
   *
   * <p>This method retrieves the current level from the game, casts it to a DevDungeonLevel, and
   * then calls the randomTPTarget method of the level to get a random teleportation target.
   *
   * @return A Coordinate representing a random teleportation target within the current level.
   * @see DevDungeonLevel#randomTPTarget()
   * @see entities.TPBallSkill TPBallSkill
   */
  public static Point getRandomTPTargetForCurrentLevel() {
    DevDungeonLevel level;
    try {
      level = (DevDungeonLevel) Game.currentLevel();
    } catch (ClassCastException e) {
      return null;
    }
    return Optional.ofNullable(level.randomTPTarget())
        .map(Coordinate::toCenteredPoint)
        .orElse(null);
  }
}
