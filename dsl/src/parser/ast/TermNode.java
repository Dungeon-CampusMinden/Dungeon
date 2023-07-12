package parser.ast;

public class TermNode extends BinaryNode {
    enum TermType {
        plus,
        minus
    }

    private final TermType termType;

    public TermNode(TermType type, Node lhs, Node rhs) {
        super(Type.Term, lhs, rhs);
        this.termType = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

