package dsl.parser.ast;

import java.util.ArrayList;

public class Node {
  // used for running index to give every Node a unique identifier
  private static int _idx;

  /**
   * @return The unique index of this node
   */
  public int getIdx() {
    return idx;
  }

  public enum Type {
    NONE,
    Program,
    Stmt,
    ObjectDefinition,
    PropertyDefinitionList,
    PropertyDefinition,
    PrototypeDefinition,
    ComponentDefinitionList,
    AggregateValueDefinition,
    DotDefinition,
    DotStmtList,
    DotStmt,
    DotEdgeStmt,
    DotEdgeRHS,
    DotNodeStmt,
    DotAttrList,
    DotAttr,
    DotEdgeOp,
    DotIdList,
    DotDependencyType,
    DotDependencyTypeAttr,
    DoubleLine,
    Arrow,
    Number,
    DecimalNumber,
    StringLiteral,
    Identifier,
    TypeSpecifier,
    FuncCall,
    ParamList,
    ParamDefList,
    StmtList,
    FuncDef,
    ParamDef,
    ReturnStmt,
    Block,
    ConditionalStmtIf,
    Bool,
    ConditionalStmtIfElse,
    ReturnMark,
    ScopeExitMark,
    Assignment,
    LogicOr,
    LogicAnd,
    Equality,
    Comparison,
    Term,
    Factor,
    Unary,
    MemberAccess,
    GroupedExpression,
    ListDefinitionNode,
    SetDefinitionNode,
    ListTypeIdentifierNode,
    SetTypeIdentifierNode,
    MapTypeIdentifierNode,
    LoopStmtNode,
    LoopBottomMark,
    ItemPrototypeDefinition,
    VarDeclNode,
    ImportNode
  }

  public static Node NONE = new Node(Type.NONE, new ArrayList<>());

  private ArrayList<Node> children;
  public final Type type;
  private Node parent;
  private SourceFileReference sourceFileReference = SourceFileReference.NULL;
  private final int idx;

  /**
   * Constructor for AST-Node with children
   *
   * @param nodeType The {@link Type} of the new node
   * @param nodeChildren List of children of the node
   */
  public Node(Type nodeType, ArrayList<Node> nodeChildren) {
    _idx++;
    idx = _idx;

    type = nodeType;
    children = nodeChildren;
    parent = NONE;

    for (var child : nodeChildren) {
      child.parent = this;
    }
  }

  /**
   * Constructor for AST-Node without children
   *
   * @param nodeType The {@link Type} of the node
   */
  public Node(Type nodeType) {
    _idx++;
    idx = _idx;

    type = nodeType;
    children = new ArrayList<>();
    parent = NONE;
  }

  /**
   * Constructor for AST-Node with SourceFileReference (e.g. for terminal symbols)
   *
   * @param nodeType The {@link Type} of the new node
   * @param sourceReference The {@link SourceFileReference} for the new node
   */
  public Node(Type nodeType, SourceFileReference sourceReference) {
    _idx++;
    idx = _idx;

    type = nodeType;
    children = new ArrayList<>();
    sourceFileReference = sourceReference;
    parent = NONE;
  }

  /**
   * Get specific child of the node by index
   *
   * @param idx The index of the child
   * @return The child with index
   */
  public Node getChild(int idx) {
    if (idx >= children.size()) {
      return Node.NONE;
    }

    return children.get(idx);
  }

  public void addChild(Node node) {
    this.children.add(node);
    node.parent = this;
  }

  public Node getParent() {
    return parent;
  }

  /**
   * Get all children of this node.
   *
   * @return List of all children of the node.
   */
  public ArrayList<Node> getChildren() {
    return new ArrayList<>(children);
  }

  /**
   * Get the {@link SourceFileReference} of this node. If this node is not a terminal or was not
   * given a specific {@link SourceFileReference} on construction, a pre-order depth-first search
   * for a {@link SourceFileReference} is performed on the node's children.
   *
   * @return The {@link SourceFileReference} for this node (or the first one found in pre-order dps
   *     in children).
   */
  public SourceFileReference getSourceFileReference() {
    if (sourceFileReference != SourceFileReference.NULL) {
      return sourceFileReference;
    } else if (children.size() > 0) {
      for (var child : children) {
        var childrensSourceFileReference = child.getSourceFileReference();
        if (childrensSourceFileReference != SourceFileReference.NULL) {
          // cache calculated sourceFileReference
          sourceFileReference = childrensSourceFileReference;
          return childrensSourceFileReference;
        }
      }
    }
    return SourceFileReference.NULL;
  }

  /**
   * Implementation of visitor pattern with {@link AstVisitor}.
   *
   * @param visitor Specific implementation of {@link AstVisitor} to use with this node.
   * @return T
   * @param <T> The return value of the visit
   */
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
