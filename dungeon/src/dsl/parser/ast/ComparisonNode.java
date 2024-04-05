package dsl.parser.ast;

public class ComparisonNode extends BinaryNode {
  public enum ComparisonType {
    none,
    greaterThan,
    greaterEquals,
    lessThan,
    lessEquals
  }

  private final ComparisonType comparisonType;

  public ComparisonType getComparisonType() {
    return comparisonType;
  }

  public ComparisonNode(ComparisonType type, Node lhs, Node rhs) {
    super(Type.Comparison, lhs, rhs);
    this.comparisonType = type;
  }

  public ComparisonNode() {
    super(Type.Comparison);
    this.comparisonType = ComparisonType.none;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
