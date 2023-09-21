package parser.ast;

public class DotAttrNode extends BinaryNode {

    DotAttrNode(IdNode lhs, IdNode rhs) {
        super(Type.DotAttr, lhs, rhs);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
