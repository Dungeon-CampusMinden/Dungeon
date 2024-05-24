package dsl.parser.ast;

public class VarDeclNode extends BinaryNode {
  public enum DeclType {
    none,
    assignmentDecl,
    typeDecl
  }

  private final VarDeclNode.DeclType declType;

  public Node getIdentifier() {
    return this.getLhs();
  }

  public VarDeclNode.DeclType getDeclType() {
    return declType;
  }

  public VarDeclNode(VarDeclNode.DeclType type, IdNode identifier, Node rhs) {
    super(Type.VarDeclNode, identifier, rhs);
    this.declType = type;
  }

  public VarDeclNode() {
    super(Type.VarDeclNode);
    this.declType = DeclType.none;
  }

  public VarDeclNode(VarDeclNode.DeclType type) {
    super(Type.VarDeclNode);
    this.declType = type;
  }

  public String getVariableName() {
    return ((IdNode) this.getIdentifier()).getName();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
