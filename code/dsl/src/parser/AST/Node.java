package parser.AST;

import java.util.ArrayList;

public class Node {
    private static int _idx;

    public int getIdx() {
        return Idx;
    }

    public enum Type {
        NONE,
        Program,
        Stmt,
        ObjectDefinition,
        PropertyDefinition,
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
        StringLiteral,
        Identifier
    }

    public static Node NONE = new Node(Type.NONE, new ArrayList<>());

    protected ArrayList<Node> children;
    public final Type type;
    private Node parent;
    private SourceFileReference sourceFileReference = SourceFileReference.NULL;
    private final int Idx;

    public Node(Type nodeType, ArrayList<Node> nodeChildren) {
        _idx++;
        Idx = _idx;

        type = nodeType;
        children = nodeChildren;
        parent = NONE;

        for (var child : nodeChildren) {
            child.parent = this;
        }
    }

    public Node(Type nodeType) {
        _idx++;
        Idx = _idx;

        type = nodeType;
        children = new ArrayList<>();
        parent = NONE;
    }

    public Node(Type nodeType, SourceFileReference sourceReference) {
        _idx++;
        Idx = _idx;

        type = nodeType;
        children = new ArrayList<>();
        sourceFileReference = sourceReference;
        parent = NONE;
    }

    public Node getChild(int idx) {
        assert idx < children.size();

        return children.get(idx);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

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

    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
