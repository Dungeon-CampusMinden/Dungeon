package dsl.parser.ast;

import dsl.error.ErrorListener;
import org.antlr.v4.runtime.tree.ErrorNode;

public class ASTErrorNode extends Node {
  private final ErrorNode internalErrorNode;

  public ASTErrorNode(ErrorNode errorNode) {
    super(Type.ErrorNode);
    this.internalErrorNode = errorNode;
  }

  public ASTErrorNode(ErrorNode errorNode, ErrorListener.ErrorRecord errorRecord) {
    this(errorNode);
    this.setErrorRecord(errorRecord);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}

