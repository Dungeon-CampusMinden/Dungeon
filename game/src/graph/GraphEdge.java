package graph;

public class GraphEdge {
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
    public GraphNode getStartNode() {
        return startNode;
    }

    /**
     * @return the end node of this edge
     */
    public GraphNode getEndNode() {
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

    public enum Type {
        directed,
        undirected
    }

    private final GraphNode startNode;
    private final GraphNode endNode;

    /**
     * Constructor
     *
     * @param edgeType the type of the edge (directed or undirected)
     * @param startNode the node at the beginning of the edge
     * @param endNode the node at the end of the edge
     */
    public GraphEdge(Type edgeType, GraphNode startNode, GraphNode endNode) {
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
    public GraphEdge(GraphNode startNode, GraphNode endNode) {
        this.edgeType = Type.undirected;

        this.startNode = startNode;
        this.startNode.addEdge(this);

        this.endNode = endNode;
        this.endNode.addEdge(this);
    }
}
