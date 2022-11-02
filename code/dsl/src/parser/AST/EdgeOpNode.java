package parser.AST;

import java.util.ArrayList;

public class EdgeOpNode extends Node {

    /**
     * @return The {@link Type} of this EdgeOperator
     */
    public Type getEdgeOpType() {
        return edgeOpType;
    }

    public enum Type {
        NONE,
        arrow,
        doubleLine
    }

    private final Type edgeOpType;

    // TODO: is the Node really needed? This is used because of easier SourceReferencing,
    // but this could be passed manually; the Node itself is not needed -> do in Cleanup
    /**
     * Constructor
     *
     * @param childNode The node corresponding the EdgeOperator
     * @param edgeOpType The {@link Type} of the new EdgeOperator
     */
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
