package dsl.parser.ast;

/** WTF? . */
public class EqualityNode extends BinaryNode {
  /** WTF? . */
  public enum EqualityType {
    /** WTF? . */
    equals,
    /** WTF? . */
    notEquals
  }

  private final EqualityType equalityType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public EqualityType getEqualityType() {
    return equalityType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param lhs foo
   * @param rhs foo
   */
  public EqualityNode(EqualityType type, Node lhs, Node rhs) {
    super(Type.Equality, lhs, rhs);
    this.equalityType = type;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
