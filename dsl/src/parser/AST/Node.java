package parser.AST;

import java.util.ArrayList;

public class Node {
    // used for running index to give every Node a unique identifier
    private static int _idx;

    /**
     * @return The unique index of this node
     */
    public int getIdx() {
        return idx;
    }

    public enum Type {
        NONE,
        Program,
        Stmt,
        ObjectDefinition,
        PropertyDefinitionList,
        PropertyDefinition,
        GameObjectDefinition,
        ComponentDefinitionList,
        AggregateValueDefinition,
        DotDefinition,
        DotStmtList,
        DotStmt,
        DotEdgeStmt,
        DotEdgeRHS,
        DotAttrStmt,
        DotNodeStmt,
        DotAttrList,
        DotAList,
        DotEdgeOp,
        DoubleLine,
        Arrow,
        Number,
        DecimalNumber,
        StringLiteral,
        Identifier,
        TypeSpecifier,
        FuncCall,
        ParamList,
        ParamDefList,
        StmtList,
        FuncDef,
        ParamDef
    }

    public static Node NONE = new Node(Type.NONE, new ArrayList<>());

    protected ArrayList<Node> children;
    public final Type type;
    private Node parent;
    private SourceFileReference sourceFileReference = SourceFileReference.NULL;
    private final int idx;

    /**
     * Constructor for AST-Node with children
     *
     * @param nodeType The {@link Type} of the new node
     * @param nodeChildren List of children of the node
     */
    public Node(Type nodeType, ArrayList<Node> nodeChildren) {
        _idx++;
        idx = _idx;

        type = nodeType;
        children = nodeChildren;
        parent = NONE;

        for (var child : nodeChildren) {
            child.parent = this;
        }
    }

    /**
     * Constructor for AST-Node without children
     *
     * @param nodeType The {@link Type} of the node
     */
    public Node(Type nodeType) {
        _idx++;
        idx = _idx;

        type = nodeType;
        children = new ArrayList<>();
        parent = NONE;
    }

    /**
     * Constructor for AST-Node with SourceFileReference (e.g. for terminal symbols)
     *
     * @param nodeType The {@link Type} of the new node
     * @param sourceReference The {@link SourceFileReference} for the new node
     */
    public Node(Type nodeType, SourceFileReference sourceReference) {
        _idx++;
        idx = _idx;

        type = nodeType;
        children = new ArrayList<>();
        sourceFileReference = sourceReference;
        parent = NONE;
    }

    /**
     * Get specific child of the node by index
     *
     * @param idx The index of the child
     * @return The child with index
     */
    public Node getChild(int idx) {
        if (idx >= children.size()) {
            return Node.NONE;
        }

        return children.get(idx);
    }

    /**
     * Get all children of this node.
     *
     * @return List of all children of the node.
     */
    public ArrayList<Node> getChildren() {
        return children;
    }

    /**
     * Get the {@link SourceFileReference} of this node. If this node is not a terminal or was not
     * given a specific {@link SourceFileReference} on construction, a pre-order depth-first search
     * for a {@link SourceFileReference} is performed on the node's children.
     *
     * @return The {@link SourceFileReference} for this node (or the first one found in pre-order
     *     dps in children).
     */
    public SourceFileReference getSourceFileReference() {
        if (sourceFileReference != SourceFileReference.NULL) {
            return sourceFileReference;
        } else if (children.size() > 0) {
            for (var child : children) {
                var childrensSourceFileReference = child.getSourceFileReference();
                if (childrensSourceFileReference != SourceFileReference.NULL) {
                    // cache calculated sourceFileReference
                    sourceFileReference = childrensSourceFileReference;
                    return childrensSourceFileReference;
                }
            }
        }
        return SourceFileReference.NULL;
    }

    /**
     * Implementation of visitor pattern with {@link AstVisitor}.
     *
     * @param visitor Specific implementation of {@link AstVisitor} to use with this node.
     * @return T
     * @param <T> The return value of the visit
     */
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
