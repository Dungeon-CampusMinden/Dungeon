package level.utils;

import core.Game;
import core.level.utils.Coordinate;
import core.utils.Point;
import java.util.Optional;
import level.DevDungeonLevel;

/** Utility class for level-related operations. */
public class LevelUtils {

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
   */
  public static Point getRandomTPTargetForCurrentLevel() {
    DevDungeonLevel level;
    try {
      level = (DevDungeonLevel) Game.currentLevel().orElse(null);
    } catch (ClassCastException e) {
      return null;
    }
    return Optional.ofNullable(level.randomTPTarget()).map(Coordinate::toPoint).orElse(null);
  }
}
