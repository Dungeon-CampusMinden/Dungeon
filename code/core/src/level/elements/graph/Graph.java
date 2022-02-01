package level.elements.graph;

import java.util.ArrayList;
import java.util.List;

/** @author Andre Matutat */
public class Graph {

    private static final int MAX_NODES = 4;
    private static final int MAX_NEIGHBOURS = 2;
    private final List<Node> nodes = new ArrayList<>();
    private final List<BFEdge> bfEdges = new ArrayList<>();

    /** Create a Graph with two connected nodes. */
    public Graph() {
        Node n1 = new Node(0);
        Node n2 = new Node(1);
        nodes.add(n1);
        nodes.add(n2);
        n1.connect(n2);
        n2.connect(n1);
    }

    /**
     * Copy the graph.
     *
     * @param graph The copy.
     */
    public Graph(Graph graph) {
        graph.getNodes().forEach(n -> nodes.add(new Node(n)));
        for (Node n : graph.getNodes()) {
            for (Integer nb : n.getNeighbours()) {
                Node n1 = nodes.get(n.getIndex());
                n1.connect(nodes.get(nb));
            }
        }
        for (BFEdge edge : graph.getBfEdges())
            bfEdges.add(new BFEdge(edge.getNode1(), edge.getNode2()));
    }

    /**
     * Try to connect an existing node with a new node.
     *
     * @param index Index of the node the new node should be connected with.
     * @return true If connection was successfully.
     */
    public boolean connectNewNode(int index) {
        Node n = nodes.get(index);
        if (canConnect(n)) {
            Node n2 = new Node(nodes.size());
            nodes.add(n2);
            n2.connect(n);
            n.connect(n2);
            return true;
        } else return false;
    }

    /**
     * Try to connect two existing nodes with another.
     *
     * @param index1 Index of the first node.
     * @param index2 Index of the second node.
     * @return true If connection was successfully.
     */
    public boolean connectNodes(int index1, int index2) {
        Node n1 = nodes.get(index1);
        Node n2 = nodes.get(index2);

        if (n1.notConnectedWith(n2) && canConnect(n1, n2)) {
            n1.connect(n2);
            n2.connect(n1);
            bfEdges.add(new BFEdge(index1, index2));
            return true;
        } else return false;
    }

    private boolean canConnect(Node node) {
        List<Node> manyNeighbour = new ArrayList<>(nodes);
        manyNeighbour.removeIf(n -> n.getNeighbours().size() <= MAX_NEIGHBOURS);
        return manyNeighbour.size() <= MAX_NODES
                || manyNeighbour.contains(node)
                || node.getNeighbours().size() + 1 <= MAX_NEIGHBOURS;
    }

    private boolean canConnect(Node node1, Node node2) {
        List<Node> manyNeighbour = new ArrayList<>(nodes);
        manyNeighbour.removeIf(node -> node.getNeighbours().size() <= MAX_NEIGHBOURS);
        boolean addN1 =
                (node1.getNeighbours().size() >= MAX_NEIGHBOURS && !manyNeighbour.contains(node1));
        boolean addN2 =
                (node2.getNeighbours().size() >= MAX_NEIGHBOURS && !manyNeighbour.contains(node2));

        return (manyNeighbour.size() + (addN1 ? 1 : 0) + (addN2 ? 1 : 0) < MAX_NODES);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    /** @return The graph in dot-notation. */
    public String toDot() {
        String dot = "digraph G {\nedge [dir=none]\n";
        for (Node n : nodes) dot += n.toDot();
        return dot + "}";
    }

    public List<BFEdge> getBfEdges() {
        return this.bfEdges;
    }
}
