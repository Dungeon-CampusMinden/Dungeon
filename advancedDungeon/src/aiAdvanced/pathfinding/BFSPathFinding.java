package aiAdvanced.pathfinding;

import core.level.utils.Coordinate;

/**
 * Breadth-First Search (BFS) pathfinding algorithm implementation.
 *
 * <p>It uses a {@link BFSQueue} (FIFO data structure) as the frontier set to keep track of nodes to
 * explore and a set (explored set) to keep track of visited nodes.
 *
 * <p>The algorithm works as follows:
 *
 * <p>This implementation provides a FIFO queue as the frontier data structure, with the core search
 * algorithm implemented in the parent class using the Template Method pattern.
 *
 * @see PathfindingLogic
 * @see aiAdvanced.systems.PathfindingSystem PathfindingSystem
 */
public class BFSPathFinding extends PathfindingLogic {

  /**
   * Constructor for BFSPathFinding.
   *
   * <p>This constructor initializes the BFS pathfinding algorithm with a FIFO queue for the
   * frontier set.
   *
   * @param start The starting coordinate for the pathfinding search.
   * @param end The ending coordinate for the pathfinding search.
   */
  public BFSPathFinding(Coordinate start, Coordinate end) {
    super(start, end, new BFSQueue());
  }
}
