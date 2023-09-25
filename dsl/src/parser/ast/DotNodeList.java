package parser.ast;

import java.util.List;

public class DotNodeList extends Node {

    public List<IdNode> getIdNodes() {
        return this.getChildren().stream().map(node -> (IdNode) node).toList();
    }

    public List<String> getIdNames() {
        return this.getIdNodes().stream().map(idNode -> idNode.getName()).toList();
    }

    public DotNodeList(List<Node> idNodes) {
        super(Type.DotNodeList);
        idNodes.forEach(this::addChild);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
