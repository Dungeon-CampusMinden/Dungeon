package parser.ast;

public class ReturnStmtNode extends Node {
    public Node getInnerStmtNode() {
        return getChild(0);
    }

    public ReturnStmtNode(Node innerStmt) {
        super(Type.ReturnStmt);
        this.children.add(innerStmt);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
