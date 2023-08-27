package parser.ast;

public class VarDeclNode extends BinaryNode {
    public enum DeclType {
        assignmentDecl,
        typeDecl
    }

    private final VarDeclNode.DeclType declType;

    public Node getIdentifier() {
        return this.getLhs();
    }

    public VarDeclNode.DeclType getDeclType() {
        return declType;
    }

    public VarDeclNode(VarDeclNode.DeclType type, Node identifier, Node rhs) {
        super(Type.VarDeclNode, identifier, rhs);
        this.declType = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
