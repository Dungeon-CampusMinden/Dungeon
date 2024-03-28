package dsl.parser.ast;

/** WTF? . */
public class MapTypeIdentifierNode extends IdNode {

  /**
   * Constructor. WTF? .
   *
   * @param keyTypeNode foo
   * @param elementTypeNode foo
   */
  public MapTypeIdentifierNode(IdNode keyTypeNode, IdNode elementTypeNode) {
    super(
        Type.MapTypeIdentifierNode,
        "[" + keyTypeNode.getName() + "->" + elementTypeNode.getName() + "]",
        keyTypeNode.getSourceFileReference());
    this.addChild(keyTypeNode);
    this.addChild(elementTypeNode);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public IdNode getKeyTypeNode() {
    return (IdNode) this.getChild(0);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public IdNode getElementTypeNode() {
    return (IdNode) this.getChild(1);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
