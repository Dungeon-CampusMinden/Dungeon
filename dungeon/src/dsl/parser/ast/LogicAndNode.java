package dsl.parser.ast;

/** WTF? . */
public class LogicAndNode extends BinaryNode {

  /**
   * WTF? .
   *
   * @param lhs foo
   * @param rhs foo
   */
  public LogicAndNode(Node lhs, Node rhs) {
    super(Type.LogicAnd, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
