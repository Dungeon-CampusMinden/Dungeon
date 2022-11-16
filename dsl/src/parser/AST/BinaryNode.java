package parser.AST;

import java.util.ArrayList;

public abstract class BinaryNode extends Node {
    public final int lhsIdx = 0;
    public final int rhsIdx = 1;

    /**
     * @return the right-hand-side of the binary node
     */
    public Node getRhs() {
        return children.get(rhsIdx);
    }

    /**
     * @return the left-hand-side of the binary node
     */
    public Node getLhs() {
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
