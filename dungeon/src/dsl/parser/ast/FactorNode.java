package dsl.parser.ast;

public class FactorNode extends BinaryNode {
  public enum FactorType {
    none,
    divide,
    multiply
  }

  private final FactorType factorType;

  public FactorType getFactorType() {
    return factorType;
  }

  public FactorNode(FactorType type, Node lhs, Node rhs) {
    super(Type.Factor, lhs, rhs);
    this.factorType = type;
  }

  public FactorNode() {
    super(Type.Factor);
    this.factorType = FactorType.none;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
