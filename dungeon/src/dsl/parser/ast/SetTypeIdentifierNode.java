package dsl.parser.ast;

public class SetTypeIdentifierNode extends IdNode {

  /**
   * Constructor
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

  public IdNode getInnerTypeNode() {
    return (IdNode) this.getChild(0);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
