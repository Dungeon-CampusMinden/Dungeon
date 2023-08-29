package core.level.generator.graphBased.levelGraph;

import core.Entity;
import core.utils.Tuple;

import java.util.*;

/**
 * A level graph that can be further processed into a room-based level.
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
 *
 * <p>The Dot representation of the graph can be obtained using {@link #toDot()}.
 *
 * <p>Use {@link #add(Set)} to add a new entity collection and thus a new node to the graph.
 */
public final class LevelGraph {
    private static final Random RANDOM = new Random();
    private final List<Node> nodes = new ArrayList<>();
    private Node root;

    /**
     * Creates a new node with the given set as payload and adds it to a random position in the
     * graph.
     *
     * @param set The entity collection to be placed as payload in the new node.
     * @return A Tuple containing the node in the graph where the given node was connected and the
     *     direction of the connection.
     */
    public Optional<Tuple<Node, Direction>> add(Set<Entity> set) {
        Node node = new Node(set, this);
        nodes.add(node);
        if (root == null) {
            root = node;
            return Optional.empty();
        } else return add(node);
    }

    public Optional<Tuple<Node, Direction>> add(Node node) {
        Collections.shuffle(nodes);
        for (Node n : nodes) {
            Optional<Tuple<Node, Direction>> tup = n.add(node);
            if (tup.isPresent()) return tup;
        }
        return Optional.empty();
    }

    public void addRandomEdges(int range) {
        // for two nodes no extra edges are needed
        if (nodes.size() >= 3) {
            int howManyExtraEdges = RANDOM.nextInt(nodes.size() / range, nodes.size());

            for (int i = 0; i < howManyExtraEdges; i++) {
                Node a = random();
                Node b = random();
                if (a == b || a.isNeighbourWith(b)) i = i - 1;
                else {
                    a.add(b);
                }
            }
        }
    }

    /**
     * Retrieves a random node from the graph.
     *
     * @return A randomly selected node from the graph.
     */
    public Node random() {
        return nodes.get(RANDOM.nextInt(nodes.size() - 1));
    }

    public Node root() {
        return root;
    }

    public List<Node> nodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Adds the provided level graph to this level graph.
     *
     * <p>This function searches for a free edge within this level graph and then connects the
     * provided level graph to it.
     *
     * <p>Note: This operation modifies both graphs and merges them into one. Both graphs (this and
     * the other) will be structurally identical.
     *
     * @param other The level graph to be connected to this graph.
     * @param connectOn A new edge will only be created with a node in this graph whose origin graph
     *     is the given one.
     * @return A tuple containing the node in this graph and the direction in which the given graph
     *     was connected.
     */
    public Optional<Tuple<Node, Direction>> add(
            final LevelGraph other, final LevelGraph connectOn) {

        List<Node> nodesWhereCanBeAdded =
                new ArrayList<>(nodes.stream().filter(n -> n.originGraph() == connectOn).toList());
        Collections.shuffle(nodesWhereCanBeAdded);

        for (Node node : nodesWhereCanBeAdded) {
            for (Node otherNode : other.nodes()) {
                // todo this can end in an endless loop
                Optional<Tuple<Node, Direction>> tup = node.add(otherNode);
                if (tup.isPresent()) {
                    nodes.addAll(other.nodes());
                    return tup;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Add all nodes from the given list to the node list of this graph.
     *
     * <p>This operation only adds nodes and does not establish any connections. Ensure that the
     * nodes are connected before calling this method.
     *
     * @param other List of nodes to be added.
     */
    public void addNodes(final List<Node> other) {
        nodes.addAll(other);
    }

    /**
     * Creates a DOT representation of the graph.
     *
     * @return DOT representation of the graph as a string.
     */
    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();

        dotBuilder.append("graph LevelGraph {\n");
        dotBuilder.append("    node [shape=box];\n");

        for (Node node : nodes) {
            int nodeId = nodes.indexOf(node);
            dotBuilder.append("    node").append(nodeId).append(" [label=\"");
            for (Entity entity : node.entities()) {
                dotBuilder.append(entity.toString()).append("\\n");
            }
            dotBuilder.append("\"];\n");

            for (Direction direction : Direction.values()) {
                Node neighbour = node.at(direction).orElse(null);
                if (neighbour != null && nodes.indexOf(neighbour) > nodeId) {
                    int neighbourId = nodes.indexOf(neighbour);
                    dotBuilder
                            .append("    node")
                            .append(nodeId)
                            .append(" -- node")
                            .append(neighbourId);
                    dotBuilder.append(" [dir=").append(direction).append("];\n");
                }
            }
        }

        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }
}
