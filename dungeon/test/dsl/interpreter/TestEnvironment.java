package dsl.interpreter;

import dsl.runtime.callable.NativeFunction;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;
import dslinterop.dslnativefunction.NativeInstantiate;

import java.util.ArrayList;
import java.util.List;

// TODO: revise class-structure.. maybe the GameEnvironment should extend the
//  'DefaultEnvironment`, which only bind the basic built in types and the
//  'TestEnvironment' would be a parallel implementation, not a deriving implementation
public class TestEnvironment extends GameEnvironment {

  @Override
  public TypeBuilder getTypeBuilder() {
    return super.getTypeBuilder();
  }

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
    ArrayList<Symbol> nativeFunctions = new ArrayList<>();
    IType entityType = (IType) this.globalScope.resolve("entity");
    NativeFunction nativeInstantiate = new NativeInstantiate(Scope.NULL, entityType);
    nativeFunctions.add(nativeInstantiate);
    return nativeFunctions;
  }

  @Override
  protected void bindBuiltInProperties() {}

  @Override
  protected void bindBuiltInMethods() {}
}
