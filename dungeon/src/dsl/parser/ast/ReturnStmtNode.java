package dsl.parser.ast;

/** WTF? . */
public class ReturnStmtNode extends Node {
  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getInnerStmtNode() {
    return getChild(0);
  }

  /**
   * WTF? .
   *
   * @param innerStmt foo
   */
  public ReturnStmtNode(Node innerStmt) {
    super(Type.ReturnStmt);
    this.addChild(innerStmt);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
