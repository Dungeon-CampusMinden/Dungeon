package level.utils;

import contrib.systems.EventScheduler;
import contrib.utils.components.skill.damageSkill.projectile.TPBallSkill;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Optional;
import java.util.function.Consumer;
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
   * @see TPBallSkill TPBallSkill
   */
  public static Point getRandomTPTargetForCurrentLevel() {
    DevDungeonLevel level;
    try {
      level = (DevDungeonLevel) Game.currentLevel().orElse(null);
    } catch (ClassCastException e) {
      return null;
    }
    return Optional.ofNullable(level.randomTPTarget())
        .map(Coordinate::toCenteredPoint)
        .orElse(null);
  }

  /**
   * Simulates an explosion at a given center coordinate, affecting tiles within a specified range.
   * The explosion spreads in a circular pattern and is obstructed by walls, ceasing to affect tiles
   * beyond them. Actions are performed on each affected tile with a delay between consecutive rings
   * of the explosion.
   *
   * @param center The center coordinate of the explosion.
   * @param range The maximum range of the explosion, defined as the radius of the circle.
   * @param delaySpread The delay (in milliseconds) before actions are performed on tiles in the
   *     next outward ring.
   * @param actionPerTile A consumer action to be performed on each tile within the explosion range
   *     and line of sight.
   */
  public static void explosionAt(
      Coordinate center, int range, long delaySpread, Consumer<Tile> actionPerTile) {
    for (int i = 0; i <= range; i++) {
      final int radius = i;
      EventScheduler.scheduleAction(
          () -> {
            for (int dx = -radius; dx <= radius; dx++) {
              for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy > radius * radius) continue; // Ensure circular pattern

                Coordinate target = new Coordinate(center.x() + dx, center.y() + dy);
                if (!hasLineOfSight(center, target)) {
                  continue; // Skip if no direct line of sight
                }

                Tile tile = Game.tileAt(target).orElse(null);
                if (tile != null) {
                  actionPerTile.accept(tile);
                }
              }
            }
          },
          delaySpread * i);
    }
  }

  /**
   * Determines if there is a direct line of sight between two coordinates on the game map, not
   * obstructed by walls. Utilizes Bresenham's line algorithm to iterate over the grid points
   * between the two coordinates. A wall encountered along this line blocks the line of sight.
   *
   * @param center The starting coordinate of the line of sight.
   * @param target The target coordinate to check visibility to.
   * @return true if there is an unobstructed line of sight from the center to the target, false if
   *     obstructed by walls.
   */
  private static boolean hasLineOfSight(Coordinate center, Coordinate target) {
    int x = center.x();
    int y = center.y();
    int dx = Math.abs(target.x() - center.x());
    int dy = Math.abs(target.y() - center.y());

    int sx = center.x() < target.x() ? 1 : -1;
    int sy = center.y() < target.y() ? 1 : -1;
    int err = dx - dy;
    int e2;

    while (true) {
      // Stop if the current tile is a wall
      Tile tile = Game.tileAt(new Coordinate(x, y)).orElse(null);
      if (tile != null && tile.levelElement() == LevelElement.WALL) {
        return false;
      }

      if (x == target.x() && y == target.y()) {
        break;
      }

      e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        x += sx;
      }
      if (e2 < dx) {
        err += dx;
        y += sy;
      }
    }

    return true; // Line of sight is clear if we reach this point
  }
}
