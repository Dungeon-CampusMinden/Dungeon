package dsl.parser.ast;

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
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for IdNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(IdNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DecNumNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DecNumNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for NumNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(NumNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for StringNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(StringNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for BinaryNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(BinaryNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotDefNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotDefNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for EdgeRhsNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(EdgeRhsNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotEdgeStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotEdgeStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotAttrNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotAttrNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotNodeLists
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotIdList node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotNodeStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotNodeStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotAttrListNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotAttrListNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotDependencyTypeNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotDependencyTypeNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for DotDependencyTypeAttrNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(DotDependencyTypeAttrNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for EdgeOpNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(EdgeOpNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for PropertyDefNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(PropertyDefNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ObjectDefNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ObjectDefNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for FuncCallNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(FuncCallNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ComponentDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(AggregateValueDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for FuncDefNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(FuncDefNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ParamDefNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ParamDefNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for GameObjectDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(PrototypeDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ReturnStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ReturnStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ConditionStmtNodeIfs
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ConditionalStmtNodeIf node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ConditionalStmtNodeIfElses
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ConditionalStmtNodeIfElse node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for StmtBlockNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(StmtBlockNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for BoolNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(BoolNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for MemberAccessNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(MemberAccessNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for LogicOrNode
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(LogicOrNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for LogicAndNode
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(LogicAndNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for EqualityNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(EqualityNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ComparisonNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ComparisonNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for TermNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(TermNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for FactorNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(FactorNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for UnaryNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(UnaryNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for AssignmentNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(AssignmentNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ListDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ListDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for SetDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(SetDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ListTYpeIdentifierNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ListTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for SetTypeIdentifierNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(SetTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for MapTypeDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(MapTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for LoopStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(LoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for WhileLoopStmtNode
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(WhileLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for CountingForLoopStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(CountingLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ForLoopStmtNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ForLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for LoopBottomMarks
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(LoopBottomMark node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for VarDeclNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(VarDeclNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ItemPrototypeDefinitionNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ItemPrototypeDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Visitor method for ImportNodes
   *
   * @param node Node to visit
   * @return T
   */
  default T visit(ImportNode node) {
    throw new UnsupportedOperationException();
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
