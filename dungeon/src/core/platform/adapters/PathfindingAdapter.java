package core.platform.adapters;

import core.level.Tile;
import core.level.elements.ILevel;
import java.util.List;
import java.util.Optional;

/**
 * Platform adapter interface for pathfinding algorithms.
 *
 * <p>PathfindingAdapter abstracts pathfinding functionality, allowing different implementations
 * to provide various pathfinding algorithms (e.g., A*, Dijkstra, BFS) without coupling the game
 * engine to a specific implementation.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Finding paths between two tiles in a level
 *   <li>Distinguishing unavailable pathfinding context from an empty path result
 * </ul>
 */
public interface PathfindingAdapter {

  /**
   * Finds a path from the start tile to the end tile within a level.
   *
   * <p>This method computes a sequence of tiles representing a path from start to end.
   * The implementation may use various pathfinding algorithms (A*, Dijkstra, etc.) to find
   * an optimal or valid path.
   *
   * <p>Return value semantics:
   * <ul>
   *   <li>If pathfinding could run, returns an {@link Optional} containing a list of tiles
   *   <li>If a path exists, that list contains the tiles from start to end
   *   <li>If no path exists, that list is empty
   *   <li>If pathfinding cannot run because the required context is unavailable, returns an empty
   *       {@link Optional}
   * </ul>
   *
   * @param level the level to search within
   * @param start the starting tile
   * @param end the destination tile
   * @return an {@link Optional} containing the path result, or empty if pathfinding cannot be
   *     performed
   */
  Optional<List<Tile>> findPath(ILevel level, Tile start, Tile end);
}
