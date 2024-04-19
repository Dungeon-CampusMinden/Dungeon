package dsl.parser.ast;

/** WTF? . */
public class BoolNode extends Node {
  private final boolean value;

  /**
   * Constructor. WTF? .
   *
   * @param value The integer value of the number
   * @param sourceFileReference Reference to the location of the identifier in the source file
   */
  public BoolNode(boolean value, SourceFileReference sourceFileReference) {
    super(Type.Bool, sourceFileReference);
    this.value = value;
  }

  /**
   * WTF? .
   *
   * @return The value of the number
   */
  public boolean getValue() {
    return value;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
