package dsl.parser.ast;

public class LogicAndNode extends BinaryNode {

  public LogicAndNode(Node lhs, Node rhs) {
    super(Type.LogicAnd, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
