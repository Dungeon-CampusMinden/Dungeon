package parser.AST;

import java.util.ArrayList;

public class EdgeRhsNode extends Node {
    private final int edgeOpIdx = 0;
    private final int idNodeIdx = 1;

    /**
     * @return The EdgeOpNode corresponding to the EdgeOperator
     */
    public Node getEdgeOpNode() {
        return this.children.get(edgeOpIdx);
    }

    /**
     * @return The IdNode corresponding to the referenced identifier on the right-hand-side
     */
    public Node getIdNode() {
        return this.children.get(idNodeIdx);
    }

    /**
     * @return The {@link EdgeOpNode.Type} of the EdgeOperator
     */
    public EdgeOpNode.Type getEdgeOpType() {
        return ((EdgeOpNode) getEdgeOpNode()).getEdgeOpType();
    }

    /**
     * Constructor
     *
     * @param edgeOpNode The EdgeOpNode corresponding to the EdgeOperator
     * @param idNode The IdNode corresponding to teh identifier on the right-hand-side
     */
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
