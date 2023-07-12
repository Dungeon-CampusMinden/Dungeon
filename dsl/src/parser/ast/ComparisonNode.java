package parser.ast;

public class ComparisonNode extends BinaryNode {
    enum ComparisonType {
        greaterThan,
        greaterEquals,
        lessThan,
        lessEquals
    }

    private final ComparisonType comparisonType;

    public ComparisonNode(ComparisonType type, Node lhs, Node rhs) {
        super(Type.Equality, lhs, rhs);
        this.comparisonType = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

