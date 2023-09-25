package parser.ast;

import taskdependencygraph.TaskEdge;

public class DotDependencyTypeNode extends IdNode {
    private final TaskEdge.Type taskEdgeType;

    public DotDependencyTypeNode(TaskEdge.Type taskEdgeType, String textValue) {
        super(Type.DotDependencyType, textValue, SourceFileReference.NULL);
        this.taskEdgeType = taskEdgeType;
    }

    public TaskEdge.Type getTaskEdgeType() {
        return taskEdgeType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
