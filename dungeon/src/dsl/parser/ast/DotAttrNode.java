package dsl.parser.ast;

/** WTF? . */
public class DotAttrNode extends BinaryNode {

  /**
   * WTF? .
   *
   * @param lhs foo
   * @param rhs foo
   */
  public DotAttrNode(Node lhs, Node rhs) {
    super(Type.DotAttr, lhs, rhs);
  }

  protected DotAttrNode(Type type, Node lhs, Node rhs) {
    super(type, lhs, rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getLhsIdName() {
    IdNode lhs = (IdNode) this.getLhs();
    return lhs.getName();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getRhsIdName() {
    IdNode rhs = (IdNode) this.getRhs();
    return rhs.getName();
  }
}
