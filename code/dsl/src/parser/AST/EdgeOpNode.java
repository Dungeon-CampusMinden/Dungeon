package parser.AST;

import java.util.ArrayList;

public class EdgeOpNode extends Node {
    public Type getEdgeOpType() {
        return edgeOpType;
    }

    public enum Type {
        NONE,
        arrow,
        doubleLine
    }

    private final Type edgeOpType;

    public EdgeOpNode(Node childNode, Type edgeOpType) {
        super(Node.Type.DotEdgeOp, new ArrayList<>());
        assert (childNode.type == Node.Type.Arrow || childNode.type == Node.Type.DoubleLine);
        this.children.add(childNode);
        this.edgeOpType = edgeOpType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
