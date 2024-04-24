package dsl.parser.ast;

/** WTF? . */
public class ComparisonNode extends BinaryNode {
  /** WTF? . */
  public enum ComparisonType {
    /** WTF? . */
    greaterThan,
    /** WTF? . */
    greaterEquals,
    /** WTF? . */
    lessThan,
    /** WTF? . */
    lessEquals
  }

  private final ComparisonType comparisonType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public ComparisonType getComparisonType() {
    return comparisonType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param lhs foo
   * @param rhs foo
   */
  public ComparisonNode(ComparisonType type, Node lhs, Node rhs) {
    super(Type.Comparison, lhs, rhs);
    this.comparisonType = type;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
