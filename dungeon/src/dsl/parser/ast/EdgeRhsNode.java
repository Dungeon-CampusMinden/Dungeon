package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class EdgeRhsNode extends Node {
  private final int edgeOpIdx = 0;
  private final int idNodeIdx = 1;

  /**
   * WTF? .
   *
   * @return The EdgeOpNode corresponding to the EdgeOperator
   */
  public Node getEdgeOpNode() {
    return this.getChild(edgeOpIdx);
  }

  /**
   * WTF? .
   *
   * @return The IdNodeList corresponding to the referenced identifiers on the right-hand-side
   */
  public Node getIdNodeList() {
    return this.getChild(idNodeIdx);
  }

  /**
   * WTF? .
   *
   * @return The {@link EdgeOpNode.Type} of the EdgeOperator
   */
  public EdgeOpNode.Type getEdgeOpType() {
    return ((EdgeOpNode) getEdgeOpNode()).getEdgeOpType();
  }

  /**
   * Constructor. WTF? .
   *
   * @param edgeOpNode The EdgeOpNode corresponding to the EdgeOperator
   * @param idNodeList The IdNode corresponding to teh identifier on the right-hand-side
   */
  public EdgeRhsNode(Node edgeOpNode, Node idNodeList) {
    super(Type.DotEdgeRHS, new ArrayList<>(2));
    this.addChild(edgeOpNode);
    this.addChild(idNodeList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
