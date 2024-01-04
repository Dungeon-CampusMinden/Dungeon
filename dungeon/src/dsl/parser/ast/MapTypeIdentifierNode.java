package dsl.parser.ast;

public class MapTypeIdentifierNode extends IdNode {

  /** Constructor */
  public MapTypeIdentifierNode(IdNode keyTypeNode, IdNode elementTypeNode) {
    super(
        Type.MapTypeIdentifierNode,
        "[" + keyTypeNode.getName() + "->" + elementTypeNode.getName() + "]",
        keyTypeNode.getSourceFileReference());
    this.addChild(keyTypeNode);
    this.addChild(elementTypeNode);
  }

  public IdNode getKeyTypeNode() {
    return (IdNode) this.getChild(0);
  }

  public IdNode getElementTypeNode() {
    return (IdNode) this.getChild(1);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
