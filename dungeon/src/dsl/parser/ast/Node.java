package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class Node {
  // used for running index to give every Node a unique identifier
  private static int _idx;

  /**
   * WTF? .
   *
   * @return The unique index of this node
   */
  public int getIdx() {
    return idx;
  }

  /** WTF? . */
  public enum Type {
    /** WTF? . */
    NONE,
    /** WTF? . */
    Program,
    /** WTF? . */
    Stmt,
    /** WTF? . */
    ObjectDefinition,
    /** WTF? . */
    PropertyDefinitionList,
    /** WTF? . */
    PropertyDefinition,
    /** WTF? . */
    PrototypeDefinition,
    /** WTF? . */
    ComponentDefinitionList,
    /** WTF? . */
    AggregateValueDefinition,
    /** WTF? . */
    DotDefinition,
    /** WTF? . */
    DotStmtList,
    /** WTF? . */
    DotStmt,
    /** WTF? . */
    DotEdgeStmt,
    /** WTF? . */
    DotEdgeRHS,
    /** WTF? . */
    DotNodeStmt,
    /** WTF? . */
    DotAttrList,
    /** WTF? . */
    DotAttr,
    /** WTF? . */
    DotEdgeOp,
    /** WTF? . */
    DotIdList,
    /** WTF? . */
    DotDependencyType,
    /** WTF? . */
    DotDependencyTypeAttr,
    /** WTF? . */
    DoubleLine,
    /** WTF? . */
    Arrow,
    /** WTF? . */
    Number,
    /** WTF? . */
    DecimalNumber,
    /** WTF? . */
    StringLiteral,
    /** WTF? . */
    Identifier,
    /** WTF? . */
    TypeSpecifier,
    /** WTF? . */
    FuncCall,
    /** WTF? . */
    ParamList,
    /** WTF? . */
    ParamDefList,
    /** WTF? . */
    StmtList,
    /** WTF? . */
    FuncDef,
    /** WTF? . */
    ParamDef,
    /** WTF? . */
    ReturnStmt,
    /** WTF? . */
    Block,
    /** WTF? . */
    ConditionalStmtIf,
    /** WTF? . */
    Bool,
    /** WTF? . */
    ConditionalStmtIfElse,
    /** WTF? . */
    ReturnMark,
    /** WTF? . */
    ScopeExitMark,
    /** WTF? . */
    Assignment,
    /** WTF? . */
    LogicOr,
    /** WTF? . */
    LogicAnd,
    /** WTF? . */
    Equality,
    /** WTF? . */
    Comparison,
    /** WTF? . */
    Term,
    /** WTF? . */
    Factor,
    /** WTF? . */
    Unary,
    /** WTF? . */
    MemberAccess,
    /** WTF? . */
    GroupedExpression,
    /** WTF? . */
    ListDefinitionNode,
    /** WTF? . */
    SetDefinitionNode,
    /** WTF? . */
    ListTypeIdentifierNode,
    /** WTF? . */
    SetTypeIdentifierNode,
    /** WTF? . */
    MapTypeIdentifierNode,
    /** WTF? . */
    LoopStmtNode,
    /** WTF? . */
    LoopBottomMark,
    /** WTF? . */
    ItemPrototypeDefinition,
    /** WTF? . */
    VarDeclNode
  }

  /** WTF? . */
  public static Node NONE = new Node(Type.NONE, new ArrayList<>());

  private ArrayList<Node> children;

  /** WTF? . */
  public final Type type;

  private Node parent;
  private SourceFileReference sourceFileReference = SourceFileReference.NULL;
  private final int idx;

  /**
   * Constructor for AST-Node with children.
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
   * Constructor for AST-Node without children.
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
   * Constructor for AST-Node with SourceFileReference (e.g. for terminal symbols).
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
   * Get specific child of the node by index.
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

  /**
   * WTF? .
   *
   * @param node
   */
  public void addChild(Node node) {
    this.children.add(node);
    node.parent = this;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
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
   * WTF? (erster Satz KURZ).
   *
   * <p>Get the {@link SourceFileReference} of this node. If this node is not a terminal or was not
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
   * WTF? .
   *
   * @param <T> foo
   * @param visitor foo
   * @return foo
   */
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
