package dsl.parser.ast;

import graph.taskdependencygraph.TaskEdge;

/** WTF? . */
public class DotDependencyTypeAttrNode extends DotAttrNode {
  /**
   * WTF? .
   *
   * @param dependencyTypeNode foo
   */
  public DotDependencyTypeAttrNode(DotDependencyTypeNode dependencyTypeNode) {
    super(
        Type.DotDependencyTypeAttr,
        new IdNode("type", SourceFileReference.NULL),
        dependencyTypeNode);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public TaskEdge.Type getDependencyType() {
    return ((DotDependencyTypeNode) this.getRhs()).getTaskEdgeType();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
