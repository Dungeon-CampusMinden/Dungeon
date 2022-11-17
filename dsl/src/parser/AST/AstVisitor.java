package parser.AST;

public interface AstVisitor<T> {
    // TODO: add default implementations for all methods, as most visitors only need to override
    //  a partial set of all methods

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
     * Visitor method for NumNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(NumNode node);

    /**
     * Visitor method for StringNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(StringNode node);

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

    /**
     * Visitor method for PropertyDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(PropertyDefNode node);

    /**
     * Visitor method for ObjectDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    T visit(ObjectDefNode node);

    default void visitChildren(Node node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
