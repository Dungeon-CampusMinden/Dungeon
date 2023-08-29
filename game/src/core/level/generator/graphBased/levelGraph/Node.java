package core.level.generator.graphBased.levelGraph;

import core.Entity;
import core.level.elements.ILevel;
import core.utils.Tuple;

import java.util.*;

/**
 * Node in the level graph.
 *
 * <p>Each node in the graph corresponds to a potential room in the level and holds a collection of
 * entities as payload, which can be queried using {@link Node#entities()}. The stored entities must
 * be placed in the generated room during the processing.
 *
 * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
 * Direction} within the node. The {@link Direction} indicates the side of the room where the door
 * should be placed. The edges are oriented so that a west edge from node A to B corresponds to an
 * east edge from B to A.
 *
 * <p>Edges are unidirectional.
 *
 * <p>There is no separate data type for edges; instead, the {@link Node}s store an array of
 * neighboring nodes, and the index in the array indicates the {@link Direction} through which the
 * nodes are connected.
 */
public final class Node {
    private final Set<Entity> entities;
    private final Node[] neighbours = new Node[Direction.values().length];
    private final LevelGraph originGraph;
    private ILevel level;

    /**
     * Creates a new node with the given collection as payload.
     *
     * @param entities The entity collection stored in this node.
     * @param originGraph is the graph in which this node was initially created and added. It helps
     *     to differentiate nodes in connected graphs.
     */
    public Node(final Set<Entity> entities, LevelGraph originGraph) {
        this.entities = entities;
        this.originGraph = originGraph;
    }

    /**
     * Adds a neighbor in the given direction.
     *
     * <p>If the origin graph of the given node is not the same graph as the origin graph of this
     * node, the graphs get connected. All nodes from this origin graph will be added to the given
     * node's origin graph, and vice versa.
     *
     * <p>This method only establishes the connection from this node to the other. Remember to also
     * call {@link #add(Node, Direction)} for the given node with the opposite direction to complete
     * the connection.
     *
     * @param node The neighbor to be added.
     * @param direction The direction at which the neighbor should be added from this node's
     *     perspective (in the neighbor's context, this corresponds to the opposite direction).
     * @return A Tuple containing the node in the graph where the given node was connected, and the
     *     direction of the connection. Returns an empty result if the nodes could not be connected.
     */
    private Optional<Tuple<Node, Direction>> add(final Node node, final Direction direction) {
        if (neighbours[direction.value()] != null) return Optional.empty();
        neighbours[direction.value()] = node;
        // if a node of a other graph gets added, all nodes of the other graph a now part of
        // this graph
        if (originGraph != node.originGraph()) originGraph.addNodes(node.originGraph().nodes());

        return Optional.of(new Tuple<>(node, direction));
    }

    /**
     * Adds a neighbor in a random direction.
     *
     * <p>If the origin graph of the given node is not the same graph as the origin graph of this
     * node, the graphs get connected. All nodes from this origin graph will be added to the given
     * node's origin graph, and vice versa.
     *
     * <p>This method establishes the connection from this node to the other, and vice versa.
     *
     * @param other The neighbor to be added.
     * @return A Tuple containing the node in the graph where the given node was connected, and the
     *     direction of the connection. Returns an empty result if the nodes could not be connected.
     */
    public Optional<Tuple<Node, Direction>> add(final Node other) {
        List<Direction> freeDirections = possibleConnectDirections(other);
        if (freeDirections.size() == 0) return Optional.empty();
        else {
            Collections.shuffle(freeDirections);
            if (other.add(this, Direction.opposite(freeDirections.get(0))).isPresent())
                return add(other, freeDirections.get(0));
        }
        return Optional.empty();
    }

    private List<Direction> possibleConnectDirections(final Node other) {
        List<Direction> freeDirections = freeDirections();
        List<Direction> otherDirections = other.freeDirections();
        otherDirections.replaceAll(Direction::opposite);
        freeDirections.retainAll(otherDirections);
        return freeDirections;
    }

    private List<Direction> freeDirections() {
        List<Direction> freeDirections = new ArrayList<>();
        if (neighbours[Direction.NORTH.value()] == null) freeDirections.add(Direction.NORTH);
        if (neighbours[Direction.EAST.value()] == null) freeDirections.add(Direction.EAST);
        if (neighbours[Direction.SOUTH.value()] == null) freeDirections.add(Direction.SOUTH);
        if (neighbours[Direction.WEST.value()] == null) freeDirections.add(Direction.WEST);
        return freeDirections;
    }

    /**
     * Retrieves the neighbor in the given direction.
     *
     * @param direction The direction to check.
     * @return An Optional containing the neighbor node in the given direction, or empty if there is
     *     no neighbor in that direction.
     */
    public Optional<Node> at(Direction direction) {
        return Optional.ofNullable(neighbours[direction.value()]);
    }

    /**
     * Checks if this node and the given node are direct neighbors.
     *
     * @param node The node to check for neighbor relationship.
     * @return True if the nodes are neighbors, false if not.
     */
    public boolean isNeighbourWith(Node node) {
        for (Node neighbour : neighbours) if (neighbour == node) return true;
        return false;
    }

    /**
     * Retrieves the entity collection of this node.
     *
     * @return The entity collection of this node.
     */
    public Set<Entity> entities() {
        return entities;
    }

    /**
     * Retrieves a copy of the neighbor node array.
     *
     * @return The neighbor node array.
     */
    public Node[] neighbours() {
        Node[] copy = new Node[neighbours.length];
        java.lang.System.arraycopy(neighbours, 0, copy, 0, neighbours.length);
        return copy;
    }

    /**
     * @return the number of neighbours of this node.
     */
    public int neighboursCount() {
        return (int) Arrays.stream(neighbours).filter(Objects::nonNull).count();
    }

    /**
     * Set the level for this node.
     *
     * @param level The level/room that is represented by this node.
     */
    public void level(ILevel level) {
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
}
