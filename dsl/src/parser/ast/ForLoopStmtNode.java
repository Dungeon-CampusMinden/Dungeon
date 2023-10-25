package parser.ast;

public class ForLoopStmtNode extends LoopStmtNode {
    public final int typeIdIdx = 1;
    public final int varIdIdx = 2;
    public final int iterableIdIdx = 3;

    public Node getTypeIdNode() {
        return this.getChild(typeIdIdx);
    }

    public Node getVarIdNode() {
        return this.getChild(varIdIdx);
    }

    public Node getIterableIdNode() {
        return this.getChild(iterableIdIdx);
    }

    public ForLoopStmtNode(Node typeIdNode, Node varIdNode, Node iterableIdNode, Node stmtNode) {
        super(LoopType.forLoop, stmtNode);
        addChildren(typeIdNode, varIdNode, iterableIdNode);
    }

    protected ForLoopStmtNode(LoopType loopType, Node typeIdNode, Node varIdNode, Node iterableIdNode, Node stmtNode) {
        super(loopType, stmtNode);
        addChildren(typeIdNode, varIdNode, iterableIdNode);
    }

    private void addChildren(Node typeIdNode, Node varIdNode, Node iterableIdNode) {
        this.addChild(typeIdNode);
        this.addChild(varIdNode);
        this.addChild(iterableIdNode);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
