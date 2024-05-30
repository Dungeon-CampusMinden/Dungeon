package dsl.parser.ast;

public class DotNodeStmtNode extends Node {
  public static int idIdx = 0;
  public static int attrListIdx = 1;

  public DotNodeStmtNode(Node id, Node attrList) {
    super(Type.DotNodeStmt);
    addChild(id);
    addChild(attrList);
  }

  public DotNodeStmtNode() {
    super(Type.DotNodeStmt);
  }

  public IdNode getIdentifier() {
    return (IdNode) this.getChild(idIdx);
  }

  public Node getAttrList() {
    return this.getChild(attrListIdx);
  }

  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
