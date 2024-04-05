package dsl.parser.ast;

public class NumNode extends Node {
  private final int value;

  /**
   * Constructor
   *
   * @param value The integer value of the number
   * @param sourceFileReference Reference to the location of the identifier in the source file
   */
  public NumNode(int value, SourceFileReference sourceFileReference) {
    super(Type.Number, sourceFileReference);
    this.value = value;
  }

  public NumNode() {
    super(Type.Number);
    this.value = 0;
  }

  /**
   * @return The value of the number
   */
  public int getValue() {
    return value;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
