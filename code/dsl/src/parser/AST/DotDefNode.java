package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class DotDefNode extends Node {
    // TODO: getter for specific stmt-Node
    private final int idNodeIdx = 0;
    private final int dotStmtStartIdx = 1;

    public Type getGraphType() {
        return graphType;
    }

    public Node getIdNode() {
        return this.children.get(idNodeIdx);
    }

    public String getGraphId() {
        return ((IdNode) getIdNode()).getName();
    }

    public List<Node> getStmtNodes() {
        return this.children.subList(dotStmtStartIdx, this.children.size());
    }

    public enum Type {
        NONE,
        graph,
        digraph
    }

    private final Type graphType;

    public DotDefNode(Type graphType, Node graphId, ArrayList<Node> dotStmts) {
        super(Node.Type.DotDefinition, new ArrayList<>(dotStmts.size() + 1));
        this.children.add(graphId);
        this.children.addAll(dotStmts);
        this.graphType = graphType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
