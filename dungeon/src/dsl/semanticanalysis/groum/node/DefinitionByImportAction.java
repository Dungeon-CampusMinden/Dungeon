package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.ImportFunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ImportAggregateTypeSymbol;

public class DefinitionByImportAction extends ActionNode {
  public static final int instancedTypeIdx = 0;
  public static final int instanceSymbolIdx = 1;
  public static final int originalSymbolIdx = 2;

  public DefinitionByImportAction(Symbol symbol, long instanceId) {
    super(ActionType.definitionByImport);

    if (symbol instanceof ImportFunctionSymbol functionSymbol) {
      this.addSymbolReference(functionSymbol.getFunctionType());
      this.addSymbolReference(symbol);
      this.addSymbolReference(functionSymbol.originalFunctionSymbol());
    } else if (symbol instanceof ImportAggregateTypeSymbol typeSymbol) {
      this.addSymbolReference(getInstanceSymbolType(symbol));
      this.addSymbolReference(symbol);
      this.addSymbolReference(typeSymbol.originalTypeSymbol());
    }
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public IType instancedType() {
    return (IType) this.symbolReferences().get(instancedTypeIdx);
  }

  // how to handle this in plain pattern groum?
  public Symbol instanceSymbol() {
    return this.symbolReferences().get(instanceSymbolIdx);
  }

  public Symbol originalSymbol() {
    return this.symbolReferences().get(originalSymbolIdx);
  }

  @Override
  public String getLabel() {
    return this.instancedType().toString() + ":<import [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
