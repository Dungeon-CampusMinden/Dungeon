package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;

/**
 * Depth-First Search (DFS) pathfinding algorithm implementation.
 *
 * <p>This class implements the PathfindingLogic interface and provides the logic for performing DFS
 * pathfinding. It maintains the open and closed sets as a LIFO stack, tracks the path, and provides
 * methods to initialize the search, perform steps, and retrieve the final path.
 *
 * @see PathfindingLogic
 * @see systems.PathfindingSystem PathfindingSystem
 */
public class DFSPathFinding extends PathfindingLogic {

  /**
   * Constructor for DFSPathFinding.
   *
   * <p>This constructor initializes the DFS pathfinding algorithm with a LIFO stack for the open
   * set by passing false to the superclass constructor.
   *
   * @param start The starting coordinate for the pathfinding search.
   * @param end The ending coordinate for the pathfinding search.
   */
  public DFSPathFinding(Coordinate start, Coordinate end) {
    super(start, end, false);
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
