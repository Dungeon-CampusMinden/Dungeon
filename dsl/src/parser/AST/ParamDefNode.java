package parser.AST;

import java.util.ArrayList;

public class ParamDefNode extends Node {
    public final int typeIdIdx = 0;
    public final int idIdx = 1;

    public Node getTypeIdNode() {
        return getChild(typeIdIdx);
    }

    public String getTypeName() {
        return ((IdNode) getTypeIdNode()).getName();
    }

    public String getIdName() {
        return ((IdNode) getIdNode()).getName();
    }

    public Node getIdNode() {
        return getChild(idIdx);
    }

    public ParamDefNode(Node typeIdNode, Node idNode) {
        super(Type.ParamDef, new ArrayList<>(2));
        this.children.add(typeIdNode);
        this.children.add(idNode);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
