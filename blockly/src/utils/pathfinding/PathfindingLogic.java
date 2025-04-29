package utils.pathfinding;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Tuple;
import java.util.*;

/**
 * PathfindingLogic is an abstract class that provides the basic structure for pathfinding
 * algorithms.
 *
 * <p>This class maintains the frontier and explored sets, tracks the path, and provides methods to
 * initialize the search, perform steps, and retrieve the final path.
 *
 * <p>It is designed to be extended by specific pathfinding algorithms, such as BFS or DFS.
 *
 * <p>It uses a data structure for the frontier set that can be configured as either FIFO or LIFO
 * based on the implementation provided in the constructor.
 *
 * @see PathfindingVisualizer
 * @see systems.PathfindingSystem PathfindingSystem
 */
public abstract class PathfindingLogic {
  /** The starting coordinate for the pathfinding search. */
  protected final Coordinate startNode;

  /** The ending coordinate for the pathfinding search. */
  protected final Coordinate endNode;

  private final GraphSearchDataStructure<Coordinate> frontierSet;
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
      Coordinate startNode, Coordinate endNode, GraphSearchDataStructure<Coordinate> frontierSet) {
    this.startNode = startNode;
    this.endNode = endNode;
    this.frontierSet = frontierSet;
  }

  /**
   * Add a coordinate to the frontier set.
   *
   * <p>A node is in the frontier set if it has been discovered but not yet processed.
   *
   * @param coord The coordinate to add.
   */
  protected void addFrontier(Coordinate coord) {
    if (lastParent != null && !cameFrom.containsKey(coord)) {
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
    return !frontierSet.isEmpty();
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
   * Returns the {@link com.badlogic.gdx.ai.pfa.GraphPath GraphPath} of the pathfinding search.
   *
   * @return A GraphPath object representing the path from start to end.
   */
  public GraphPath<Tile> graphPath() {
    GraphPath<Tile> path = new DefaultGraphPath<>();
    for (Coordinate pathStep : finalPath()) {
      path.add(Game.tileAT(pathStep));
    }
    return path;
  }

  /**
   * All taken steps in the pathfinding search.
   *
   * <p>This method returns a list of tuples containing the node and its state (OPEN, CLOSED, or
   * PATH).
   *
   * @return A list of tuples containing the node and its state.
   * @see PathfindingVisualizer
   * @see TileState
   */
  public List<Tuple<Coordinate, TileState>> steps() {
    return steps;
  }

  /** Perform the pathfinding search. */
  public abstract void performSearch();
}
