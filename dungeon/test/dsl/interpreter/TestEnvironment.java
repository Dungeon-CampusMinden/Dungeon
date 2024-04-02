package dsl.interpreter;

import dsl.runtime.callable.NativeFunction;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dslinterop.dslnativefunction.NativeInstantiate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// TODO: revise class-structure.. maybe the GameEnvironment should extend the
//  'DefaultEnvironment`, which only bind the basic built in types and the
//  'TestEnvironment' would be a parallel implementation, not a deriving implementation
public class TestEnvironment extends GameEnvironment {
  private final HashSet<String> mockTypeNames;

  @Override
  public TypeBuilder typeBuilder() {
    return super.typeBuilder();
  }

  public TestEnvironment() {
    super();

    // build scenario builder return type
    Symbol entityTypeSymbol = this.getGlobalScope().resolve("entity");
    // if (entityTypeSymbol != Symbol.NULL) {
    IType entityType = (IType) entityTypeSymbol;
    IType entitySetType =
        this.typeFactory()
            .setType(
                entityType,
                this.getGlobalScope()); // new SetType(entityType, this.getGlobalScope());
    this.loadTypes(entitySetType);
    IType entitySetSetType = this.typeFactory().setType(entitySetType, this.getGlobalScope());
    // new SetType(entitySetType, this.getGlobalScope());
    this.loadTypes(entitySetSetType);
    this.mockTypeNames = new HashSet<>();
  }

  @Override
  public boolean isTypeName(String name) {
    return super.isTypeName(name) || this.mockTypeNames.contains(name);
  }

  public void addMockTypeName(String name) {
    this.mockTypeNames.add(name);
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
