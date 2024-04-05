package dsl.parser.ast;

public class DecNumNode extends Node {
  private final float value;

  /**
   * Constructor
   *
   * @param value The integer value of the number
   * @param sourceFileReference Reference to the location of the identifier in the source file
   */
  public DecNumNode(float value, SourceFileReference sourceFileReference) {
    super(Type.DecimalNumber, sourceFileReference);
    this.value = value;
  }

  public DecNumNode() {
    super(Type.DecimalNumber);
    this.value = 0.0f;
  }

  /**
   * @return The value of the number
   */
  public float getValue() {
    return value;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
