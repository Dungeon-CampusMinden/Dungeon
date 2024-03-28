package dsl.parser.ast;

/** WTF? . */
public class ForLoopStmtNode extends LoopStmtNode {
  /** WTF? . */
  public final int typeIdIdx = 1;

  /** WTF? . */
  public final int varIdIdx = 2;

  /** WTF? . */
  public final int iterableIdx = 3;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getTypeIdNode() {
    return this.getChild(typeIdIdx);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getVarIdNode() {
    return this.getChild(varIdIdx);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getIterableIdNode() {
    return this.getChild(iterableIdx);
  }

  /**
   * WTF? .
   *
   * @param typeIdNode foo
   * @param varIdNode foo
   * @param iterabelExpressionNode foo
   * @param stmtNode foo
   */
  public ForLoopStmtNode(
      Node typeIdNode, Node varIdNode, Node iterabelExpressionNode, Node stmtNode) {
    super(LoopType.forLoop, stmtNode);
    addChildren(typeIdNode, varIdNode, iterabelExpressionNode);
  }

  protected ForLoopStmtNode(
      LoopType loopType,
      Node typeIdNode,
      Node varIdNode,
      Node iterableExpressionNode,
      Node stmtNode) {
    super(loopType, stmtNode);
    addChildren(typeIdNode, varIdNode, iterableExpressionNode);
  }

  private void addChildren(Node typeIdNode, Node varIdNode, Node iterableIdNode) {
    this.addChild(typeIdNode);
    this.addChild(varIdNode);
    this.addChild(iterableIdNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
