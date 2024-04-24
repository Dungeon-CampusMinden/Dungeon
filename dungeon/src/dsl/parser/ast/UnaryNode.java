package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class UnaryNode extends Node {
  /** WTF? . */
  public enum UnaryType {
    /** WTF? . */
    not,
    /** WTF? . */
    minus
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getInnerNode() {
    return this.getChild(0);
  }

  private final UnaryType unaryType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public UnaryType getUnaryType() {
    return unaryType;
  }

  /**
   * WTF? .
   *
   * @param type foo
   * @param inner foo
   */
  public UnaryNode(UnaryType type, Node inner) {
    super(Type.Unary, new ArrayList<>(1));
    this.unaryType = type;
    this.addChild(inner);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
