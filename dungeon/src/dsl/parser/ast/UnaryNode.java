package dsl.parser.ast;

import java.util.ArrayList;

public class UnaryNode extends Node {
  public enum UnaryType {
    not,
    minus
  }

  public Node getInnerNode() {
    return this.getChild(0);
  }

  private final UnaryType unaryType;

  public UnaryType getUnaryType() {
    return unaryType;
  }

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
