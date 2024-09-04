package newdsl.graph;

public class TaskEdge {

    private final EdgeType edgeType;
    private final TaskNode startNode;
    private final TaskNode endNode;

    public TaskEdge(EdgeType edgeType, TaskNode startNode, TaskNode endNode) {
        this.edgeType = edgeType;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public TaskNode getStartNode() {
        return startNode;
    }

    public TaskNode getEndNode() {
        return endNode;
    }

    public enum EdgeType {
        subtask_mandatory, //
        subtask_optional, //
        sequence, //
        conditional_false, //
        conditional_correct //
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", this.startNode.getTask().getId(), this.edgeType.toString(), this.endNode.getTask().getId());
    }
}
