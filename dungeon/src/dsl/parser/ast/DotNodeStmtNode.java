package dsl.parser.ast;

/** WTF? . */
public class DotNodeStmtNode extends Node {
  /**
   * WTF? .
   *
   * @param id foo
   * @param attrList foo
   */
  public DotNodeStmtNode(Node id, Node attrList) {
    super(Type.DotNodeStmt);
    addChild(id);
    addChild(attrList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
