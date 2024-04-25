package dsl.interpreter;

import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;
import java.util.ArrayList;
import java.util.List;

/** WTF? . */
// TODO: revise class-structure.. maybe the GameEnvironment should extend the
//  'DefaultEnvironment`, which only bind the basic built in types and the
//  'TestEnvironment' would be a parallel implementation, not a deriving implementation
public class TestEnvironment extends GameEnvironment {
  /**
   * WTF? .
   *
   * @return foo
   */
  @Override
  public TypeBuilder getTypeBuilder() {
    return super.getTypeBuilder();
  }

  /** WTF? . */
  public TestEnvironment() {
    super();

    // build scenario builder return type
    Symbol entityTypeSymbol = this.getGlobalScope().resolve("entity");
    // if (entityTypeSymbol != Symbol.NULL) {
    IType entityType = (IType) entityTypeSymbol;
    IType entitySetType = new SetType(entityType, this.getGlobalScope());
    this.loadTypes(entitySetType);
    IType entitySetSetType = new SetType(entitySetType, this.getGlobalScope());
    this.loadTypes(entitySetSetType);
  }

  @Override
  public Class<?>[] getBuiltInAggregateTypeClasses() {
    return new Class[] {CustomQuestConfig.class};
  }

  @Override
  public List<IDSLExtensionProperty<?, ?>> getBuiltInProperties() {
    return new ArrayList<>();
  }

  @Override
  protected void registerDefaultRuntimeObjectTranslators() {}

  @Override
  protected void registerDefaultTypeAdapters() {}

  @Override
  protected ArrayList<Symbol> buildDependantNativeFunctions() {
    return new ArrayList<>();
  }

  @Override
  protected void bindBuiltInProperties() {}

  @Override
  protected void bindBuiltInMethods() {}
}
