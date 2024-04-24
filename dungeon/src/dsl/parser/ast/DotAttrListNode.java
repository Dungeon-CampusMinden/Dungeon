package dsl.parser.ast;

import java.util.List;

/** WTF? . */
public class DotAttrListNode extends Node {
  /**
   * WTF? .
   *
   * @param attributes foo
   */
  public DotAttrListNode(List<Node> attributes) {
    super(Type.DotAttrList);
    attributes.forEach(this::addChild);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
