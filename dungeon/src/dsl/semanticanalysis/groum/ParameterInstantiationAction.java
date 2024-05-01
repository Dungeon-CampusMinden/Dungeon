package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class ParameterInstantiationAction extends ActionNode {
  public static final int instantiatedTypeIdx = 0;
  public static final int parameterSymbol = 1;

  public ParameterInstantiationAction(Symbol parameterSymbol, long instanceId) {
    super(ActionType.parameterInstantiation);
    this.addSymbolReference((Symbol) parameterSymbol.getDataType());
    this.addSymbolReference(parameterSymbol);
    this.referencedInstanceId(instanceId);
  }

  public IType instantiatedType() {
    return (IType) this.symbolReferences().get(instantiatedTypeIdx);
  }

  public Symbol parameterSymbol() {
    return this.symbolReferences().get(parameterSymbol);
  }

  @Override
  public String getLabel() {
    return this.instantiatedType().toString()
        + ":<param_init ["
        + this.referencedInstanceId()
        + "]>"
        + "(name: '"
        + this.parameterSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
