package parser.AST;

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

    /**
     * Constructor
     *
     * @param sourceFileReference The sourceFileReference of the node corresponding the
     *     EdgeOperator.
     * @param edgeOpType The {@link Type} of the new EdgeOperator
     */
    public EdgeOpNode(SourceFileReference sourceFileReference, Type edgeOpType) {
        super(Node.Type.DotEdgeOp, sourceFileReference);
        this.edgeOpType = edgeOpType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
