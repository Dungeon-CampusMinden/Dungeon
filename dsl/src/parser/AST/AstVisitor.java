package parser.AST;

public interface AstVisitor<T> {
    /**
     * Basic fallback method for all Node types
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(Node node) {
        return null;
    }

    /**
     * Visitor method for IdNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(IdNode node) {
        return null;
    }

    /**
     * Visitor method for NumNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(NumNode node) {
        return null;
    }

    /**
     * Visitor method for StringNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(StringNode node) {
        return null;
    }

    /**
     * Visitor method for BinaryNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(BinaryNode node) {
        return null;
    }

    /**
     * Visitor method for DotDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(DotDefNode node) {
        return null;
    }

    /**
     * Visitor method for EdgeRhsNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(EdgeRhsNode node) {
        return null;
    }

    /**
     * Visitor method for EdgeStmtNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(EdgeStmtNode node) {
        return null;
    }

    /**
     * Visitor method for EdgeOpNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(EdgeOpNode node) {
        return null;
    }

    /**
     * Visitor method for PropertyDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(PropertyDefNode node) {
        return null;
    }

    /**
     * Visitor method for ObjectDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(ObjectDefNode node) {
        return null;
    }

    default void visitChildren(Node node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
