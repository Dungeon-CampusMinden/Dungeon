package parser.ast;

import java.util.ArrayList;
import java.util.List;

// TODO: refactor to store all IDs in plain list?
public class EdgeStmtNode extends Node {
    private final int lhsIdIdx = 0;

    private final int rhsStmtsStartIdx = 1;
    private int attrListIdx = -1;

    /**
     * Constructor
     *
     * @param lhsID The {@link IdNode} corresponding to the identifier on the left-hand-side
     * @param rhsStmts A list of all following {@link EdgeRhsNode}s of the statement
     * @param attrList The {@link Node} corresponding to the attribute list of the statement (or
     *     'Node.NONE', if there is no attribute list)
     */
    public EdgeStmtNode(Node lhsID, ArrayList<Node> rhsStmts, Node attrList) {
        super(Type.DotEdgeStmt, new ArrayList<>());
        this.addChild(lhsID);
        rhsStmts.forEach(this::addChild);
        this.attrListIdx = this.getChildren().size();
        this.addChild(attrList);
    }

    /**
     * @return The {@link IdNode} corresponding to the identifier on the left-hand-side
     */
    public Node getLhsId() {
        return this.getChild(lhsIdIdx);
    }

    /**
     * @return A list of {@link EdgeRhsNode}s of the statement
     */
    public List<Node> getRhsStmts() {
        return this.getChildren().subList(rhsStmtsStartIdx, attrListIdx);
    }

    /**
     * @return The {@link Node} corresponding to the attribute list of the statement (or
     *     'Node.NONE', if there is no attribute list)
     */
    public Node getAttrList() {
        return this.getChild(attrListIdx);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
