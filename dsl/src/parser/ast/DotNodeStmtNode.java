package parser.ast;

public class DotNodeStmtNode extends Node {
    public DotNodeStmtNode(IdNode id, DotAttrListNode attrList) {
        super(Type.DotNodeStmt);
        addChild(id);
        addChild(attrList);
    }

    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
