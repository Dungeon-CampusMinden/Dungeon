package dsl.parser.ast;

import graph.taskdependencygraph.TaskEdge;

public class DotDependencyTypeAttrNode extends DotAttrNode {
  public DotDependencyTypeAttrNode(DotDependencyTypeNode dependencyTypeNode) {
    super(
        Type.DotDependencyTypeAttr,
        new IdNode("type", SourceFileReference.NULL),
        dependencyTypeNode);
  }

  public TaskEdge.Type getDependencyType() {
    return ((DotDependencyTypeNode) this.getRhs()).getTaskEdgeType();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
