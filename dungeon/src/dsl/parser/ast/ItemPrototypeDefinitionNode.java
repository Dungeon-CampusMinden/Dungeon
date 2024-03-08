package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** Represents an `item_type`-definition. */
public class ItemPrototypeDefinitionNode extends Node {
  /** WTF? . */
  public final int idIdx = 0;

  /** WTF? . */
  public final int valueDefinitionListIdx = 1;

  /**
   * WTF? .
   *
   * @return literal String of the identifier of the item type definition node
   */
  public String getIdName() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return the node representing the identifier of this item type definition node
   */
  public Node getIdNode() {
    return getChild(idIdx);
  }

  /**
   * WTF? .
   *
   * @return the node representing the component definitions of this item type definition node
   */
  public Node getPropertyDefinitionListNode() {
    return getChild(valueDefinitionListIdx);
  }

  /**
   * WTF? .
   *
   * @return a List of nodes representing individual property definitions of this item type
   *     definition node
   */
  public List<Node> getPropertyDefinitionNodes() {
    return getPropertyDefinitionListNode().getChildren();
  }

  /**
   * Constructor. WTF? .
   *
   * @param idNode node representing the identifier of the item type definition
   * @param propertyDefinitionListNode node representing the property definition list of the game
   *     object definition
   */
  public ItemPrototypeDefinitionNode(Node idNode, Node propertyDefinitionListNode) {
    super(Type.ItemPrototypeDefinition, new ArrayList<>(2));
    this.addChild(idNode);
    this.addChild(propertyDefinitionListNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
