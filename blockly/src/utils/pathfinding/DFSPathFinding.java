package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;

/**
 * Depth-First Search (DFS) pathfinding algorithm implementation.
 *
 * <p>It uses a last-in-first-out (LIFO) stack for the open set and a set (closed set) to keep track
 * of visited nodes.
 *
 * <p>Pseudocode:
 *
 * <pre>
 *   BFS(start, end):
 *   openSet = new Stack()
 *   closedSet = new Set()
 *   openSet.add(start)
 *   closedSet.add(start)
 *   while openSet is not empty:
 *     current = openSet.pop()
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
      addToClosedSet(current.coordinate());
      for (Coordinate neighbor : current.neighbors()) {
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
