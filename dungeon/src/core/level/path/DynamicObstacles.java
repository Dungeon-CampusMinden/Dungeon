package core.level.path;

import core.level.utils.Coordinate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dynamic obstacles that can be blocked or unblocked at runtime.
 *
 * <p>DynamicObstacles provides a thread-safe registry of coordinates that are considered
 * obstacles/blocked during pathfinding and movement calculations. This allows dynamic obstacle
 * management without modifying the static level geometry.
 *
 * <p>Use cases:
 * <ul>
 *   <li>Blocking paths around entities or objects
 *   <li>Temporarily disabling passage through certain coordinates
 *   <li>Dynamic environmental hazards or barriers
 * </ul>
 *
 * <p>All methods are thread-safe and handle null coordinates gracefully.
 *
 * <p>This class is not instantiable; all members are static.
 */
public final class DynamicObstacles {

  private static final Set<Coordinate> BLOCKED = ConcurrentHashMap.newKeySet();

  private DynamicObstacles() {}

  /**
   * Marks a coordinate as blocked/obstacle.
   *
   * <p>After blocking, the coordinate will be considered an obstacle for pathfinding and
   * movement checks. Null coordinates are ignored.
   *
   * @param c the coordinate to block, or null (safely ignored)
   */
  public static void block(final Coordinate c) {
    if (c != null) BLOCKED.add(c);
  }

  /**
   * Removes a blocked/obstacle status from a coordinate.
   *
   * <p>After unblocking, the coordinate will no longer be considered an obstacle.
   * Null coordinates are ignored.
   *
   * @param c the coordinate to unblock, or null (safely ignored)
   */
  public static void unblock(final Coordinate c) {
    if (c != null) BLOCKED.remove(c);
  }

  /**
   * Checks whether a coordinate is currently blocked/obstacle.
   *
   * @param c the coordinate to check, or null
   * @return true if the coordinate is blocked, false if unblocked or null
   */
  public static boolean isBlocked(final Coordinate c) {
    return c != null && BLOCKED.contains(c);
  }

  /**
   * Clears all blocked coordinates, removing all dynamic obstacles.
   *
   * <p>After calling this method, all coordinates will be unblocked.
   */
  public static void clear() {
    BLOCKED.clear();
  }
}
