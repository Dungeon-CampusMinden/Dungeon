package contrib.level.generator.graphBased.levelGraph;

import core.Entity;
import core.level.elements.ILevel;
import java.util.*;

/**
 * Node in the level graph.
 *
 * <p>Each node in the graph corresponds to a potential room in the level and holds a collection of
 * entities as payload, which can be queried using {@link LevelNode#entities()}. The stored entities
 * must be placed in the generated room during the processing.
 *
 * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
 * Direction} within the node. The {@link Direction} indicates the side of the room where the door
 * should be placed. The edges are oriented so that a west edge from node A to B corresponds to an
 * east edge from B to A.
 *
 * <p>Edges are unidirectional.
 *
 * <p>There is no separate data type for edges; instead, the {@link LevelNode}s store an array of
 * neighboring nodes, and the index in the array indicates the {@link Direction} through which the
 * nodes are connected.
 */
public class LevelNode {

  static final int MAX_NEIGHBOURS = Direction.values().length;

  /**
   * A collection of entities stored in this node. Intended to be used to add new entities after
   * initialization of the {@link LevelNode} class.
   */
  protected final Set<Entity> entities;

  private final LevelNode[] neighbours = new LevelNode[MAX_NEIGHBOURS];
  private final LevelGraph originGraph;
  private ILevel level;

  /**
   * Creates a new node with the given collection as payload.
   *
   * @param entities The entity collection stored in this node.
   * @param originGraph The graph in which this node was initially created and added. It helps to
   *     differentiate nodes in connected graphs.
   */
  public LevelNode(final Set<Entity> entities, final LevelGraph originGraph) {
    this.entities = entities;
    this.originGraph = originGraph;
  }

  /**
   * Creates a new node with an empty collection as payload.
   *
   * @param originGraph is the graph in which this node was initially created and added. It helps to
   *     differentiate nodes in connected graphs.
   */
  public LevelNode(final LevelGraph originGraph) {
    this(new HashSet<>(), originGraph);
  }

  /**
   * Adds a neighbor in a random direction if possible.
   *
   * <p>A connection is possible when both nodes have at least one available neighboring slot, and
   * the two slots are opposite each other. (For example, if in node A neighbor to the NORTH is
   * free, then in node B the neighbor to the SOUTH must be free).
   *
   * <p>If the origin graph of the given node is not the same as the origin graph of this node, the
   * graphs will be connected. All nodes from this origin graph will be added to the given node's
   * origin graph, and vice versa.
   *
   * <p>This method establishes the connection from this node to the other and vice versa.
   *
   * @param other The neighbor to be added.
   * @return true if the connection was successful, false if not.
   */
  public boolean connect(final LevelNode other) {
    List<Direction> freeDirections = possibleConnectDirections(other);
    if (!freeDirections.isEmpty()) {
      Collections.shuffle(freeDirections);
      if (other.connect(this, Direction.opposite(freeDirections.getFirst())))
        return connect(other, freeDirections.getFirst());
    }
    return false;
  }

  /**
   * Retrieves the neighbor at the given direction.
   *
   * @param direction The direction to check.
   * @return An Optional containing the neighbor node in the given direction, or empty if there is
   *     no neighbor in that direction.
   */
  public Optional<LevelNode> at(final Direction direction) {
    return Optional.ofNullable(neighbours[direction.value()]);
  }

  /**
   * Checks if this node and the given node are direct neighbors.
   *
   * @param node The node to check for neighbor relationship.
   * @return True if the nodes are neighbors, false if not.
   */
  public boolean isNeighbourWith(final LevelNode node) {
    for (LevelNode neighbour : neighbours) if (neighbour == node) return true;
    return false;
  }

  /**
   * Retrieves the entity collection of this node.
   *
   * @return The entity collection of this node.
   */
  public Set<Entity> entities() {
    return new HashSet<>(entities);
  }

  /**
   * Retrieves a copy of the neighbor node array.
   *
   * @return The neighbor node array.
   */
  public LevelNode[] neighbours() {
    LevelNode[] copy = new LevelNode[neighbours.length];
    java.lang.System.arraycopy(neighbours, 0, copy, 0, neighbours.length);
    return copy;
  }

  /**
   * Retrieves the number of neighbors of this node.
   *
   * @return The number of neighbours of this node.
   */
  public int neighboursCount() {
    return (int) Arrays.stream(neighbours).filter(Objects::nonNull).count();
  }

