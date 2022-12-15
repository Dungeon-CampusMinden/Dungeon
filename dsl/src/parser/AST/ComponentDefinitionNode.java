package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class ComponentDefinitionNode extends Node {

    public final int idIdx = 0;
    public final int propertyDefinitionListIdx = 1;

    public String getIdName() {
        return ((IdNode) getIdNode()).getName();
    }

    public Node getIdNode() {
        return getChild(idIdx);
    }

    public Node getPropertyDefinitionListNode() {
        return getChild(propertyDefinitionListIdx);
    }

    public List<Node> getPropertyDefinitionNodes() {
        return getPropertyDefinitionListNode().getChildren();
    }

    public ComponentDefinitionNode(Node idNode, Node propertyDefinitionList) {
        super(Type.ComponentDefinition, new ArrayList<>(2));
        this.children.add(idNode);
        this.children.add(propertyDefinitionList);
    }

    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
