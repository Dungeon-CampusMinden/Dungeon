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
 * <p>This class maintains the open and closed sets, tracks the path, and provides methods to
 * initialize the search, perform steps, and retrieve the final path.
 *
 * <p>It is designed to be extended by specific pathfinding algorithms, such as BFS or DFS.
 *
 * <p>It uses a deque for the open set, allowing for both FIFO and LIFO behavior based on the
 * constructor parameter.
 *
 * @see PathfindingVisualizer
 * @see Node
 * @see systems.PathfindingSystem PathfindingSystem
 */
public abstract class PathfindingLogic {
  /** The starting coordinate for the pathfinding search. */
  protected final Coordinate startNode;

  /** The ending coordinate for the pathfinding search. */
  protected final Coordinate endNode;

  private final Deque<Node> openSet = new ArrayDeque<>();
  private final boolean fifo;
  private final Set<Coordinate> closedSet = new HashSet<>();
  private final List<Tuple<Node, TileState>> steps = new ArrayList<>();
  private final Map<Coordinate, Coordinate> cameFrom = new HashMap<>();
  private Coordinate lastParent = null;

  /**
   * Constructor for PathfindingLogic.
   *
   * @param startNode The starting coordinate for the pathfinding search.
   * @param endNode The ending coordinate for the pathfinding search.
   * @param fifo True if the open set should be treated as a FIFO queue, false for LIFO.
   */
  protected PathfindingLogic(Coordinate startNode, Coordinate endNode, boolean fifo) {
    this.startNode = startNode;
    this.endNode = endNode;
    this.fifo = fifo;
  }

  /**
   * Check if the coordinate is in the open set.
   *
   * <p>A node is in the open set if it has been added to the open set but not yet processed.
   *
   * @param coord The coordinate to check.
   */
  protected void addToOpenSet(Coordinate coord) {
    if (lastParent != null && !cameFrom.containsKey(coord)) {
      cameFrom.put(coord, lastParent);
    }
    Node node = new Node(coord);
    steps.add(Tuple.of(node, TileState.OPEN));
    if (fifo) openSet.addLast(node);
    else openSet.push(node);
  }

  /**
   * Poll the next node from the open set.
   *
   * <p>This method retrieves and removes the next node to be processed from the open set.
   *
   * @return The next node to be processed. Returns null if the open set is empty.
   */
  protected Node pollNextNode() {
    Node node = fifo ? openSet.pollFirst() : openSet.pop();
    steps.add(Tuple.of(node, TileState.CURRENT));
    lastParent = node != null ? node.coordinate() : null;
    return node;
  }

  /**
   * Check if the open set is empty.
   *
   * @return True if the open set is empty, false otherwise.
   */
  protected boolean hasOpenNodes() {
    return !openSet.isEmpty();
  }

  /**
   * Check if the coordinate is in the closed set.
   *
   * <p>A node is in the closed set if it has been processed and marked as visited.
   *
   * @param coord The coordinate to check.
   */
  protected void addToClosedSet(Coordinate coord) {
    // record the step, then mark this coord visited
    Node node = new Node(coord);
    steps.add(Tuple.of(node, TileState.CLOSED));
    closedSet.add(coord);
  }

  /**
   * Check if the coordinate is in the open set.
   *
   * <p>A node is in the open set if it has been added to the open set but not yet processed.
   *
   * @param coord The coordinate to check.
   * @return True if the coordinate is in the open set, false otherwise.
   */
  protected boolean isClosed(Coordinate coord) {
    return closedSet.contains(coord);
  }

  /**
   * Build the final path from the end coordinate to the start coordinate.
   *
   * @return A list of nodes representing the final path from start to end.
   */
  public List<Node> finalPath() {
    List<Node> path = new ArrayList<>();
    Coordinate curr = this.endNode;
    while (curr != null) {
      path.add(new Node(curr));
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
    for (Node node : finalPath()) {
      path.add(Game.tileAT(node.coordinate()));
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
   */
  public List<Tuple<Node, TileState>> steps() {
    return steps;
  }

  /** Perform the pathfinding search. */
  public abstract void performSearch();
}
