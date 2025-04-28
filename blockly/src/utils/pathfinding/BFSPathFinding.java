package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;

/**
 * Breadth-First Search (BFS) pathfinding algorithm implementation.
 *
 * <p>This class implements the PathfindingLogic interface and provides the logic for performing BFS
 * pathfinding. It maintains the open and closed sets, tracks the path, and provides methods to
 * initialize the search, perform steps, and retrieve the final path.
 *
 * @see PathfindingLogic
 * @see systems.PathfindingSystem PathfindingSystem
 */
public class BFSPathFinding extends PathfindingLogic {

  /**
   * Constructor for BFSPathFinding.
   *
   * <p>This constructor initializes the BFS pathfinding algorithm with a FIFO queue for the open
   * set.
   *
   * @param start The starting coordinate for the pathfinding search.
   * @param end The ending coordinate for the pathfinding search.
   */
  public BFSPathFinding(Coordinate start, Coordinate end) {
    super(start, end, true);
  }

  @Override
  public void performSearch() {
    addToOpenSet(startNode);
    addToClosedSet(startNode);

    while (hasOpenNodes()) {
      Node current = pollNextNode();
      for (Coordinate neighbor : current.neighbors()) {
        if (!isClosed(neighbor)) {
          addToOpenSet(neighbor);
          addToClosedSet(neighbor);
        }
        if (neighbor.equals(endNode)) {
          addToClosedSet(neighbor);
          return;
        }
      }
    }
  }
}
