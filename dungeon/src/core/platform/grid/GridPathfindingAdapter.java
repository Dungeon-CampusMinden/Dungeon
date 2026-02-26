package core.platform.grid;

import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.platform.PathfindingAdapter;
import java.util.*;

/**
 * Pure-Java grid pathfinding (4-neighborhood) as a backend-agnostic fallback.
 *
 * <p>Uses BFS (uniform cost). Returns shortest path in number of steps.
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

        final Tile t = level.tileAt(next).orElse(null);
        if (t == null || !t.isAccessible()) continue;

        visited.add(next);
        cameFrom.put(next, cur);
        q.addLast(next);
      }
    }

    if (!visited.contains(goalC)) return Optional.of(List.of());

    // Reconstruct (inclusive: start..end)
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
