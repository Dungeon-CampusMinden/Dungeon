package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class EdgeStmtNode extends Node {
    private final int lhsIdIdx = 0;

    private final int rhsStmtsStartIdx = 1;
    private int attrListIdx = -1;

    public EdgeStmtNode(Node lhsID, ArrayList<Node> rhsStmts, Node attrList) {
        super(Type.DotEdgeStmt, new ArrayList<>());
        this.children.add(lhsID);
        this.children.addAll(rhsStmts);
        this.attrListIdx = this.children.size();
        this.children.add(attrList);
    }

    public Node getLhsId() {
        return this.children.get(lhsIdIdx);
    }

    public List<Node> getRhsStmts() {
        return this.children.subList(rhsStmtsStartIdx, attrListIdx);
    }

    public Node getAttrList() {
        return this.children.get(attrListIdx);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
