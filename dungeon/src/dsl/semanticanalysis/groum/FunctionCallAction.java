package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;

public class FunctionCallAction extends ActionNode {
  public static final int functionSymbolIdx = 0;

  public FunctionCallAction(Symbol functionSymbol, long instanceId) {
    super(ActionType.functionCall);
    this.addSymbolReference(functionSymbol);
    this.referencedInstanceId(instanceId);
  }

  public Symbol functionSymbol() {
    return this.symbolReferences().get(functionSymbolIdx);
  }

  @Override
  public String getLabel() {
    return "<call '" + this.functionSymbol().getName() +"' ["+this.referencedInstanceId()+"]>";
  }
}
