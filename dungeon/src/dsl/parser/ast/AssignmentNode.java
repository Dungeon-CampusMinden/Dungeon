package dsl.parser.ast;

/** WTF? . */
public class AssignmentNode extends BinaryNode {
  /**
   * WTF? .
   *
   * @param lhs foo
   * @param rhs foo
   */
  public AssignmentNode(Node lhs, Node rhs) {
    super(Type.Assignment, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
