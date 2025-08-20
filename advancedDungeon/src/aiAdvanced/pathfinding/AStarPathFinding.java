package aiAdvanced.pathfinding;

import contrib.utils.LevelUtils;
import core.Game;
import core.level.Tile;
import core.level.elements.astar.TileConnection;
import core.level.elements.astar.TileHeuristic;
import core.level.utils.Coordinate;
import java.util.*;

/**
 * A* (A-Star) pathfinding algorithm implementation.
 *
 * <p>It uses a {@link AStarPriorityQueue} (priority queue data structure) as the frontier set to
 * explore nodes based on their estimated total cost, maintaining g-scores (actual path cost) and
 * f-scores (g-score + heuristic) to determine optimal paths.
 *
 * <p>The algorithm works by prioritizing nodes that appear most promising based on both the cost
 * already accumulated to reach them and their estimated distance to the goal.
 *
 * <p>This implementation provides a priority queue as the frontier data structure, with nodes
 * ordered by their f-scores. It overrides the parent's search algorithm to implement the A*
 * specific behavior.
 *
 * @see PathfindingLogic
 * @see TileHeuristic
 * @see TileConnection
 * @see aiAdvanced.systems.PathfindingSystem PathfindingSystem
 */
public class AStarPathFinding extends PathfindingLogic {
  private final Map<Coordinate, Double> gScore = new HashMap<>();
  private final Map<Coordinate, Double> fScore = new HashMap<>();
  private final TileHeuristic heuristic = new TileHeuristic();

  /**
   * Constructor for AStarPathFinding.
   *
   * @param startNode The starting coordinate for the pathfinding search.
   * @param endNode The ending coordinate for the pathfinding search.
   */
  public AStarPathFinding(Coordinate startNode, Coordinate endNode) {
    super(startNode, endNode, new AStarPriorityQueue());
    // Initialize gScore and fScore maps
    gScore.put(startNode, 0.0);
    fScore.put(startNode, heuristicCost(startNode, endNode));
  }

  /**
   * Calculate heuristic cost between two coordinates using TileHeuristic.
   *
   * @param from source coordinate
   * @param to destination coordinate
   * @return estimated cost to reach destination
   */
  private double heuristicCost(Coordinate from, Coordinate to) {
    // Create temporary Tile objects to use with TileHeuristic
    Tile fromTile = Game.tileAt(from).orElse(null);
    Tile toTile = Game.tileAt(to).orElse(null);

    return heuristic.estimate(fromTile, toTile);
  }

  /**
   * Perform the A* pathfinding search.
   *
   * <p>This method overrides the template method to implement the A* algorithm that balances actual
   * path costs with heuristic estimations. The algorithm follows these steps:
   *
   * <ol>
   *   <li>Add start node to frontier with f-score = h-score
   *   <li>While frontier is not empty:
   *       <ol>
   *         <li>Get node with lowest f-score from frontier
   *         <li>If node is goal, end search
   *         <li>Mark node as explored
   *         <li>For each neighbor:
   *             <ol>
   *               <li>Calculate tentative g-score = current g-score + cost to neighbor
   *               <li>If tentative g-score is better than previous:
   *                   <ol>
   *                     <li>Update g-score and f-score (f = g + h)
   *                     <li>If neighbor not in frontier, add it
   *                     <li>Update neighbor's priority in frontier
   *                   </ol>
   *             </ol>
   *       </ol>
   * </ol>
   */
  @Override
  public void performSearch() {
    // Add start node to frontier
    addFrontier(startNode);

    while (!isFrontierEmpty()) {
      // Get the node with lowest f-score
      Coordinate current = pollNextNode();

      // If we've reached the goal, we can stop
      if (current.equals(endNode)) {
        return;
      }

      // Mark as explored
      addExplored(current);

      // Check all neighbors
      for (Coordinate neighbor : LevelUtils.walkableNeighbors(current)) {
        // Skip if already explored
        if (isExplored(neighbor)) {
          continue;
        }

        // Calculate tentative gScore using TileConnection for cost
        double tentativeGScore =
            gScore.getOrDefault(current, Double.MAX_VALUE) + connectionCost(current, neighbor);

        // If this is a better path to neighbor or neighbor not in frontier
        if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
          // Update scores
          gScore.put(neighbor, tentativeGScore);
          fScore.put(neighbor, tentativeGScore + heuristicCost(neighbor, endNode));

          // Add to frontier if not already there
          if (!isFrontier(neighbor)) {
            addFrontier(neighbor);

            // Use inherited getter instead of reflection
            ((AStarPriorityQueue) frontierSet()).updatePriority(neighbor, fScore.get(neighbor));
          }
        }
      }
    }
  }

  /**
   * Calculate cost between two coordinates using {@link TileConnection}.
   *
   * @param from source coordinate
   * @param to destination coordinate
   * @return cost of moving from source to destination
   */
  private double connectionCost(Coordinate from, Coordinate to) {
    // Create temporary Tile objects
    Tile fromTile = Game.tileAt(from).orElse(null);
    Tile toTile = Game.tileAt(to).orElse(null);

    // Create a TileConnection between the tiles and get its cost
    TileConnection connection = new TileConnection(fromTile, toTile);
    return connection.getCost();
  }
}
