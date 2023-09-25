package parser.ast;

import java.util.ArrayList;
import java.util.List;

public class DotEdgeStmtNode extends Node {
    private final int lhsIdIdx = 0;

    private final int rhsStmtsStartIdx = 1;
    private int attrListIdx = -1;

    /**
     * Constructor
     *
     * @param idGroups A list of all {@link IdNode}s of the statement in order
     * @param attrList The {@link Node} corresponding to the attribute list of the statement (or
     *     'Node.NONE', if there is no attribute list)
     */
    public DotEdgeStmtNode(List<Node> idGroups, Node attrList) {
        super(Type.DotEdgeStmt, new ArrayList<>());
        idGroups.forEach(this::addChild);
        this.attrListIdx = this.getChildren().size();
        this.addChild(attrList);
    }

    // TODO: javadoc
    public List<DotNodeList> getIdGroups() {
        return this.getChildren().subList(0, attrListIdx).stream().map(node -> (DotNodeList)node).toList();
    }

    /**
     * @return The {@link Node} corresponding to the attribute list of the statement (or
     *     'Node.NONE', if there is no attribute list)
     */
    public Node getAttrListNode() {
        return this.getChild(attrListIdx);
    }

    public List<Node> getAttributes() {
        return this.getChild(attrListIdx).getChildren();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

