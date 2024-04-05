package dsl.parser.ast;

public class DotAttrNode extends BinaryNode {

  public DotAttrNode(Node lhs, Node rhs) {
    super(Type.DotAttr, lhs, rhs);
  }

  protected DotAttrNode(Type type, Node lhs, Node rhs) {
    super(type, lhs, rhs);
  }

  protected DotAttrNode(Type type) {
    super(type);
  }

  public DotAttrNode() {
    super(Type.DotAttr);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }

  public String getLhsIdName() {
    IdNode lhs = (IdNode) this.getLhs();
    return lhs.getName();
  }

  public String getRhsIdName() {
    IdNode rhs = (IdNode) this.getRhs();
    return rhs.getName();
  }
}
