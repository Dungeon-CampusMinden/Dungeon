package core.platform.gdx;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import core.level.Tile;
import core.level.elements.ILevel;
import core.platform.PathfindingAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** libGDX/gdx-ai backed A* pathfinding implementation. */
public final class GdxPathfindingAdapter implements PathfindingAdapter {

  @Override
  public Optional<List<Tile>> findPath(final ILevel level, final Tile start, final Tile end) {
    if (level == null) return Optional.empty();
    if (start == null || end == null) return Optional.of(List.of());
    if (!start.isAccessible() || !end.isAccessible()) return Optional.of(List.of());

    final GraphPath<Tile> path = new DefaultGraphPath<>();
    new IndexedAStarPathFinder<>(level).searchNodePath(start, end, level.tileHeuristic(), path);

    if (path.getCount() <= 0) return Optional.of(List.of());

    final List<Tile> out = new ArrayList<>(path.getCount());
    for (int i = 0; i < path.getCount(); i++) {
      out.add(path.get(i));
    }
    return Optional.of(Collections.unmodifiableList(out));
  }
}
