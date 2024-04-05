package dsl.parser.ast;

import graph.taskdependencygraph.TaskEdge;

public class DotDependencyTypeNode extends IdNode {
  private final TaskEdge.Type taskEdgeType;

  public DotDependencyTypeNode(TaskEdge.Type taskEdgeType, String textValue) {
    super(Type.DotDependencyType, textValue, SourceFileReference.NULL);
    this.taskEdgeType = taskEdgeType;
  }

  public DotDependencyTypeNode() {
    super(Type.DotDependencyType, "", SourceFileReference.NULL);
    this.taskEdgeType = TaskEdge.Type.none;
  }

  public TaskEdge.Type getTaskEdgeType() {
    return taskEdgeType;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
