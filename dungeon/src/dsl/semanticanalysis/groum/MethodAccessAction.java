package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;

public class MethodAccessAction extends ActionNode {
  public static final int instanceSymbolIdx = 0;
  public static final int methodSymbolIdx = 1;

  public MethodAccessAction(Symbol instanceSymbol, FunctionSymbol method, long instanceId) {
    super(ActionType.functionCallAccess);
    this.addSymbolReference(instanceSymbol);
    this.addSymbolReference(method);
    this.referencedInstanceId(instanceId);
  }

  public Symbol instanceSymbol() {
    return this.symbolReferences().get(instanceSymbolIdx);
  }

  public FunctionSymbol methodSymbol() {
    return (FunctionSymbol) this.symbolReferences().get(methodSymbolIdx);
  }

  @Override
  public String getLabel() {
    return "";
  }
}
