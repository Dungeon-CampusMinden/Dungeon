package dsl.parser.ast;

/** WTF? . */
public class EdgeOpNode extends Node {

  /**
   * WTF? .
   *
   * @return The {@link Type} of this EdgeOperator
   */
  public Type getEdgeOpType() {
    return edgeOpType;
  }

  /** WTF? . */
  public enum Type {
    /** WTF? . */
    NONE,
    /** WTF? . */
    arrow,
    /** WTF? . */
    doubleLine
  }

  private final Type edgeOpType;

  /**
   * Constructor. WTF? .
   *
   * @param sourceFileReference The sourceFileReference of the node corresponding the EdgeOperator.
   * @param edgeOpType The {@link Type} of the new EdgeOperator
   */
  public EdgeOpNode(SourceFileReference sourceFileReference, Type edgeOpType) {
    super(Node.Type.DotEdgeOp, sourceFileReference);
    this.edgeOpType = edgeOpType;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
