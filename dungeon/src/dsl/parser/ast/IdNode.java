package dsl.parser.ast;

/** WTF? . */
public class IdNode extends Node {
  private final String name;

  /**
   * Constructor. WTF? .
   *
   * @param name The literal value of the identifier
   * @param sourceFileReference Reference to the location of the identifier in the source file
   */
  public IdNode(String name, SourceFileReference sourceFileReference) {
    super(Type.Identifier, sourceFileReference);
    this.name = name;
  }

  /**
   * Constructor for subclasses.
   *
   * @param nodeType the type of this node
   * @param name The literal value of the identifier
   * @param sourceFileReference Reference to the location of the identifier in the source file
   */
  protected IdNode(Type nodeType, String name, SourceFileReference sourceFileReference) {
    super(nodeType, sourceFileReference);
    this.name = name;
  }

  /**
   * WTF? .
   *
   * @return The name of the identifier
   */
  public String getName() {
    return name;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
