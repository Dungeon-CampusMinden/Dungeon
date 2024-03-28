package dsl.parser.ast;

/** WTF? .WTF? . */
public class LogicOrNode extends BinaryNode {
  /**
   * WTF? .
   *
   * @param lhs foo
   * @param rhs foo
   */
  public LogicOrNode(Node lhs, Node rhs) {
    super(Type.LogicOr, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
