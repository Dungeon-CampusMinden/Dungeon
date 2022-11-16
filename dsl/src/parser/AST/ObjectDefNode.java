package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class ObjectDefNode extends Node {
    public final int typeSpecifierIdx = 0;
    public final int idIdx = 1;
    public final int propertyDefListIdx = 2;

    public enum Type {
        NONE,
        GrammarBuiltInType,
        IdForType
    }

    private final Type type;

    public Type getDefinitionType() {
        return this.type;
    }

    public Node getTypeSpecifier() {
        return this.getChild(typeSpecifierIdx);
    }

    public Node getId() {
        return this.getChild(idIdx);
    }

    public List<Node> getPropertyDefinitions() {
        return this.children.subList(propertyDefListIdx, this.children.size());
    }

    public ObjectDefNode(Node typeSpecifier, Node id, Node propertyDefList, Type type) {
        super(
                Node.Type.ObjectDefinition,
                new ArrayList<>(propertyDefList.getChildren().size() + 2));
        this.type = type;

        this.children.add(typeSpecifier);
        this.children.add(id);
        this.children.add(propertyDefList);
    }
}
