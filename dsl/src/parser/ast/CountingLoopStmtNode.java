package parser.ast;

public class CountingLoopStmtNode extends ForLoopStmtNode {
    public final int counterIdIdx = 4;

    public Node getCounterIdNode() {
        return this.getChild(counterIdIdx);
    }

    public CountingLoopStmtNode(Node typeIdNode, Node varIdNode, Node iterableIdNode, Node stmtNode, Node counterIdNode) {
        super(LoopType.countingForLoop, typeIdNode, varIdNode, iterableIdNode, stmtNode);
        this.addChild(counterIdNode);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
