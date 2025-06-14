package aiAdvanced.pathfinding;

import contrib.utils.LevelUtils;
import core.level.utils.Coordinate;
import core.utils.Tuple;
import java.util.*;

/**
 * PathfindingLogic provides a basic structure for pathfinding algorithms.
 *
 * <p>This class maintains the frontier and explored sets, tracks the path, and provides methods to
 * initialize the search, perform steps, and retrieve the final path.
 *
 * <p>It is designed to be extended by specific pathfinding algorithms, such as BFS or DFS.
 *
 * <p>It uses a data structure for the frontier set that can be configured as either FIFO or LIFO
 * based on the implementation provided in the constructor.
 *
 * <p>The class implements the Template Method pattern for the core search algorithm, allowing
 * subclasses to simply provide the appropriate data structure to determine traversal behavior.
 *
 * @see PathfindingVisualizer
 * @see aiAdvanced.systems.PathfindingSystem PathfindingSystem
 */
public abstract class PathfindingLogic {
  /** The starting coordinate for the pathfinding search. */
  protected final Coordinate startNode;

  /** The ending coordinate for the pathfinding search. */
  protected final Coordinate endNode;

  private final ArrayDeque<Coordinate> frontierSet;
  private final Set<Coordinate> exploredSet = new HashSet<>();
  private final List<Tuple<Coordinate, TileState>> steps = new ArrayList<>();
  private final Map<Coordinate, Coordinate> cameFrom = new HashMap<>();
  private Coordinate lastParent = null;

  /**
   * Constructor for PathfindingLogic.
   *
   * @param startNode The starting coordinate for the pathfinding search.
   * @param endNode The ending coordinate for the pathfinding search.
   * @param frontierSet The data structure to use for the frontier set (FIFO or LIFO).
   */
  protected PathfindingLogic(
      Coordinate startNode, Coordinate endNode, ArrayDeque<Coordinate> frontierSet) {
    this.startNode = startNode;
    this.endNode = endNode;
    this.frontierSet = frontierSet;
  }

  /**
   * Add a coordinate to the frontier set.
   *
   * <p>A node is in the frontier set if it has been discovered but not yet processed.
   *
   * <p>This method also keeps track of the last parent node for backtracking purposes.
   *
   * @param coord The coordinate to add.
   */
  protected void addFrontier(Coordinate coord) {
    if (lastParent != null) {
      cameFrom.put(coord, lastParent);
    }
    steps.add(Tuple.of(coord, TileState.OPEN));
    frontierSet.push(coord);
  }

  /**
   * Poll the next node from the frontier set.
   *
   * <p>This method retrieves and removes the next node to be processed from the frontier set.
   *
   * @return The next node to be processed. Returns null if the frontier set is empty.
   */
  protected Coordinate pollNextNode() {
    Coordinate node = frontierSet.pop();
    steps.add(Tuple.of(node, TileState.CURRENT));
    lastParent = node;
    return node;
  }

  /**
   * Check if the frontier set is empty.
   *
   * @return True if the frontier set is empty, false otherwise.
   */
  protected boolean isFrontierEmpty() {
    return frontierSet.isEmpty();
  }

  /**
   * Add a coordinate to the explored set.
   *
   * <p>A node is in the explored set if it has been processed and marked as visited.
   *
   * @param coord The coordinate to add to the explored set.
   */
  protected void addExplored(Coordinate coord) {
    steps.add(Tuple.of(coord, TileState.CLOSED));
    exploredSet.add(coord);
  }

  /**
   * Check if the coordinate is in the explored set.
   *
   * <p>A node is in the explored set if it has been processed and marked as visited.
   *
   * @param coord The coordinate to check.
   * @return True if the coordinate is in the explored set, false otherwise.
   */
  protected boolean isExplored(Coordinate coord) {
    return exploredSet.contains(coord);
  }

  /**
   * Check if the coordinate is in the frontier set.
   *
   * <p>A node is in the frontier set if it has been discovered but not yet processed.
   *
   * @param coord The coordinate to check.
   * @return True if the coordinate is in the frontier set, false otherwise.
   */
  protected boolean isFrontier(Coordinate coord) {
    return frontierSet.contains(coord);
  }

  /**
   * Provides direct access to the frontier set for subclasses.
   *
   * @return the deque used as the frontier.
   */
  protected ArrayDeque<Coordinate> frontierSet() {
    return frontierSet;
  }

  /**
   * Build the final path from the end coordinate to the start coordinate.
   *
   * @return A list of nodes representing the final path from start to end.
   */
  public List<Coordinate> finalPath() {
    List<Coordinate> path = new ArrayList<>();
    Coordinate curr = this.endNode;
    while (curr != null) {
      path.add(curr);
      curr = cameFrom.get(curr);
    }
    Collections.reverse(path);
    return path;
  }

  /**
   * All taken steps in the pathfinding search in the order they were taken.
   *
   * <p>This includes all nodes added to the frontier set or marked as explored and all nodes polled
   * from the frontier set.
   *
   * @return A list of tuples containing the node and its {@link TileState}.
   * @see PathfindingVisualizer
   */
  public List<Tuple<Coordinate, TileState>> steps() {
    return steps;
  }

  /**
   * Perform the pathfinding search.
   *
   * <p>This is a template method that implements the core search algorithm common to BFS, DFS, and
   * other graph search approaches. The algorithm follows these steps:
   *
   * <ol>
   *   <li>Add start node to frontier
   *   <li>While frontier is not empty:
   *       <ol>
   *         <li>Get next node from frontier (order determined by frontier data structure)
   *         <li>Mark node as explored
   *         <li>For each neighbor:
   *             <ol>
   *               <li>If not explored or in frontier, add to frontier
   *               <li>If neighbor is target, end search
   *             </ol>
   *       </ol>
   * </ol>
   */
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
