package dsl.parser.ast;

public class DotNodeStmtNode extends Node {
  public DotNodeStmtNode(Node id, Node attrList) {
    super(Type.DotNodeStmt);
    addChild(id);
    addChild(attrList);
  }

  public DotNodeStmtNode() {
    super(Type.DotNodeStmt);
  }

  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
