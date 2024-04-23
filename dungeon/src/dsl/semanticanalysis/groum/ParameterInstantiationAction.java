package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class ParameterInstantiationAction extends ActionNode {
  public static final int instantiatedTypeIdx = 0;
  public static final int parameterSymbol = 1;

  public ParameterInstantiationAction(Symbol parameterSymbol, long instanceId) {
    super(ActionType.passAsParameter);
    this.addSymbolReference((Symbol) parameterSymbol.getDataType());
    this.addSymbolReference(parameterSymbol);
    this.referencedInstanceId(instanceId);
  }

  public IType instantiatedType() {
    return (IType) this.symbolReferences().get(instantiatedTypeIdx);
  }

  @Override
  public String getLabel() {
    return this.instantiatedType().toString()
        + ":<param_init ["
        + this.referencedInstanceId()
        + "]>";
  }
}
