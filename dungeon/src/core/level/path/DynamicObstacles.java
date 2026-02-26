package core.level.path;

import core.level.utils.Coordinate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine-agnostic registry for dynamic pathfinding obstacles (e.g. entities with a
 * BlockComponent).
 *
 * <p>This class maintains a thread-safe set of {@link Coordinate}s that are currently considered
 * blocked for pathfinding purposes. Systems that manage blocking entities are responsible for
 * registering and unregistering coordinates via {@link #block(Coordinate)} and {@link
 * #unblock(Coordinate)}.
 *
 * <p>This class cannot be instantiated.
 */
public final class DynamicObstacles {
  private static final Set<Coordinate> BLOCKED = ConcurrentHashMap.newKeySet();

  /** Private constructor to prevent instantiation of this utility class. */
  private DynamicObstacles() {}

  /**
   * Marks the given coordinate as blocked.
   *
   * <p>If the coordinate is already blocked, this method has no effect.
   *
   * @param c the coordinate to block; {@code null} values are silently ignored
   */
  public static void block(final Coordinate c) {
    if (c != null) BLOCKED.add(c);
  }

  /**
   * Removes the blocked state from the given coordinate.
   *
   * <p>If the coordinate is not currently blocked, this method has no effect.
   *
   * @param c the coordinate to unblock; {@code null} values are silently ignored
   */
  public static void unblock(final Coordinate c) {
    if (c != null) BLOCKED.remove(c);
  }

  /**
   * Returns whether the given coordinate is currently blocked.
   *
   * @param c the coordinate to check
   * @return {@code true} if the coordinate is blocked, {@code false} if it is not blocked or
   *     {@code null}
   */
  public static boolean isBlocked(final Coordinate c) {
    return c != null && BLOCKED.contains(c);
  }

  /**
   * Removes all blocked coordinates from the registry.
   *
   * <p>This can be used when a level is unloaded or reset to ensure no stale obstacles remain.
   */
  public static void clear() {
    BLOCKED.clear();
  }
}
