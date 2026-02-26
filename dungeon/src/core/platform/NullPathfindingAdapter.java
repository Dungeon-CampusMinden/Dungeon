package core.platform;

import core.level.Tile;
import core.level.elements.ILevel;
import java.util.List;
import java.util.Optional;

/** Safe default: no pathfinding available. */
public final class NullPathfindingAdapter implements PathfindingAdapter {
  @Override
  public Optional<List<Tile>> findPath(ILevel level, Tile start, Tile end) {
    if (level == null) return Optional.empty();
    return Optional.of(List.of());
  }
}
