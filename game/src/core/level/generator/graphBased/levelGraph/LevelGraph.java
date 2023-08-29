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
    private final Set<Node> nodes = new HashSet<>();
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
        List<Node> shuffledNodes = new ArrayList<>(nodes().stream().toList());
        Collections.shuffle(shuffledNodes);
        for (Node n : shuffledNodes) {
            Optional<Tuple<Node, Direction>> tup = n.add(node);
            if (tup.isPresent()) return tup;
        }
        return Optional.empty();
    }

    /**
     * Adds new edges between random nodes in the graph.
     *
     * <p>If the graph has only two nodes, no additional edges can be added.
     *
     * @param divider Specifies the range for determining the number of extra edges to be added. The
     *     number will be randomly selected between the number of nodes in the graph divided by the
     *     divider and the total number of nodes in the graph.
     */
    public void addRandomEdges(int divider) {
        // for two nodes no extra edges are needed
        if (nodes.size() >= 3) {
            int howManyExtraEdges = RANDOM.nextInt(nodes.size() / divider, nodes.size());

            // Consider only nodes that still have space for another neighbor.
            // Examine every possible combination (no random selection as it could lead to potential
            // infinite loops).

            List<Node> listA = new ArrayList<>(nodes().stream().toList());
            listA.removeIf(n -> n.neighboursCount() == Direction.values().length);
            List<Node> listB = new ArrayList<>(listA);
            Collections.shuffle(listA);
            Collections.shuffle(listB);

            int connected = 0;
            for (Node a : listA)
                for (Node b : listB)
                    if (a != b && !a.isNeighbourWith(b) && a.add(b).isPresent()) {
                        connected++;
                        if (connected >= howManyExtraEdges) return;
                    }
        }
    }

    /**
     * Get the root node of this graph.
     *
     * @return the root node of this graph.
     */
    public Node root() {
        return root;
    }

    /**
     * Get all nodes in the graph.
     *
     * @return copy of the set with all nodes in this graph.
     */
    public Set<Node> nodes() {
        return new HashSet<>(nodes);
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
                Optional<Tuple<Node, Direction>> tup = node.add(otherNode);
                if (tup.isPresent()) return tup;
            }
        }
        return Optional.empty();
    }

    /**
     * Add all nodes from the given set to the node list of this graph.
     *
     * <p>This operation only adds nodes and does not establish any connections. Ensure that the
     * nodes are connected before calling this method.
     *
     * @param other Set of nodes to be added.
     */
    public void addNodes(final Set<Node> other) {
        nodes.addAll(other);
    }

    /**
     * Creates a DOT representation of the graph.
     *
     * @return DOT representation of the graph as a string.
     */
    public String toDot() {
        List<Node> nodeList = nodes.stream().toList();

        StringBuilder dotBuilder = new StringBuilder();

        dotBuilder.append("graph LevelGraph {\n");
        dotBuilder.append("    node [shape=box];\n");

        for (Node node : nodeList) {
            int nodeId = nodeList.indexOf(node);
            dotBuilder.append("    node").append(nodeId).append(" [label=\"");
            for (Entity entity : node.entities()) {
                dotBuilder.append(entity.toString()).append("\\n");
            }
            dotBuilder.append("\"];\n");

            for (Direction direction : Direction.values()) {
                Node neighbour = node.at(direction).orElse(null);
                if (neighbour != null && nodeList.indexOf(neighbour) > nodeId) {
                    int neighbourId = nodeList.indexOf(neighbour);
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
