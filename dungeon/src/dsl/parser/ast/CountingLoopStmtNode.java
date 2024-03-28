package dsl.parser.ast;

/** WTF? . */
public class CountingLoopStmtNode extends ForLoopStmtNode {
  /** WTF? . */
  public final int counterIdIdx = 4;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getCounterIdNode() {
    return this.getChild(counterIdIdx);
  }

  /**
   * WTF? .
   *
   * @param typeIdNode foo
   * @param varIdNode foo
   * @param iterableExpressionNode foo
   * @param counterIdNode foo
   * @param stmtNode foo
   */
  public CountingLoopStmtNode(
      Node typeIdNode,
      Node varIdNode,
      Node iterableExpressionNode,
      Node counterIdNode,
      Node stmtNode) {
    super(LoopType.countingForLoop, typeIdNode, varIdNode, iterableExpressionNode, stmtNode);
    this.addChild(counterIdNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
