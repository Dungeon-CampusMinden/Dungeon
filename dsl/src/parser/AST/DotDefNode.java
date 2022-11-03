package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class DotDefNode extends Node {
    private final int idNodeIdx = 0;
    private final int dotStmtStartIdx = 1;

    /**
     * @return the {@link Type} of the dot graph definition
     */
    public Type getGraphType() {
        return graphType;
    }

    /**
     * @return the IdNode corresponding to the identifier of the graph
     */
    public Node getIdNode() {
        return this.children.get(idNodeIdx);
    }

    /**
     * @return the identifier of the graph as a String
     */
    public String getGraphId() {
        return ((IdNode) getIdNode()).getName();
    }

    /**
     * @return all dot statements in the graph definition as a list
     */
    public List<Node> getStmtNodes() {
        return this.children.subList(dotStmtStartIdx, this.children.size());
    }

    public enum Type {
        NONE,
        graph,
        digraph
    }

    private final Type graphType;

    /**
     * Constructor
     *
     * @param graphType The {@link Type} of this graph
     * @param graphId The IdNode corresponding to the identifier of the graph
     * @param dotStmts A list of all dot statements in the definition
     */
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
