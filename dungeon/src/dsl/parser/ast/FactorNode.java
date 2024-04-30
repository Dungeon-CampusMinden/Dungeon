package dsl.parser.ast;

/** WTF? . */
public class FactorNode extends BinaryNode {
  /** WTF? . */
  public enum FactorType {
    /** WTF? . */
    divide,
    /** WTF? . */
    multiply
  }

  private final FactorType factorType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public FactorType getFactorType() {
    return factorType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param lhs foo
   * @param rhs foo
   */
  public FactorNode(FactorType type, Node lhs, Node rhs) {
    super(Type.Factor, lhs, rhs);
    this.factorType = type;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
