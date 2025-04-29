package utils.pathfinding;

import core.level.utils.Coordinate;
import utils.LevelUtils;

/**
 * Depth-First Search (DFS) pathfinding algorithm implementation.
 *
 * <p>It uses a {@link DFSStack} (LIFO data structure) as the frontier set to keep track of nodes to
 * explore and a set (explored set) to keep track of visited nodes.
 *
 * <p>The algorithm works as follows:
 *
 * <ul>
 *   <li>Initialize a stack for the frontier set (nodes to explore)
 *   <li>Initialize a set for the explored set (visited nodes)
 *   <li>Add the starting node to the frontier set and mark it as visited
 *   <li>While there are nodes in the frontier set:
 *   <li>Take the most recently added node from the stack
 *   <li>Mark this node as visited if not already
 *   <li>For each walkable neighboring node:
 *   <li>If not already visited, add it to the frontier set
 *   <li>If this neighbor is the destination, end the search
 * </ul>
 *
 * @see PathfindingLogic
 * @see systems.PathfindingSystem PathfindingSystem
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

  @Override
  public void performSearch() {
    addFrontier(startNode);

    while (!isFrontierEmpty()) {
      Coordinate current = pollNextNode();
      addExplored(current);
      for (Coordinate neighbor : LevelUtils.walkableNeighbors(current)) {
        if (!isExplored(neighbor) && !isFrontier(neighbor)) {
          addFrontier(neighbor);
        }
        if (neighbor.equals(endNode)) {
          return;
        }
      }
    }
  }
}
