package core.level.path;

import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.platform.adapters.PathfindingAdapter;
import java.util.*;

/**
 * A PathfindingAdapter implementation using breadth-first search (BFS) on grid-based levels.
 *
 * <p>GridPathfindingAdapter implements pathfinding for grid-based dungeons by applying BFS,
 * which guarantees to find the shortest path (in terms of tile count) if one exists.
 *
 * <p>Pathfinding behavior:
 * <ul>
 *   <li>Searches the 4-directional grid (up, down, left, right)
 *   <li>Only traverses accessible tiles
 *   <li>Respects dynamic obstacles managed by DynamicObstacles
 *   <li>Returns paths as lists of tiles (inclusive of start and end)
 *   <li>Returns empty paths if start/end are the same, if the end is blocked, or if no path exists
 * </ul>
 *
 * <p>Return semantics:
 * <ul>
 *   <li>Valid path: Optional containing an unmodifiable list of tiles from start to end
 *   <li>No path exists: Optional containing an empty list
 *   <li>Invalid input (null level, null start/end): Optional containing an empty list or single tile
 * </ul>
 */
public final class GridPathfindingAdapter implements PathfindingAdapter {
  private static final int[][] DIRS = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

  @Override
  public Optional<List<Tile>> findPath(final ILevel level, final Tile start, final Tile end) {
    if (level == null) return Optional.empty();
    if (start == null || end == null) return Optional.of(List.of());
    if (!start.isAccessible() || !end.isAccessible()) return Optional.of(List.of());
    if (start == end) return Optional.of(List.of(start));

    final Coordinate startC = start.coordinate();
    final Coordinate goalC = end.coordinate();

    if (DynamicObstacles.isBlocked(goalC)) return Optional.of(List.of());

    final ArrayDeque<Coordinate> q = new ArrayDeque<>();
    final Map<Coordinate, Coordinate> cameFrom = new HashMap<>();
    final Set<Coordinate> visited = new HashSet<>();

    q.add(startC);
    visited.add(startC);

    while (!q.isEmpty()) {
      final Coordinate cur = q.removeFirst();
      if (cur.equals(goalC)) break;

      for (int[] d : DIRS) {
        final Coordinate next = new Coordinate(cur.x() + d[0], cur.y() + d[1]);
        if (visited.contains(next)) continue;
        if (DynamicObstacles.isBlocked(next)) continue;

        final Tile t = level.tileAt(next).orElse(null);
        if (t == null || !t.isAccessible()) continue;

        visited.add(next);
        cameFrom.put(next, cur);
        q.addLast(next);
      }
    }

    if (!visited.contains(goalC)) return Optional.of(List.of());

    // Reconstruct (inclusive: start...end)
    final List<Tile> tiles = new ArrayList<>();
    Coordinate cur = goalC;

    while (true) {
      level.tileAt(cur).ifPresent(tiles::add);

      if (cur.equals(startC)) break;

      cur = cameFrom.get(cur);
      if (cur == null) return Optional.of(List.of()); // defensive fallback
    }

    Collections.reverse(tiles);
    return Optional.of(Collections.unmodifiableList(tiles));
  }
}
