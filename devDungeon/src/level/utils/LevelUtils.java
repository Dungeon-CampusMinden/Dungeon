package level.utils;

import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.util.Optional;
import java.util.function.Consumer;
import level.DevDungeonLevel;
import systems.EventScheduler;

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
   * @return A Coordinate representing a random teleportation target within the current level. If
   *     the current level is not a DevDungeonLevel, or if no random teleportation target is
   *     available, null is returned.
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

  /**
   * Creates an explosion at the given center coordinate with the given range and delay spread. The
   * actionPerTile consumer is called for each tile within the explosion range.
   *
   * <p>The explosion happens in a circular area around the center coordinate. By using BFS, the
   * explosion is spread out from the center to the outer edges of the range. The delay spread
   * parameter determines the delay between each tile being processed (Using {@link
   * systems.EventScheduler}).
   *
   * @param center The center coordinate of the explosion.
   * @param range The range of the explosion.
   * @param delaySpread The delay between each tile being processed.
   * @param actionPerTile The action to be performed for each tile within the explosion range.
   */
  public static void explosionAt(
      Coordinate center, int range, long delaySpread, Consumer<Tile> actionPerTile) {
    // using a indexed for to increase the delay between each depth level
    for (int i = 0; i < range; i++) {
      final int depth = i;
      EventScheduler.getInstance()
          .scheduleAction(
              () -> {
                for (int x = center.x - depth; x <= center.x + depth; x++) {
                  for (int y = center.y - depth; y <= center.y + depth; y++) {
                    Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
                    if (tile != null && tile.coordinate().distance(center) <= depth) {
                      actionPerTile.accept(tile);
                    }
                  }
                }
              },
              delaySpread * i);
    }
  }
}
