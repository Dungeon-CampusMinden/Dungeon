package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class InstantiationAction extends ActionNode {
  public final static int instancedTypeIdx = 0;
  public final static int instanceSymbolIdx = 1;

  public InstantiationAction(Symbol symbol, long instanceId) {
    super(ActionType.instantiation);
    this.addSymbolReference((Symbol)symbol.getDataType());
    this.addSymbolReference(symbol);
    this.referencedInstanceId(instanceId);
  }

  public IType instancedType() {
    return (IType) this.symbolReferences().get(instancedTypeIdx);
  }

  // how to handle this in plain pattern groum?
  public Symbol instanceSymbol() {
    return this.symbolReferences().get(instanceSymbolIdx);
  }

  @Override
  public String getLabel() {
    return this.instancedType().toString()+":<init ["+this.referencedInstanceId()+"]>";
  }
}
