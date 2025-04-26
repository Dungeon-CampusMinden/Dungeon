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
   */
  public BFSPathFinding() {
    super(true);
  }

  @Override
  public void performSearch(Coordinate start, Coordinate end) {
    addToOpenSet(start);
    addToClosedSet(start);

    while (hasOpenNodes()) {
      Node current = pollNextNode();
      for (Coordinate neighbor : current.neighbors()) {
        if (!isClosed(neighbor)) {
          addToOpenSet(neighbor);
          addToClosedSet(neighbor);
        }
        if (neighbor.equals(end)) {
          addToClosedSet(neighbor);
          return;
        }
      }
    }
  }
}
