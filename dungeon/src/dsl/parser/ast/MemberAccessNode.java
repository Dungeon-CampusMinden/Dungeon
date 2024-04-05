package dsl.parser.ast;

public class MemberAccessNode extends BinaryNode {

  public MemberAccessNode(Node lhs, Node rhs) {
    super(Type.MemberAccess, lhs, rhs);
  }

  public MemberAccessNode() {
    super(Type.MemberAccess);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
