package parser.ast;

public class VarDeclNode extends BinaryNode {

    public VarDeclNode(Node identifier, Node rhs) {
        super (Type.VarDeclNode, identifier, rhs);
    }
}
