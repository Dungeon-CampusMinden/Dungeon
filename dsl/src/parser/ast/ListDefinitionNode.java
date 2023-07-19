package parser.ast;

import java.util.ArrayList;

public class ListDefinitionNode extends Node {

    public ArrayList<Node> getEntries() {
        return this.getChild(0).getChildren();
    }

    public ListDefinitionNode(Node entryList) {
        super(Type.ListDefinitionNode, new ArrayList<>(1));
        this.children.add(entryList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

