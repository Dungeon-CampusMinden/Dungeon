package parser.ast;

import java.util.ArrayList;
import java.util.List;

// TODO: refactor to store all IDs in plain list?
public class DotEdgeStmtNode extends Node {
    private final int lhsIdIdx = 0;

    private final int rhsStmtsStartIdx = 1;
    private int attrListIdx = -1;

    /**
     * Constructor
     *
     * @param ids A list of all {@link IdNode}s of the statement in order
     * @param attrList The {@link Node} corresponding to the attribute list of the statement (or
     *     'Node.NONE', if there is no attribute list)
     */
    public DotEdgeStmtNode(List<Node> ids, Node attrList) {
        super(Type.DotEdgeStmt, new ArrayList<>());
        ids.forEach(this::addChild);
        this.attrListIdx = this.getChildren().size();
        this.addChild(attrList);
    }

    /**
     * @return The {@link IdNode} corresponding to the identifier on the left-hand-side
     */
    // public Node getLhsId() {
    //     return this.getChild(lhsIdIdx);
    // }

    /**
     * @return A list of {@link EdgeRhsNode}s of the statement
     */
    // public List<Node> getRhsStmts() {
    //     return this.getChildren().subList(rhsStmtsStartIdx, attrListIdx);
    // }

    public List<Node> getIds() {
        return this.getChildren().subList(0, attrListIdx);
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
