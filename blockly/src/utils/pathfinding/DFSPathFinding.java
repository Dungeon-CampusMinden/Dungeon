package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;
import utils.LevelUtils;

/**
 * Depth-First Search (DFS) pathfinding algorithm implementation.
 *
 * <p>It uses a last-in-first-out (LIFO) stack for the open set and a set (closed set) to keep track
 * of visited nodes.
 *
 * <p>The algorithm works as follows:
 * <ul>
 *   <li>Initialize a stack for nodes to explore (open set)</li>
 *   <li>Initialize a set to track visited nodes (closed set)</li>
 *   <li>Add the starting node to both the open set and closed set</li>
 *   <li>While there are nodes to explore in the open set:</li>
 *   <li>  Take the most recently added node from the stack</li>
 *   <li>  Mark this node as visited (add to closed set)</li>
 *   <li>  For each neighboring node that can be traversed:</li>
 *   <li>    If not already visited, add it to the open set</li>
 *   <li>    If this neighbor is the destination, end the search</li>
 * </ul>
 *
 * <p>This approach explores as far as possible along each branch before backtracking,
 * which is characteristic of depth-first search algorithms.
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
    super(start, end, new DFSStack());
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
