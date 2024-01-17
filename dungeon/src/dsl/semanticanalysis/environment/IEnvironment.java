package dsl.semanticanalysis.environment;

import dsl.runtime.interop.RuntimeObjectTranslator;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IEnvironment {

  TypeBuilder getTypeBuilder();

  /**
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

  void addFileScope(FileScope fileScope);

  FileScope getFileScope(Path file);

  /**
   * The {@link FileScope} relating to "no file" needs the {@link IEnvironment}-specific global
   * Scope as a parent, so we define it here
   *
   * @return
   */
  FileScope getNullFileScope();

  HashMap<Path, FileScope> getFileScopes();

  /**
   * @return all available function definitions
   */
  default Symbol[] getFunctions() {
    return new Symbol[0];
  }

  // TODO: needs to be extended to handle files
  /**
   * @param types AggregateTypes to load into the environment
   */
  default void loadTypes(IType... types) {}

  /**
   * @param types AggregateTypes to load into the environment
   */
  default void loadTypes(List<IType> types) {}

  /**
   * @param functionDefinitions FunctionSymbols to load into the environment
   */
  default void loadFunctions(ScopedSymbol... functionDefinitions) {}

  /**
   * @param functionDefinitions FunctionSymbols to load into the environment
   */
  default void loadFunctions(List<ScopedSymbol> functionDefinitions) {}

  /**
   * @return symbol table of this environment
   */
  SymbolTable getSymbolTable();

  /**
   * @return global scope of this environment
   */
  IScope getGlobalScope();

  default HashMap<Type, IType> javaTypeToDSLTypeMap() {
    return new HashMap<>();
  }

  default IType getDSLTypeForClass(Class<?> clazz) {
    IType dslType = BuiltInType.noType;
    String dslTypeName = TypeBuilder.getDSLTypeName(clazz);
    Symbol dslTypeSymbol = this.getGlobalScope().resolve(dslTypeName);
    if (dslTypeSymbol != Symbol.NULL) {
      dslType = (IType) dslTypeSymbol;
    }
    return dslType;
  }

  default Symbol resolveInGlobalScope(String name) {
    return this.getGlobalScope().resolve(name);
  }

  RuntimeObjectTranslator getRuntimeObjectTranslator();

  Path defaultRelLibPath = Paths.get("dungeon/assets/scripts/lib");
  String defaultScenarioSubDirName = "scenario";

  default Path relLibPath() {
    return defaultRelLibPath;
  }

  default String scenarioSubDirName() {
    return defaultScenarioSubDirName;
  }

  default Path relScenarioPath() {
    return Paths.get(relLibPath() + "/" + scenarioSubDirName());
  }

  default Path libPath() {
    return relLibPath().toAbsolutePath();
  }

  default Path scenarioPath() {
    return relScenarioPath().toAbsolutePath();
  }
}
