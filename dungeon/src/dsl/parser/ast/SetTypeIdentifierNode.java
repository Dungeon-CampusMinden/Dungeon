package dsl.parser.ast;

/** WTF? . */
public class SetTypeIdentifierNode extends IdNode {

  /**
   * Constructor. WTF? .
   *
   * @param innerTypeNode the inner type of the list type
   */
  public SetTypeIdentifierNode(IdNode innerTypeNode) {
    super(
        Type.SetTypeIdentifierNode,
        innerTypeNode.getName() + "<>",
        innerTypeNode.getSourceFileReference());
    this.addChild(innerTypeNode);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public IdNode getInnerTypeNode() {
    return (IdNode) this.getChild(0);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
