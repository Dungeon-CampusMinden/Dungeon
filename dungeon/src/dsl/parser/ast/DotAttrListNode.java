package dsl.parser.ast;

import java.util.List;

public class DotAttrListNode extends Node {
    public DotAttrListNode(List<Node> attributes) {
        super(Type.DotAttrList);
        attributes.forEach(this::addChild);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
