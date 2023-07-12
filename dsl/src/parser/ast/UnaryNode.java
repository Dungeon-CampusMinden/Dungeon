package parser.ast;

public class UnaryNode extends BinaryNode {
    enum UnaryType {
        not,
        minus
    }

    private final UnaryType unaryType;

    public UnaryNode(UnaryType type, Node lhs, Node rhs) {
        super(Type.Factor, lhs, rhs);
        this.unaryType = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
