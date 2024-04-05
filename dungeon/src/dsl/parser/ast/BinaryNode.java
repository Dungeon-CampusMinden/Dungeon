package dsl.parser.ast;

import java.util.ArrayList;

public abstract class BinaryNode extends Node {
  public final int lhsIdx = 0;
  public final int rhsIdx = 1;

  /**
   * @return the right-hand-side of the binary node
   */
  public Node getRhs() {
    return getChild(rhsIdx);
  }

  /**
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

  public BinaryNode(Type type) {
    super(type);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
