package core.platform.gdx;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.path.DynamicObstacles;
import core.level.utils.Coordinate;
import core.platform.PathfindingAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * LibGDX-ai based {@link PathfindingAdapter} implementation.
 *
 * <p>Acts as an adapter between the engine-agnostic {@code ILevel}/{@code Tile} model and the
 * gdx-ai A* implementation by building a lightweight {@code IndexedGraph} on demand.</p>
 *
 * <p>Returns an {@link java.util.Optional} containing the path as a {@link java.util.List} of
 * {@link core.level.Tile} instances. If no path can be found (or inputs are invalid/not accessible),
 * an empty list is returned inside the {@code Optional}.</p>
 */
public final class GdxPathfindingAdapter implements PathfindingAdapter {

  private static final Heuristic<Tile> MANHATTAN = (from, to) -> {
    if (from == null || to == null) return 0f;
    return from.distance(to);
  };

  @Override
  public Optional<List<Tile>> findPath(final ILevel level, final Tile start, final Tile end) {
    if (level == null) return Optional.empty();
    if (start == null || end == null) return Optional.of(List.of());
    if (!start.isAccessible() || !end.isAccessible()) return Optional.of(List.of());

    final IndexedGraph<Tile> graph = new LevelGraph(level);
    final GraphPath<Tile> out = new DefaultGraphPath<>();

    final boolean found = new IndexedAStarPathFinder<>(graph).searchNodePath(start, end, MANHATTAN, out);
    if (!found || out.getCount() <= 0) return Optional.of(List.of());

    final List<Tile> path = new ArrayList<>(out.getCount());
    for (int i = 0; i < out.getCount(); i++) {
      path.add(out.get(i));
    }
    return Optional.of(path);
  }

  /**
   * gdx-ai IndexedGraph wrapper around our engine-agnostic ILevel.
   * This keeps all gdx-ai graph types out of core.level.*.
   */
  private static final class LevelGraph implements IndexedGraph<Tile> {
    private final ILevel level;
    private final int width;
    private final int height;
    private final int nodeCount;

    private LevelGraph(final ILevel level) {
      this.level = level;
      final Tile[][] layout = level.layout();
      this.height = layout != null ? layout.length : 0;
      this.width = (layout != null && layout.length > 0) ? layout[0].length : 0;
      this.nodeCount = Math.max(0, width * height);
    }

    @Override
    public int getNodeCount() {
      return nodeCount;
    }

    @Override
    public int getIndex(final Tile node) {
      if (node == null) return 0;
      final Coordinate c = node.coordinate();
      return c.y() * width + c.x();
    }

    @Override
    public Array<Connection<Tile>> getConnections(final Tile fromNode) {
      final Array<Connection<Tile>> out = new Array<>(4);
      if (fromNode == null) return out;

      final Coordinate c = fromNode.coordinate();

      addIfWalkable(out, fromNode, c.x() + 1, c.y());
      addIfWalkable(out, fromNode, c.x() - 1, c.y());
      addIfWalkable(out, fromNode, c.x(), c.y() + 1);
      addIfWalkable(out, fromNode, c.x(), c.y() - 1);

      return out;
    }

    private void addIfWalkable(Array<Connection<Tile>> out, Tile from, int x, int y) {
      Coordinate next = new Coordinate(x, y);
      if (DynamicObstacles.isBlocked(next)) return;

      level.tileAt(next)
        .filter(Tile::isAccessible)
        .ifPresent(to -> out.add(new TileConnection(from, to)));
    }
  }

  /**
   * gdx-ai Connection implementation (moved out of core.level.elements.astar).
   */
  private static final class TileConnection implements Connection<Tile> {
    private final Tile from;
    private final Tile to;
    private final float cost;

    private TileConnection(final Tile from, final Tile to) {
      this.from = from;
      this.to = to;
      this.cost = from != null && to != null ? from.distance(to) : 1f;
    }

    @Override public float getCost() { return cost; }
    @Override public Tile getFromNode() { return from; }
    @Override public Tile getToNode() { return to; }
  }
}
