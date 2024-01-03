package dsl.parser.ast;

public class LogicOrNode extends BinaryNode {
  public LogicOrNode(Node lhs, Node rhs) {
    super(Type.LogicOr, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
