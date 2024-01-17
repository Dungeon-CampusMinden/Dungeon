package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class ImportAggregateTypeSymbol extends AggregateType {
  protected final AggregateType originalTypeSymbol;

  public AggregateType originalTypeSymbol() {
    return originalTypeSymbol;
  }

  public ImportAggregateTypeSymbol(AggregateType originalTypeSymbol, IScope parentScope) {
    super(originalTypeSymbol.getName(), parentScope);
    this.originalTypeSymbol = originalTypeSymbol;
  }

  public ImportAggregateTypeSymbol(
      AggregateType originalTypeSymbol, String name, IScope parentScope) {
    super(name, parentScope);
    this.originalTypeSymbol = originalTypeSymbol;
  }

  @Override
  public void setTypeMemberToField(HashMap<String, Field> typeMemberToField) {
    // only allowed via original symbol
    throw new RuntimeException("This operation is not allowed via an imported symbol");
  }

  @Override
  public HashMap<String, Field> getTypeMemberToField() {
    return this.originalTypeSymbol.getTypeMemberToField();
  }

  @Override
  public Kind getTypeKind() {
    return this.originalTypeSymbol.getTypeKind();
  }

  @Override
  public Class<?> getOriginType() {
    return this.originalTypeSymbol.getOriginType();
  }

  @Override
  public boolean bind(Symbol symbol) {
    // only allowed via original symbol
    throw new RuntimeException("This operation is not allowed via an imported symbol");
  }

  @Override
  public Symbol resolve(String name, boolean resolveInParent) {
    return this.originalTypeSymbol.resolve(name, resolveInParent);
  }

  @Override
  public Symbol resolve(String name) {
    return this.originalTypeSymbol.resolve(name);
  }

  @Override
  public IScope getParent() {
    return this.originalTypeSymbol.getParent();
  }

  @Override
  public List<Symbol> getSymbols() {
    return this.originalTypeSymbol.getSymbols();
  }
}
