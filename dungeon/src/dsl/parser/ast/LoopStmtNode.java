package dsl.parser.ast;

public abstract class LoopStmtNode extends Node {
  public final int stmtIdx = 0;

  public enum LoopType {
    whileLoop,
    forLoop,
    countingForLoop
  }

  private final LoopType loopType;

  public LoopType loopType() {
    return this.loopType;
  }

  public Node getStmtNode() {
    return this.getChild(this.stmtIdx);
  }

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
