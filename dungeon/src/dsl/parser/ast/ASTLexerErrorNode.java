package dsl.parser.ast;

import dsl.error.ErrorListener;

public class ASTLexerErrorNode extends Node {
  public ASTLexerErrorNode(ErrorListener.ErrorRecord errorRecord) {
    super(Type.ErrorNode);
    this.setErrorRecord(errorRecord);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
