package parser.AST;

import java.util.ArrayList;

public class EdgeRhsNode extends Node {
    private final int edgeOpIdx = 0;
    private final int idNodeIdx = 1;

    public Node getEdgeOpNode() {
        return this.children.get(edgeOpIdx);
    }

    public Node getIdNode() {
        return this.children.get(idNodeIdx);
    }

    public EdgeOpNode.Type getEdgeOpType() {
        return ((EdgeOpNode) getEdgeOpNode()).getEdgeOpType();
    }

    public EdgeRhsNode(Node edgeOpNode, Node idNode) {
        super(Type.DotEdgeRHS, new ArrayList<>(2));
        this.children.add(edgeOpNode);
        this.children.add(idNode);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
