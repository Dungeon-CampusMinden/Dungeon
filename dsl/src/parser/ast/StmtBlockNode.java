package parser.ast;

import java.util.List;

public class StmtBlockNode extends Node {
    public List<Node> getStmts() {
        return this.getChild(0).getChildren();
    }

    public StmtBlockNode(Node stmtList) {
        super(Type.Block);
        this.children.add(stmtList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
