package parser.AST;

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
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
     * Visitor method for DecNumNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(DecNumNode node) {
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

    /**
     * Visitor method for FuncCallNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(FuncCallNode node) {
        return null;
    }

    /**
     * Visitor method for ComponentDefinitionNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(AggregateValueDefinitionNode node) {
        return null;
    }

    /**
     * Visitor method for ParamDefNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(ParamDefNode node) {
        return null;
    }

    /**
     * Visitor method for GameObjectDefinitionNodes
     *
     * @param node Node to visit
     * @return T
     */
    default T visit(GameObjectDefinitionNode node) {
        return null;
    }

    /**
     * Visit all children of the passed node
     *
     * @param node The node to visit all children of
     */
    default void visitChildren(Node node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
