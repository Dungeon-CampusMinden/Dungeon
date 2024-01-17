package dsl.runtime.environment;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.interop.RuntimeObjectTranslator;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.value.PrototypeValue;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.instantiation.TypeInstantiator;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;

// this extends the normal IEnvironment definition by storing prototypes
// which are basically evaluated type definitions (of game objects)
public class RuntimeEnvironment implements IEnvironment {
  private final SymbolTable symbolTable;
  private final HashMap<String, Symbol> functions;
  private final HashMap<String, PrototypeValue> prototypes;
  protected HashMap<Path, FileScope> fileScopes = new HashMap<>();
  private final FileScope entryPointFileScope;
  private final FileScope nullFileScope;
  private final HashMap<Type, IType> javaTypeToDSLType;
  private final RuntimeObjectTranslator runtimeObjectTranslator;
  private final TypeBuilder typeBuilder;
  private final TypeInstantiator typeInstantiator;

  public RuntimeObjectTranslator getRuntimeObjectTranslator() {
    return runtimeObjectTranslator;
  }

  /**
   * Constructor. Create new runtime environment from an existing environment and add all type
   * definitions to the stored types.
   *
   * @param other the other environment to create a new RuntimeEnvironment from
   */
  public RuntimeEnvironment(
      IEnvironment other, DSLInterpreter interpreter, FileScope entryPointFileScope) {
    this.symbolTable = other.getSymbolTable();
    this.typeBuilder = other.getTypeBuilder();

    var functions = other.getFunctions();
    this.functions = new HashMap<>();
    for (var function : functions) {
      this.functions.put(function.getName(), function);
    }

    this.prototypes = new HashMap<>();

    this.javaTypeToDSLType = other.javaTypeToDSLTypeMap();

    this.runtimeObjectTranslator = other.getRuntimeObjectTranslator();
    this.typeInstantiator = new TypeInstantiator(interpreter);
    this.fileScopes = other.getFileScopes();
    this.entryPointFileScope = entryPointFileScope;
    this.nullFileScope = other.getNullFileScope();
  }

  public FileScope entryPointFileScope() {
    return entryPointFileScope;
  }

  /**
   * Lookup a {@link PrototypeValue} with name
   *
   * @param name the name of the Prototype to lookup
   * @return the Prototype with the passed name or Prototype.NONE
   */
  public PrototypeValue lookupPrototype(String name) {
    return this.prototypes.getOrDefault(name, PrototypeValue.NONE);
  }

  /**
   * Add new {@link PrototypeValue}
   *
   * @param prototype the new Prototype
   * @return true on success, false otherwise
   */
  public boolean addPrototype(PrototypeValue prototype) {
    if (this.prototypes.containsKey(prototype.getName())) {
      return false;
    } else {
      this.prototypes.put(prototype.getName(), prototype);
      return true;
    }
  }

  @Override
  public TypeBuilder getTypeBuilder() {
    return this.typeBuilder;
  }

  @Override
  public void addFileScope(FileScope fileScope) {}

  @Override
  public FileScope getFileScope(Path file) {
    FileScope scope = this.fileScopes.get(file);
    if (scope == null) {
      scope = this.nullFileScope;
    }
    return scope;
  }

  @Override
  public FileScope getNullFileScope() {
    return this.nullFileScope;
  }

  @Override
  public HashMap<Path, FileScope> getFileScopes() {
    return this.fileScopes;
  }

  @Override
  public SymbolTable getSymbolTable() {
    return this.symbolTable;
  }

  @Override
  public IScope getGlobalScope() {
    return this.symbolTable.globalScope();
  }

  @Override
  public HashMap<Type, IType> javaTypeToDSLTypeMap() {
    return this.javaTypeToDSLType;
  }

  public Object translateRuntimeObject(Object object, IMemorySpace parentMemorySpace) {
    return this.runtimeObjectTranslator.translateRuntimeObject(object, parentMemorySpace, this);
  }

  public Object translateRuntimeObject(
      Object object, IMemorySpace parentMemorySpace, IType targetType) {
    return this.runtimeObjectTranslator.translateRuntimeObject(
        object, parentMemorySpace, this, targetType);
  }

  public TypeInstantiator getTypeInstantiator() {
    return this.typeInstantiator;
  }
}
