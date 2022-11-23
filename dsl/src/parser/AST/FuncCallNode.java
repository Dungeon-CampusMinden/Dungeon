package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class FuncCallNode extends Node {
    public final int idIdx = 0;
    public final int paramListIdx = 1;

    public Node getId() {
        return this.getChild(idIdx);
    }

    public String getIdName() {
        return ((IdNode) this.getChild(idIdx)).getName();
    }

    public List<Node> getParameters() {
        return this.children.get(paramListIdx).getChildren();
    }

    public FuncCallNode(Node id, Node paramList) {
        super(
            Type.ObjectDefinition,
            new ArrayList<>(paramList.getChildren().size() + 1));

        this.children.add(id);
        this.children.add(paramList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
