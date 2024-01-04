package dsl.parser.ast;

import java.util.ArrayList;

public class StmtBlockNode extends Node {
  public ArrayList<Node> getStmts() {
    return this.getChild(0).getChildren();
  }

  public StmtBlockNode(Node stmtList) {
    super(Type.Block);
    this.addChild(stmtList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
