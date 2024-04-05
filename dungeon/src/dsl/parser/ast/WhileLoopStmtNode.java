package dsl.parser.ast;

public class WhileLoopStmtNode extends LoopStmtNode {
  public final int expressionIdx = 1;

  public Node getExpressionNode() {
    return this.getChild(expressionIdx);
  }

  public WhileLoopStmtNode(Node expressionNode, Node stmtNode) {
    super(LoopType.whileLoop, stmtNode);
    this.addChild(expressionNode);
  }

  public WhileLoopStmtNode() {
    super(LoopType.whileLoop);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
