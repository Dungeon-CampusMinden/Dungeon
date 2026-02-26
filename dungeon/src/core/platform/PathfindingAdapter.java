package core.platform;

import core.level.Tile;
import core.level.elements.ILevel;
import java.util.List;
import java.util.Optional;

/**
 * Backend-specific pathfinding that can be swapped per host (libGDX, LITIENGINE, headless).
 *
 * <p>Core code must not depend on a concrete pathfinding implementation.
 */
public interface PathfindingAdapter {
  /**
   * Computes a path from start to end in the given level.
   *
   * <p>Return {@code Optional.empty()} if no level/context is available.
   * Return {@code Optional.of(List.of())} for "no path".
   */
  Optional<List<Tile>> findPath(ILevel level, Tile start, Tile end);
}
