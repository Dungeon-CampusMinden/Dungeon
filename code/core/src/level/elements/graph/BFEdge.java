package level.elements.graph;

/**
 * An edge, that creates a circle in the graph.
 *
 * @author Andre Matutat
 */
public class BFEdge {
    private final int node1;
    private final int node2;

    /**
     * @param node1 Index of the first node of the edge.
     * @param node2 Index of the second node of the edge.
     */
    public BFEdge(int node1, int node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public int getNode1() {
        return node1;
    }

    public int getNode2() {
        return node2;
    }

    public String toString() {
        return node1 + "->" + node2;
    }
}
