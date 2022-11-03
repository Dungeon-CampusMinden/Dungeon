package parser.AST;

public interface AstVisitor<T> {

    /**
     * Basic fallback method for all Node types
     *
     * @param node Node to visit
     * @return T
     */
    T visit(Node node);

    /**
     * Visitor method for IdNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(IdNode node);

    /**
     * Visitor method for BinaryNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(BinaryNode node);

    /**
     * Visitor method for DotDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(DotDefNode node);

    /**
     * Visitor method for EdgeRhsNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(EdgeRhsNode node);

    /**
     * Visitor method for EdgeStmtNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(EdgeStmtNode node);

    /**
     * Visitor method for EdgeOpNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(EdgeOpNode node);
}
