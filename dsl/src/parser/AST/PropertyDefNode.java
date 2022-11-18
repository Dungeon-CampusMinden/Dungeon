package parser.AST;

import java.util.ArrayList;

public class PropertyDefNode extends Node {
    public final int idIdx = 0;
    public final int stmtIdx = 1;

    public Node getIdNode() {
        return this.children.get(idIdx);
    }

    public Node getStmtNode() {
        return this.children.get(stmtIdx);
    }

    public String getIdName() {
        return ((IdNode) this.getIdNode()).getName();
    }

    public PropertyDefNode(Node id, Node stmt) {
        super(Type.PropertyDefinition, new ArrayList<>(2));
        this.children.add(id);
        this.children.add(stmt);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
