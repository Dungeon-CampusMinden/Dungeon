package dsl.parser.ast;

import dsl.IndexGenerator;
import dsl.error.ErrorRecord;
import dsl.programmanalyzer.Relatable;
import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.neo4j.ogm.annotation.*;

@NodeEntity(label = "AstNode")
public class Node implements Relatable {
  // used for running index to give every Node a unique identifier
  // TODO: this really just for testing!! -> remove it!
  private static long g_fileVersion;

  public static void setFileVersion(long version) {
    g_fileVersion = version;
  }

  @Property private boolean hasErrorChild;
  @Property private boolean subTreeHasError;

  @Relate(type = "HAS_ERROR_RECORD")
  @Transient
  protected ErrorRecord errorRecord;

  @Property private boolean hasErrorRecord;
  @Property protected long fileVersion;

  @Property @Transient private RecognitionException exception;

  @Override
  public Long getId() {
    return this.internalId;
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
    ExpressionList,
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
    ImportNode,
    ErrorNode
  }

  public static Node NONE = new Node(Type.NONE, new ArrayList<>());

  @Relate(type = "PARENT_OF")
  @Transient
  protected ArrayList<Node> children;

  @Property public final Type type;

  @Relate(type = "CHILD_OF")
  @Transient
  protected Node parent;

  @Relate @Transient protected SourceFileReference sourceFileReference = SourceFileReference.NULL;

  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();

  // @Id private final long id = IndexGenerator.getIdx();

  /*private void recordRelationships() {
    RelationshipRecorder.instance.add(this.idx, "HAS_ERROR_RECORD", errorRecord.idx);
    RelationshipRecorder.instance.add(
        this.idx, "PARENT_OF", children.stream().map(Node::getIdx).toList());
    RelationshipRecorder.instance.add(this.idx, "CHILD_OF", parent.getIdx());
    RelationshipRecorder.instance.add(this.idx, "SOURCE_FILE_REFERENCE", sourceFileReference.id);
  }*/

  public Node() {
    this(Type.NONE, new ArrayList<>());
    RelationshipRecorder.instance.addRelatable(this);
    // recordRelationships();
  }

  /**
   * Constructor for AST-Node with children
   *
   * @param nodeType The {@link Type} of the new node
   * @param nodeChildren List of children of the node
   */
  public Node(Type nodeType, ArrayList<Node> nodeChildren) {
    this.fileVersion = g_fileVersion;

    type = nodeType;
    children = nodeChildren;
    parent = NONE;

    for (var child : nodeChildren) {
      child.parent = this;

      if (child.type == Type.ErrorNode) {
        this.hasErrorChild = true;
      }
      if (child.hasErrorChild || child.subTreeHasError || child.hasErrorRecord) {
        this.subTreeHasError = true;
      }
    }

    for (int i = 0; i < nodeChildren.size(); i++) {
      var child = nodeChildren.get(i);
      // TODO: do we need this? CHILD_OF relationship can be queried backwards...
      // RelationshipRecorder.instance.add(this, child, i);
    }
    RelationshipRecorder.instance.addRelatable(this);
  }

  /**
   * Constructor for AST-Node without children
   *
   * @param nodeType The {@link Type} of the node
   */
  public Node(Type nodeType) {
    this.fileVersion = g_fileVersion;

    type = nodeType;
    children = new ArrayList<>();
    parent = NONE;
    RelationshipRecorder.instance.addRelatable(this);
  }

  /**
   * Constructor for AST-Node with SourceFileReference (e.g. for terminal symbols)
   *
   * @param nodeType The {@link Type} of the new node
   * @param sourceReference The {@link SourceFileReference} for the new node
   */
  public Node(Type nodeType, SourceFileReference sourceReference) {
    this.fileVersion = g_fileVersion;

    type = nodeType;
    children = new ArrayList<>();
    sourceFileReference = sourceReference;
    parent = NONE;
    RelationshipRecorder.instance.addRelatable(this);
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

  public void addChildren(List<Node> children) {
    for (var child : children) {
      this.addChild(child);
    }
  }

  public void addChild(Node node) {
    int idx = this.children.size();
    // RelationshipRecorder.instance.add(this, node, idx);
    this.children.add(node);
    node.parent = this;

    if (node.type.equals(Type.ErrorNode)) {
      this.hasErrorChild = true;
    }
    if (node.hasErrorChild || node.subTreeHasError || node.hasErrorRecord) {
      this.subTreeHasError = true;
    }
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

  public boolean hasErrorChild() {
    return this.hasErrorChild;
  }

  public boolean subTreeHasError() {
    return this.subTreeHasError;
  }

  public ErrorRecord getErrorRecord() {
    return this.errorRecord;
  }

  public void setErrorRecord(ErrorRecord errorRecord) {
    if (this.equals(Node.NONE)) {
      throw new RuntimeException("TRIED TO SET ERROR RECORD IN Node.NONE!!");
    }
    if (errorRecord != null) {
      this.errorRecord = errorRecord;
      this.hasErrorRecord = true;
    }
  }

  public boolean hasErrorRecord() {
    return this.hasErrorRecord;
  }

  public boolean hasError() {
    return this.hasErrorRecord || this.hasErrorChild;
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

  public void setSourceFileReference(ParserRuleContext ctx) {
    this.sourceFileReference = SourceFileReference.fromCtx(ctx);
  }

  public void setSourceFileReference(SourceFileReference sfr) {
    this.sourceFileReference = sfr;
  }
}
