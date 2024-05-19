package dsl.parser.ast;

/** WTF? . */
public class TermNode extends BinaryNode {

  /** WTF? . */
  public enum TermType {
    /** WTF? . */
    plus,
    /** WTF? . */
    minus
  }

  private final TermType termType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public TermType getTermType() {
    return termType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param lhs foo
   * @param rhs foo
   */
  public TermNode(TermType type, Node lhs, Node rhs) {
    super(Type.Term, lhs, rhs);
    this.termType = type;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
