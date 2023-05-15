package dslToGame.graph;

public class Edge implements Comparable<Edge> {
    private static int _idx;

    private final int idx;

    /**
     * @return The unique index of this node
     */
    public int getIdx() {
        return idx;
    }

    private final Type edgeType;

    // dot allows for the definition of attributes for each edge, these will be stored in this
    // PropertyBag
    private final PropertyBag attributes = new PropertyBag();

    /**
     * @return the attributes associated with this edge
     */
    public PropertyBag getAttributes() {
        return attributes;
    }

    /**
     * @return the {@link Type} of this edge
     */
    public Type getEdgeType() {
        return edgeType;
    }

    /**
     * @return the start node of this edge (on it's beginning)
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * @return the end node of this edge
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * @return a formatted name for this edge, featuring the values and hashCodes for each node (for
     *     identification) and marker for the edgeType
     */
    public String getName() {
        String separator = edgeType.equals(Type.directed) ? "->" : "--";
        return String.format(
                "%1$s[%2$s] %3$s %4$s[%5$s]",
                startNode.getValue(),
                startNode.hashCode(),
                separator,
                endNode.getValue(),
                endNode.hashCode());
    }

    @Override
    public int compareTo(Edge o) {
        return this.idx - o.idx;
    }

    public enum Type {
        directed,
        undirected
    }

    private final Node startNode;
    private final Node endNode;

    /**
     * Constructor
     *
     * @param edgeType the type of the edge (directed or undirected)
     * @param startNode the node at the beginning of the edge
     * @param endNode the node at the end of the edge
     */
    public Edge(Type edgeType, Node startNode, Node endNode) {
        this.idx = _idx++;

        this.edgeType = edgeType;

        this.startNode = startNode;
        this.startNode.addEdge(this);

        this.endNode = endNode;
        this.endNode.addEdge(this);
    }

    /**
     * Constructor, the edge type will be set to undirected
     *
     * @param startNode the node at the beginning of the edge
     * @param endNode the node at the end of the edge
     */
    public Edge(Node startNode, Node endNode) {
        this.idx = _idx++;

        this.edgeType = Type.undirected;

        this.startNode = startNode;
        this.startNode.addEdge(this);

        this.endNode = endNode;
        this.endNode.addEdge(this);
    }
}
