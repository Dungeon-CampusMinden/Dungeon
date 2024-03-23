package dsl.parser.ast;

import org.antlr.v4.runtime.tree.ErrorNode;

public class ASTErrorNode extends Node {
  private final ErrorNode internalErrorNode;

  public ASTErrorNode(ErrorNode errorNode) {
    super(Type.ErrorNode);
    this.internalErrorNode = errorNode;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
