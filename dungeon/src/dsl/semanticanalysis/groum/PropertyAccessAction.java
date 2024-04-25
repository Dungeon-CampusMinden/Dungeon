package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class PropertyAccessAction extends ActionNode {
  public static final int instanceTypeIdx = 0;
  public static final int instanceSymbolIdx = 1;
  public static final int propertySymbolIdx = 2;

  public PropertyAccessAction(Symbol instanceSymbol, Symbol property, long instanceId) {
    super(ActionType.propertyAccess);

    this.addSymbolReference(getInstanceSymbolType(instanceSymbol));
    this.addSymbolReference(instanceSymbol);
    this.addSymbolReference(property);
    this.referencedInstanceId(instanceId);
  }

  public IType instanceDataType() {
    return (IType) this.symbolReferences().get(instanceTypeIdx);
  }

  public Symbol propertySymbol() {
    return this.symbolReferences().get(propertySymbolIdx);
  }

  public Symbol instanceSymbol() {
    return this.symbolReferences().get(instanceSymbolIdx);
  }

  @Override
  public String getLabel() {
    return this.instanceDataType().getName() + ":<property access '" + this.propertySymbol().getName() + "' [" + this.referencedInstanceId()+"]>";
  }
}
