package dsl.semanticanalysis.environment;

import dsl.runtime.interop.RuntimeObjectTranslator;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** WTF? . */
public interface IEnvironment {

  /**
   * WTF? .
   *
   * @return foo
   */
  TypeBuilder getTypeBuilder();

  /**
   * WTF? .
   *
   * @return all available types of the environment
   */
  default IType[] getTypes() {
    ArrayList<IType> types = new ArrayList<>();

    for (Symbol symbol : this.getGlobalScope().getSymbols()) {
      if (symbol instanceof IType) {
        types.add((IType) symbol);
      }
    }
    return types.toArray(new IType[0]);
  }

  // default Symbol lookupType(String name) { return Symbol.NULL; }

  /**
   * WTF? .
   *
   * @return all available function definitions
   */
  default Symbol[] getFunctions() {
    return new Symbol[0];
  }

  // default Symbol lookupFunction(String name) { return Symbol.NULL; }
  /**
   * WTF? .
   *
   * @param types AggregateTypes to load into the environment
   */
  default void loadTypes(IType... types) {}

  /**
   * WTF? .
   *
   * @param types AggregateTypes to load into the environment
   */
  default void loadTypes(List<IType> types) {}

  /**
   * WTF? .
   *
   * @param functionDefinitions FunctionSymbols to load into the environment
   */
  default void loadFunctions(ScopedSymbol... functionDefinitions) {}

  /**
   * WTF? .
   *
   * @param functionDefinitions FunctionSymbols to load into the environment
   */
  default void loadFunctions(List<ScopedSymbol> functionDefinitions) {}

  /**
   * WTF? .
   *
   * @return symbol table of this environment
   */
  SymbolTable getSymbolTable();

  /**
   * WTF? .
   *
   * @return global scope of this environment
   */
  IScope getGlobalScope();

  /**
   * WTF? .
   *
   * @return foo
   */
  default HashMap<Type, IType> javaTypeToDSLTypeMap() {
    return new HashMap<>();
  }

  /**
   * WTF? .
   *
   * @param clazz foo
   * @return foo
   */
  default IType getDSLTypeForClass(Class<?> clazz) {
    IType dslType = BuiltInType.noType;
    String dslTypeName = TypeBuilder.getDSLTypeName(clazz);
    Symbol dslTypeSymbol = this.getGlobalScope().resolve(dslTypeName);
    if (dslTypeSymbol != Symbol.NULL) {
      dslType = (IType) dslTypeSymbol;
    }
    return dslType;
  }

  /**
   * WTF? .
   *
   * @param name foo
   * @return foo
   */
  default Symbol resolveInGlobalScope(String name) {
    return this.getGlobalScope().resolve(name);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  RuntimeObjectTranslator getRuntimeObjectTranslator();
}
