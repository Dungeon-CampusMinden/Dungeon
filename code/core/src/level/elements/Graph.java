package level.elements;

import java.util.ArrayList;
import java.util.List;

/** @author Andre Matutat */
public class Graph {

    private final int MAX_NODES = 4;
    private final int MAX_NEIGHBOURS = 2;
    private List<Node> nodes = new ArrayList<>();

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
     * copy graph
     *
     * @param g
     */
    public Graph(Graph g) {
        g.getNodes().forEach(n -> nodes.add(new Node(n)));
        for (Node n : g.getNodes()) {
            for (Integer nb : n.getNeighbours()) {
                Node n1 = nodes.get(n.getIndex());
                n1.connect(nodes.get(nb));
            }
        }
    }

    /**
     * Try to connect a existing node with a new node
     *
     * @param index index of the node the new node should be connect with
     * @return true if connection was successfully
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
     * Try to connect two existing nodes with eachother
     *
     * @param index1 index of the first node
     * @param index2 index of the second node
     * @return true if connection was successfully
     */
    public boolean connectNodes(int index1, int index2) {
        Node n1 = nodes.get(index1);
        Node n2 = nodes.get(index2);

        if (n1.notConnectedWith(n2) && canConnect(n1, n2)) {
            n1.connect(n2);
            n2.connect(n1);
            return true;
        } else return false;
    }

    private boolean canConnect(Node n) {
        List<Node> manyNeighbour = new ArrayList<>(nodes);
        manyNeighbour.removeIf(node -> node.getNeighbours().size() <= MAX_NEIGHBOURS);
        if (manyNeighbour.size() <= MAX_NODES
                || manyNeighbour.contains(n)
                || n.getNeighbours().size() + 1 <= MAX_NEIGHBOURS) return true;
        else return false;
    }

    private boolean canConnect(Node n1, Node n2) {
        List<Node> manyNeighbour = new ArrayList<>(nodes);
        manyNeighbour.removeIf(node -> node.getNeighbours().size() <= MAX_NEIGHBOURS);
        boolean addN1 =
                (n1.getNeighbours().size() >= MAX_NEIGHBOURS && !manyNeighbour.contains(n1));
        boolean addN2 =
                (n2.getNeighbours().size() >= MAX_NEIGHBOURS && !manyNeighbour.contains(n2));

        return (manyNeighbour.size() + (addN1 ? 1 : 0) + (addN2 ? 1 : 0) < MAX_NODES);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public String toDot() {
        String dot = "digraph G {\nedge [dir=none]\n";
        for (Node n : nodes) dot += n.toDot();
        return dot + "}";
    }
}
