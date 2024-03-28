package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** WTF? . */
public class AggregateValueDefinitionNode extends Node {

  /** WTF? . */
  public final int idIdx = 0;

  /** WTF? . */
  public final int propertyDefinitionListIdx = 1;

  /**
   * WTF? .
   *
   * @return literal String of the identifier of the component definition node
   */
  public String getIdName() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return the node representing the identifier of this component definition node
   */
  public Node getIdNode() {
    return getChild(idIdx);
  }

  /**
   * WTF? .
   *
   * @return the node representing the property definitions of this component definition node
   */
  public Node getPropertyDefinitionListNode() {
    return getChild(propertyDefinitionListIdx);
  }

  /**
   * WTF? .
   *
   * @return a List of nodes representing individual property definitions of this component
   *     definition node
   */
  public List<Node> getPropertyDefinitionNodes() {
    return getPropertyDefinitionListNode().getChildren();
  }

  /**
   * Constructor. WTF? .
   *
   * @param idNode node representing the identifier of the component definition
   * @param propertyDefinitionList node representing the property definition list of the component
   *     definition
   */
  public AggregateValueDefinitionNode(Node idNode, Node propertyDefinitionList) {
    super(Type.AggregateValueDefinition, new ArrayList<>(2));
    this.addChild(idNode);
    this.addChild(propertyDefinitionList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
