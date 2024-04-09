package dsl.parser.ast;

import dsl.error.ErrorListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class ASTOffendingSymbol extends Node {
  private TerminalNode offendingTerminal;

  public ASTOffendingSymbol() {
    super(Type.ErrorNode);
    this.offendingTerminal = null;
  }

  public ASTOffendingSymbol(TerminalNode offendingTerminal, ErrorListener.ErrorRecord errorRecord) {
    super(Type.ErrorNode);
    this.offendingTerminal = offendingTerminal;
    this.setErrorRecord(errorRecord);
  }

  public TerminalNode getOffendingTerminal() {
    return offendingTerminal;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
