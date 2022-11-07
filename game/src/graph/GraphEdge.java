package graph;

public class GraphEdge {
    private final Type edgeType;

    private final PropertyBag attributes = new PropertyBag();

    public PropertyBag getAttributes() {
        return attributes;
    }

    public Type getEdgeType() {
        return edgeType;
    }

    public GraphNode getStartNode() {
        return startNode;
    }

    public GraphNode getEndNode() {
        return endNode;
    }

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


    public GraphEdge(Type edgeType, GraphNode startNode, GraphNode endNode) {
        this.edgeType = edgeType;

        this.startNode = startNode;
        this.startNode.addEdge(this);

        this.endNode = endNode;
        this.endNode.addEdge(this);
    }

    public GraphEdge(GraphNode startNode, GraphNode endNode) {
        this.edgeType = Type.undirected;

        this.startNode = startNode;
        this.startNode.addEdge(this);

        this.endNode = endNode;
        this.endNode.addEdge(this);
    }
}
