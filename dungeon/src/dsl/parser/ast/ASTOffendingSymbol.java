package dsl.parser.ast;

import dsl.error.ErrorListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ASTOffendingSymbol extends Node {
  private TerminalNode offendingTerminal;

  public ASTOffendingSymbol(TerminalNode offendingTerminal, ErrorListener.ErrorRecord errorRecord) {
    super(Type.ErrorNode);
    this.offendingTerminal = offendingTerminal;
    this.setErrorRecord(errorRecord);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
