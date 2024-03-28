package dsl.parser.ast;

/** WTF? . */
public class WhileLoopStmtNode extends LoopStmtNode {
  /** WTF? . */
  public final int expressionIdx = 1;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getExpressionNode() {
    return this.getChild(expressionIdx);
  }

  /**
   * WTF? .
   *
   * @param expressionNode foo
   * @param stmtNode foo
   */
  public WhileLoopStmtNode(Node expressionNode, Node stmtNode) {
    super(LoopType.whileLoop, stmtNode);
    this.addChild(expressionNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
