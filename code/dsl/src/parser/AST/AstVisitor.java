package parser.AST;

public interface AstVisitor<T> {
    T visit(Node node);

    T visit(IdNode node);

    T visit(BinaryNode node);

    T visit(DotDefNode node);

    T visit(EdgeRhsNode node);

    T visit(EdgeStmtNode node);

    T visit(EdgeOpNode node);
}
