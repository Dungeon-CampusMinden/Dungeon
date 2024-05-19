package dsl.parser.ast;

/** WTF? . */
public class VarDeclNode extends BinaryNode {
  /** WTF? . */
  public enum DeclType {
    /** WTF? . */
    assignmentDecl,
    /** WTF? . */
    typeDecl
  }

  private final VarDeclNode.DeclType declType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getIdentifier() {
    return this.getLhs();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public VarDeclNode.DeclType getDeclType() {
    return declType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param identifier foo
   * @param rhs foo
   */
  public VarDeclNode(VarDeclNode.DeclType type, IdNode identifier, Node rhs) {
    super(Type.VarDeclNode, identifier, rhs);
    this.declType = type;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getVariableName() {
    return ((IdNode) this.getIdentifier()).getName();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
