package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class StmtBlockNode extends Node {
  public ArrayList<Node> getStmts() {
    return this.getChildren();
  }

  public StmtBlockNode(List<Node> stmts) {
    super(Type.Block);
    this.addChildren(stmts);
  }

  public StmtBlockNode() {
    super(Type.Block);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
