package dsl.parser.ast;

/** WTF? . */
public class MemberAccessNode extends BinaryNode {

  /**
   * WTF? .
   *
   * @param lhs foo
   * @param rhs foo
   */
  public MemberAccessNode(Node lhs, Node rhs) {
    super(Type.MemberAccess, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
