package dsl.parser.ast;

public class CountingLoopStmtNode extends ForLoopStmtNode {
  public final int counterIdIdx = 4;

  public Node getCounterIdNode() {
    return this.getChild(counterIdIdx);
  }

  public CountingLoopStmtNode(
      Node typeIdNode,
      Node varIdNode,
      Node iterableExpressionNode,
      Node counterIdNode,
      Node stmtNode) {
    super(LoopType.countingForLoop, typeIdNode, varIdNode, iterableExpressionNode, stmtNode);
    this.addChild(counterIdNode);
  }

  public CountingLoopStmtNode() {
    super(LoopType.countingForLoop);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
