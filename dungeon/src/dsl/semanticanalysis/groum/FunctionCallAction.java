package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.FunctionSymbol;

public class FunctionCallAction extends ActionNode {
  public static final int functionSymbolIdx = 0;

  public FunctionCallAction(FunctionSymbol functionSymbol, long instanceId) {
    super(ActionType.passAsParameter);
    this.addSymbolReference(functionSymbol);
    this.referencedInstanceId(instanceId);
  }

  @Override
  public String getLabel() {
    return "";
  }
}
