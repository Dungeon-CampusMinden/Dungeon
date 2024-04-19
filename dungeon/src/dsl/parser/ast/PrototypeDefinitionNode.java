package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** WTF? . */
public class PrototypeDefinitionNode extends Node {
  /** WTF? . */
  public final int idIdx = 0;

  /** WTF? . */
  public final int componentDefinitionListIdx = 1;

  /**
   * WTF? .
   *
   * @return literal String of the identifier of the game object definition node
   */
  public String getIdName() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return the node representing the identifier of this game object definition node
   */
  public Node getIdNode() {
    return getChild(idIdx);
  }

  /**
   * WTF? .
   *
   * @return the node representing the component definitions of this game object definition node
   */
  public Node getComponentDefinitionListNode() {
    return getChild(componentDefinitionListIdx);
  }

  /**
   * WTF? .
   *
   * @return a List of nodes representing individual component definitions of this game object
   *     definition node
   */
  public List<Node> getComponentDefinitionNodes() {
    return getComponentDefinitionListNode().getChildren();
  }

  /**
   * Constructor. WTF? .
   *
   * @param idNode node representing the identifier of the game object definition
   * @param componentDefinitionList node representing the component definition list of the game
   *     object definition
   */
  public PrototypeDefinitionNode(Node idNode, Node componentDefinitionList) {
    super(Type.PrototypeDefinition, new ArrayList<Node>(2));
    this.addChild(idNode);
    this.addChild(componentDefinitionList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
