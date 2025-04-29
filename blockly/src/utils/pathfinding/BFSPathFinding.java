package utils.pathfinding;

import core.level.utils.Coordinate;
import utils.LevelUtils;

/**
 * Breadth-First Search (BFS) pathfinding algorithm implementation.
 *
 * <p>It uses a queue (open set) to keep track of nodes to explore and a set (closed set) to keep
 * track of visited nodes.
 *
 * <p>Pseudocode:
 *
 * <pre>
 *   BFS(start, end):
 *   openSet = new Queue()
 *   closedSet = new Set()
 *   openSet.add(start)
 *   closedSet.add(start)
 *   while openSet is not empty:
 *     current = openSet.poll()
 *     closedSet.add(current)
 *     for each neighbor of current:
 *       if neighbor is not in closedSet:
 *         openSet.add(neighbor)
 *       if neighbor == end:
 *         return
 * </pre>
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
    super(start, end, new BFSQueue());
  }

  @Override
  public void performSearch() {
    addToOpenSet(startNode);
    addToClosedSet(startNode);

    while (hasOpenNodes()) {
      Coordinate current = pollNextNode();
      addToClosedSet(current);
      for (Coordinate neighbor : LevelUtils.walkableNeighbors(current)) {
        if (!isClosed(neighbor)) {
          addToOpenSet(neighbor);
        }
        if (neighbor.equals(endNode)) {
          return;
        }
      }
    }
  }
}
