package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.ImportFunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ImportAggregateTypeSymbol;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class DefinitionByImportAction extends ActionNode {
  @Relationship private final IType instancedType;
  @Relationship private final Symbol instanceSymbol;
  @Relationship private final Symbol originalSymbol;

  public DefinitionByImportAction(Symbol symbol, long instanceId) {
    super(ActionType.definitionByImport);

    if (symbol instanceof ImportFunctionSymbol functionSymbol) {
      this.instancedType = functionSymbol.getFunctionType();
      this.instanceSymbol = symbol;
      this.originalSymbol = functionSymbol.originalFunctionSymbol();
    } else if (symbol instanceof ImportAggregateTypeSymbol typeSymbol) {
      this.instancedType = (IType) getInstanceSymbolType(symbol);
      this.instanceSymbol = symbol;
      this.originalSymbol = typeSymbol.originalTypeSymbol();
    } else {
      this.instancedType = BuiltInType.noType;
      this.instanceSymbol = Symbol.NULL;
      this.originalSymbol = Symbol.NULL;
    }
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public IType instancedType() {
    return this.instancedType;
  }

  // how to handle this in plain pattern groum?
  public Symbol instanceSymbol() {
    return this.instanceSymbol;
  }

  public Symbol originalSymbol() {
    return this.originalSymbol;
  }

  @Override
  public String getLabel() {
    return this.instancedType().toString() + ":<import [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}