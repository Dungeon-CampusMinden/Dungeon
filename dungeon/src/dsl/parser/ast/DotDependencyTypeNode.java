package dsl.parser.ast;

import graph.taskdependencygraph.TaskEdge;

/** WTF? . */
public class DotDependencyTypeNode extends IdNode {
  private final TaskEdge.Type taskEdgeType;

  /**
   * WTF? .
   *
   * @param taskEdgeType foo
   * @param textValue foo
   */
  public DotDependencyTypeNode(TaskEdge.Type taskEdgeType, String textValue) {
    super(Type.DotDependencyType, textValue, SourceFileReference.NULL);
    this.taskEdgeType = taskEdgeType;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public TaskEdge.Type getTaskEdgeType() {
    return taskEdgeType;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
