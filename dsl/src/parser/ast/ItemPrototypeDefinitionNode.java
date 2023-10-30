package parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ItemPrototypeDefinitionNode extends Node {
    public final int idIdx = 0;
    public final int valueDefinitionListIdx = 1;

    /**
     * @return literal String of the identifier of the game object definition node
     */
    public String getIdName() {
        return ((IdNode) getIdNode()).getName();
    }

    /**
     * @return the node representing the identifier of this game object definition node
     */
    public Node getIdNode() {
        return getChild(idIdx);
    }

    /**
     * @return the node representing the component definitions of this game object definition node
     */
    public Node getPropertyDefinitionListNode() {
        return getChild(valueDefinitionListIdx);
    }

    /**
     * @return a List of nodes representing individual component definitions of this game object
     *     definition node
     */
    public List<Node> getPropertyDefinitionNodes() {
        return getPropertyDefinitionListNode().getChildren();
    }

    /**
     * Constructor
     *
     * @param idNode node representing the identifier of the game object definition
     * @param valueDefinitionListNode node representing the component definition list of the game
     *     object definition
     */
    public ItemPrototypeDefinitionNode(Node idNode, Node valueDefinitionListNode) {
        super(Type.ItemPrototypeDefinition, new ArrayList<Node>(2));
        this.addChild(idNode);
        this.addChild(valueDefinitionListNode);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
