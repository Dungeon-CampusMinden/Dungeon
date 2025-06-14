package aiAdvanced.pathfinding;

import core.level.Tile;
import core.level.utils.Coordinate;

/**
 * Depth-First Search (DFS) pathfinding algorithm implementation.
 *
 * <p>It uses a {@link DFSStack} (LIFO data structure) as the frontier set to keep track of nodes to
 * explore and a set (explored set) to keep track of visited nodes.
 *
 * <p>The algorithm works as follows:
 *
 * <p>This implementation provides a LIFO stack as the frontier data structure, with the core search
 * algorithm implemented in the parent class using the Template Method pattern.
 *
 * @see PathfindingLogic
 * @see aiAdvanced.systems.PathfindingSystem PathfindingSystem
 */
public class DFSPathFinding extends PathfindingLogic {

  /**
   * Constructor for DFSPathFinding.
   *
   * <p>This constructor initializes the DFS pathfinding algorithm with a LIFO stack for the
   * frontier set.
   *
   * @param start The starting coordinate for the pathfinding search.
   * @param end The ending coordinate for the pathfinding search.
   */
  public DFSPathFinding(Coordinate start, Coordinate end) {
    super(start, end, new DFSStack());
  }

  /**
   * Constructor for DFSPathFinding.
   *
   * <p>This constructor initializes the DFS pathfinding algorithm with a LIFO stack for the
   * frontier set.
   *
   * @param start The start tile for the pathfinding search.
   * @param end The end tile for the pathfinding search.
   */
  public DFSPathFinding(Tile start, Tile end) {
    this(start.coordinate(), end.coordinate());
  }
}
