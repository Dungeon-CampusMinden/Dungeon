package contrib.level.generator.graphBased.levelGraph;

import core.Entity;
import core.utils.Tuple;

import java.util.*;

/**
 * A level graph that can be further processed into a room-based level.
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
 *
 * <p>The Dot representation of the graph can be obtained using {@link #toDot()}.
 *
 * <p>Use {@link #add(Set)} to add a new entity collection and thus a new node to the graph.
 */
public final class LevelGraph {
    private static final Random RANDOM = new Random();
    private final Set<LevelNode> nodes = new HashSet<>();
    private LevelNode root;

    /**
     * Creates a new node with the given set as payload and adds it to a random position in the
     * graph.
     *
     * @param set The entity collection to be placed as payload in the new node.
     * @return true if the connection was successful, false if not.
     */
    public boolean add(Set<Entity> set) {
        LevelNode node = new LevelNode(set, this);
        nodes.add(node);
        if (root == null) {
            root = node;
            return true;
        } else return add(node);
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
     * @return true if the connection was successful, false if not.
     */
    public boolean add(final LevelGraph other, final LevelGraph connectOn) {
        List<LevelNode> connectOnNodes =
                nodes.stream().filter(n -> n.originGraph() == connectOn).toList();
        // the graph is not connected with this graph
        if (connectOnNodes.isEmpty()) return false;

        List<LevelNode> otherNodes =
                other.nodes().stream().filter(n -> n.originGraph() == other).toList();
        if (otherNodes.isEmpty()) return false;

        // Nodes that have free Edges
        List<LevelNode> connectOnNodesFree =
                connectOnNodes.stream()
                        .filter(n -> n.neighboursCount() < Direction.values().length)
                        .toList();

        if (connectOnNodesFree.isEmpty()) {
            connectOn.createAdapterNode(connectOn, Direction.random());
            return add(other, connectOn);
        }
        List<LevelNode> otherNodesFree =
                otherNodes.stream()
                        .filter(n -> n.neighboursCount() < Direction.values().length)
                        .toList();
        if (otherNodesFree.isEmpty()) {
            other.createAdapterNode(other, Direction.random());
            return add(other, connectOn);
        }

        // try to find a matching Node-Pair (two Nodes that can be connected without any
        // changes)
        Optional<Tuple<LevelNode, LevelNode>> match = matching(connectOnNodesFree, otherNodesFree);
        if (match.isPresent()) {
            // there is an easy way to connect them, so do it
            Tuple<LevelNode, LevelNode> matchUnpacked = match.get();
            addNodesToNodeList(other.nodes());
            other.addNodesToNodeList(nodes());
            return matchUnpacked.a().connect(matchUnpacked.b());
        }

        // add adapter and try again
        other.createAdapterNode(other, Direction.random());
        return add(other, connectOn);
    }

    private LevelNode createAdapterNode(LevelGraph origin, Direction direction) {
        List<LevelNode> nodes =
                new ArrayList<>(
                        origin.nodes().stream().filter(n -> n.originGraph() == origin).toList());
        LevelNode adapter = new LevelNode(new HashSet<>(), origin);
        if (nodes.isEmpty()) {
            origin.add(adapter);
        } else {
            Collections.shuffle(nodes);
            LevelNode on = nodes.get(0);

            Optional<LevelNode> old = on.forceNeighbor(adapter, direction);
            adapter.forceNeighbor(on, Direction.opposite(direction));

            // readd possible edge
            old.ifPresent(
                    node -> {
                        node.forceNeighbor(adapter, Direction.opposite(direction));
                        adapter.forceNeighbor(node, direction);
                    });
        }
        origin.addNodesToNodeList(Set.of(adapter));
        return adapter;
    }

    private Optional<Tuple<LevelNode, LevelNode>> matching(
            List<LevelNode> connectOn, List<LevelNode> other) {
        for (LevelNode nodeA : connectOn) {
            List<Direction> freeDirections = nodeA.freeDirections();
            for (Direction direction : freeDirections) {
                for (LevelNode nodeB : other) {
                    if (nodeB.at(Direction.opposite(direction)).isEmpty())
                        return Optional.of(new Tuple<>(nodeA, nodeB));
                }
            }
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

            List<LevelNode> listA = new ArrayList<>(nodes().stream().toList());
            listA.removeIf(n -> n.neighboursCount() == Direction.values().length);
            List<LevelNode> listB = new ArrayList<>(listA);
            Collections.shuffle(listA);
            Collections.shuffle(listB);

            int connected = 0;
            for (LevelNode a : listA)
                for (LevelNode b : listB)
                    if (a != b && !a.isNeighbourWith(b) && a.connect(b)) {
                        connected++;
                        if (connected >= howManyExtraEdges) return;
                    }
        }
    }

    /**
     * Add all nodes from the given set to the node list of this graph.
     *
     * <p>This will also add the nodes to each graph that is connected with this graph.
     *
     * <p>This operation only adds nodes and does not establish any connections. Ensure that the
     * nodes are connected before calling this method.
     *
     * @param nodes Set of nodes to be added.
     */
    public void addNodesToNodeList(final Set<LevelNode> nodes) {
        addNodesToNodeList(nodes, new HashSet<>());
    }

    /**
     * Get the root node of this graph.
     *
     * @return the root node of this graph.
     */
    public LevelNode root() {
        return root;
    }

    /**
     * Get all nodes in the graph.
     *
     * @return copy of the set with all nodes in this graph.
     */
    public Set<LevelNode> nodes() {
        return new HashSet<>(nodes);
    }

    private boolean add(LevelNode node) {
        if (node.neighboursCount() == Direction.values().length) return false;
        List<LevelNode> shuffledNodes = new ArrayList<>(nodes().stream().toList());
        shuffledNodes.remove(node);
        Collections.shuffle(shuffledNodes);
        for (LevelNode n : shuffledNodes) {
            if (n.connect(node)) return true;
        }

        // could not create a connection because no node has a free edge where the other node has a
        // free edge
        createAdapterNode(this, Direction.opposite(node.freeDirections().get(0)));
        return add(node);
    }

    private void addNodesToNodeList(final Set<LevelNode> nodes, Set<LevelGraph> alreadyVisited) {
        if (nodes.isEmpty()) return;
        if (root == null) root = nodes.stream().findFirst().get();
        this.nodes.addAll(nodes);
        alreadyVisited.add(this);
        // collect each other node
        Set<LevelGraph> otherGraphs = new HashSet<>();
        for (LevelNode n : nodes) otherGraphs.add(n.originGraph());
        for (LevelGraph og : otherGraphs)
            if (!alreadyVisited.contains(og)) og.addNodesToNodeList(nodes, alreadyVisited);
    }

    /**
     * Creates a DOT representation of the graph.
     *
     * @return DOT representation of the graph as a string.
     */
    public String toDot() {
        List<LevelNode> nodeList = nodes.stream().toList();

        StringBuilder dotBuilder = new StringBuilder();

        dotBuilder.append("graph LevelGraph {\n");
        dotBuilder.append("    node [shape=box];\n");

        for (LevelNode node : nodeList) {
            int nodeId = nodeList.indexOf(node);
            dotBuilder.append("    node").append(nodeId).append(" [label=\"");
            for (Entity entity : node.entities()) {
                dotBuilder.append(entity.toString()).append("\\n");
            }
            dotBuilder.append("\"];\n");

            for (Direction direction : Direction.values()) {
                LevelNode neighbour = node.at(direction).orElse(null);
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
