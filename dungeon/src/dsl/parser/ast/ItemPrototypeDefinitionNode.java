package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** Represents an `item_type`-definition. */
public class ItemPrototypeDefinitionNode extends Node {
  public final int idIdx = 0;
  public final int valueDefinitionListIdx = 1;

  /**
   * @return literal String of the identifier of the item type definition node
   */
  public String getIdName() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * @return the node representing the identifier of this item type definition node
   */
  public Node getIdNode() {
    return getChild(idIdx);
  }

  /**
   * @return the node representing the component definitions of this item type definition node
   */
  public Node getPropertyDefinitionListNode() {
    return getChild(valueDefinitionListIdx);
  }

  /**
   * @return a List of nodes representing individual property definitions of this item type
   *     definition node
   */
  public List<Node> getPropertyDefinitionNodes() {
    return getPropertyDefinitionListNode().getChildren();
  }

  /**
   * Constructor
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

  public ItemPrototypeDefinitionNode() {
    super(Type.ItemPrototypeDefinition);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
