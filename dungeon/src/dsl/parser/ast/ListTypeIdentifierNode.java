package dsl.parser.ast;

public class ListTypeIdentifierNode extends IdNode {

  /**
   * Constructor
   *
   * @param innerTypeNode the inner type of the list type
   */
  public ListTypeIdentifierNode(IdNode innerTypeNode) {
    super(
        Type.ListTypeIdentifierNode,
        innerTypeNode.getName() + "[]",
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
