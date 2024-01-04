package dsl.parser.ast;

public class EqualityNode extends BinaryNode {
  public enum EqualityType {
    equals,
    notEquals
  }

  private final EqualityType equalityType;

  public EqualityType getEqualityType() {
    return equalityType;
  }

  public EqualityNode(EqualityType type, Node lhs, Node rhs) {
    super(Type.Equality, lhs, rhs);
    this.equalityType = type;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
