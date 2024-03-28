package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class StmtBlockNode extends Node {
  /**
   * WTF? .
   *
   * @return foo
   */
  public ArrayList<Node> getStmts() {
    return this.getChild(0).getChildren();
  }

  /**
   * WTF? .
   *
   * @param stmtList foo
   */
  public StmtBlockNode(Node stmtList) {
    super(Type.Block);
    this.addChild(stmtList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
