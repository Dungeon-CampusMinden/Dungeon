package dsl.parser.ast;

public class AssignmentNode extends BinaryNode {
  public AssignmentNode(Node lhs, Node rhs) {
    super(Type.Assignment, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
