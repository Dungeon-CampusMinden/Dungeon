package parser.AST;

import java.util.ArrayList;

public abstract class BinaryNode extends Node {
    public final int lhsIdx = 0;
    public final int rhsIdx = 1;

    public Node GetRhs() {
        return children.get(rhsIdx);
    }

    public Node GetLhs() {
        return children.get(lhsIdx);
    }

    protected BinaryNode(Type type, Node lhs, Node rhs) {
        super(type, new ArrayList<>(2));

        children.add(lhs);
        children.add(rhs);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
