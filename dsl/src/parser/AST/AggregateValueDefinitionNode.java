package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class AggregateValueDefinitionNode extends Node {

    public final int idIdx = 0;
    public final int propertyDefinitionListIdx = 1;

    /**
     * @return literal String of the identifier of the component definition node
     */
    public String getIdName() {
        return ((IdNode) getIdNode()).getName();
    }

    /**
     * @return the node representing the identifier of this component definition node
     */
    public Node getIdNode() {
        return getChild(idIdx);
    }

    /**
     * @return the node representing the property definitions of this component definition node
     */
    public Node getPropertyDefinitionListNode() {
        return getChild(propertyDefinitionListIdx);
    }

    /**
     * @return a List of nodes representing individual property definitions of this component
     *     definition node
     */
    public List<Node> getPropertyDefinitionNodes() {
        return getPropertyDefinitionListNode().getChildren();
    }

    /**
     * Constructor
     *
     * @param idNode node representing the identifier of the component definition
     * @param propertyDefinitionList node representing the property definition list of the component
     *     definition
     */
    public AggregateValueDefinitionNode(Node idNode, Node propertyDefinitionList) {
        super(Type.AggregateValueDefinition, new ArrayList<>(2));
        this.children.add(idNode);
        this.children.add(propertyDefinitionList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
