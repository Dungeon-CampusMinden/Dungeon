package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;

public class FunctionCallAction extends ActionNode {
  public static final int functionSymbolIdx = 0;

  public FunctionCallAction(Symbol functionSymbol, long instanceId) {
    super(ActionType.functionCall);
    this.addSymbolReference(functionSymbol);
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public Symbol functionSymbol() {
    return this.symbolReferences().get(functionSymbolIdx);
  }

  @Override
  public String getLabel() {
    return "<call '" + this.functionSymbol().getName() + "' [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
