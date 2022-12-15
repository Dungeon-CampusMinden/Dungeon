package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class GameObjectDefinitionNode extends Node {
    public final int idIdx = 0;
    public final int componentDefinitionListIdx = 1;

    public String getIdName() {
        return ((IdNode) getIdNode()).getName();
    }

    public Node getIdNode() {
        return getChild(idIdx);
    }

    public Node getComponentDefinitionListNode() {
        return getChild(componentDefinitionListIdx);
    }

    public List<Node> getComponentDefinitionNodes() {
        return getComponentDefinitionListNode().getChildren();
    }

    public GameObjectDefinitionNode(Node idNode, Node componentDefinitionList) {
        super(Type.GameObjectDefinition, new ArrayList<Node>(2));
        this.children.add(idNode);
        this.children.add(componentDefinitionList);
    }

    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