  /**
   * Set the level for this node.
   *
   * @param level The level/room that is represented by this node.
   */
  public void level(final ILevel level) {
    this.level = level;
  }

  /**
   * Get level/room that is represented by this node.
   *
   * @return level/room that is represented by this node.
   */
  public ILevel level() {
    return level;
  }

  /**
   * Get the origin graph of the node.
   *
   * <p>This is the graph in which this node was initially created and added. It helps to
   * differentiate nodes in connected graphs.
   *
   * @return The origin graph of the node.
   */
  public LevelGraph originGraph() {
    return originGraph;
  }

  /**
   * Adds a neighbor in the given direction.
   *
   * <p>If the origin graph of the given node is not the same graph as the origin graph of this
   * node, the graphs get connected. All nodes from this origin graph will be added to the given
   * node's origin graph, and vice versa.
   *
   * <p>This method only establishes the connection from this node to the other. Remember to also
   * call this function for the given node with the opposite direction to complete the connection.
   *
   * @param node The neighbor to be added.
   * @param direction The direction at which the neighbor should be added from this node's
   *     perspective (in the neighbor's context, this corresponds to the opposite direction).
   * @return true if the connection was successful, false if not.
   */
  public boolean connect(final LevelNode node, final Direction direction) {
    if (this == node || neighbours[direction.value()] != null) return false;
    neighbours[direction.value()] = node;
    // if a node of another graph gets added, all nodes of the other graph a now part of
    // this graph
    if (originGraph != node.originGraph())
      originGraph.addNodesToNodeList(node.originGraph().nodes());

    return true;
  }

  /**
   * Searches for all possible directions at which the given node can be connected to this node.
   *
   * <p>This method searches for all available directions in this node where the other node has
   * available directions at the opposite positions.
   *
   * @param other The node to be considered.
   * @return List of possible connection directions, starting from this node (when connecting, the
   *     opposite direction should be used for 'other').
   */
  private List<Direction> possibleConnectDirections(final LevelNode other) {
    List<Direction> freeDirections = freeDirections();
    List<Direction> otherDirections = other.freeDirections();
    otherDirections.replaceAll(Direction::opposite);
    freeDirections.retainAll(otherDirections);
    return freeDirections;
  }

  /**
   * Adds a neighbor in a random direction if possible.
   *
   * <p>A connection is possible when both nodes have at least one available neighboring slot, and
   * the two slots are opposite each other. (For example, if in node A neighbor to the NORTH is
   * free, then in node B the neighbor to the SOUTH must be free).
   *
   * <p>If the origin graph of the given node is not the same as the origin graph of this node, the
   * graphs will be connected. All nodes from this origin graph will be added to the given node's
   * origin graph, and vice versa.
   *
   * <p>This method establishes the connection from this node to the other and vice versa.
   *
   * @param node The neighbor to be added.
   * @param direction The direction at which the neighbor should be added from this node's
   *     perspective (in the neighbor's context, this corresponds to the opposite direction).
   * @return An Optional containing the old neighbor, if there was any.
   */
  Optional<LevelNode> forceNeighbor(final LevelNode node, final Direction direction) {
    LevelNode old = neighbours[direction.value()];
    neighbours[direction.value()] = node;
    if (old != null && old != node) old.forceNeighbor(null, Direction.opposite(direction));
    return Optional.ofNullable(old);
  }

  /**
   * Returns a list of all directions where this node does not have neighbors.
   *
   * @return List of directions without neighbors.
   */
  List<Direction> freeDirections() {
    List<Direction> freeDirections = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      int directionValue = direction.value();
      if (neighbours[directionValue] == null) {
        freeDirections.add(direction);
      }
    }
    return freeDirections;
  }

  /**
   * Retrieves a set of directions indicating neighboring nodes from other graphs.
   *
   * <p>This method iterates through all possible directions and checks if the neighbor in that
   * direction belongs to a different graph than the current node's graph. If a neighbor from
   * another graph is found in a particular direction, that direction is added to the result set.
   *
   * @return A {@code Set} of directions indicating neighboring nodes from other graphs.
   */
  public Set<Direction> whereNeighboursFromOtherGraphs() {
    Set<Direction> dirs = new HashSet<>();
    for (Direction direction : Direction.values()) {
      int directionValue = direction.value();
      if (neighbours[directionValue] != null
          && neighbours[directionValue].originGraph() != originGraph) {
        dirs.add(direction);
      }
    }
    return dirs;
  }
}
