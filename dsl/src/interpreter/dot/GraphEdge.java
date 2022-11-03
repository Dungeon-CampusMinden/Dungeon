package interpreter.dot;

public class GraphEdge {
    // TODO: create storage for attributes

    private final Type edgeType;

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
                startNode.getName(),
                startNode.hashCode(),
                separator,
                endNode.getName(),
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
        this.endNode = endNode;
    }
}
