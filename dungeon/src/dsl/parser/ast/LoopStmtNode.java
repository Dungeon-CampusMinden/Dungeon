package dsl.parser.ast;

/** WTF? . */
public abstract class LoopStmtNode extends Node {
  /** WTF? . */
  public final int stmtIdx = 0;

  /** WTF? . */
  public enum LoopType {
    /** WTF? . */
    whileLoop,
    /** WTF? . */
    forLoop,
    /** WTF? . */
    countingForLoop
  }

  private final LoopType loopType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public LoopType loopType() {
    return this.loopType;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getStmtNode() {
    return this.getChild(this.stmtIdx);
  }

  /**
   * WTF? .
   *
   * @param loopType foo
   * @param stmtNode foo
   */
  public LoopStmtNode(LoopType loopType, Node stmtNode) {
    super(Type.LoopStmtNode);
    this.loopType = loopType;

    addChild(stmtNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
