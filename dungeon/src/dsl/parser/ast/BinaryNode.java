package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public abstract class BinaryNode extends Node {
  /** WTF? . */
  public final int lhsIdx = 0;

  /** WTF? . */
  public final int rhsIdx = 1;

  /**
   * WTF? .
   *
   * @return the right-hand-side of the binary node
   */
  public Node getRhs() {
    return getChild(rhsIdx);
  }

  /**
   * WTF? .
   *
   * @return the left-hand-side of the binary node
   */
  public Node getLhs() {
    return getChild(lhsIdx);
  }

  protected BinaryNode(Type type, Node lhs, Node rhs) {
    super(type, new ArrayList<>(2));

    addChild(lhs);
    addChild(rhs);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
