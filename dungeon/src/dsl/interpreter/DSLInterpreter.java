package dsl.interpreter;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import dsl.interpreter.taskgraph.Interpreter;
import dsl.parser.DungeonASTConverter;
import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.memoryspace.EncapsulatedObject;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.memoryspace.MemorySpace;
import dsl.runtime.value.*;
import dsl.semanticanalysis.*;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.PropertySymbol;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.callbackadapter.CallbackAdapter;
import dsl.semanticanalysis.typesystem.instantiation.TypeInstantiator;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import entrypoint.DSLEntryPoint;
import entrypoint.DSLFileLoader;
import entrypoint.DungeonConfig;
import entrypoint.ParsedFile;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import task.Task;
import task.dslinterop.DSLAssignTask;
import task.tasktype.Element;
import task.tasktype.Quiz;

// TODO: specify EXACT semantics of value copying and setting

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {
  private RuntimeEnvironment environment;
  private final ArrayDeque<IMemorySpace> memoryStack;
  private final ArrayDeque<IMemorySpace> fileMemoryStack;
  private final ArrayDeque<IMemorySpace> instanceMemoryStack;
  private final HashMap<FileScope, IMemorySpace> fileScopeToMemorySpace;
  private IMemorySpace globalSpace;

  private SymbolTable symbolTable() {
    return environment.getSymbolTable();
  }

  public IMemorySpace getCurrentMemorySpace() {
    return this.memoryStack.peek();
  }

  public IMemorySpace getCurrentInstanceMemorySpace() {
    return this.instanceMemoryStack.peek();
  }

  private final ArrayDeque<Node> statementStack;

  private static final String RETURN_VALUE_NAME = "$return_value$";

  private final ScenarioBuilderStorage scenarioBuilderStorage;

  /** Constructor. */
  public DSLInterpreter() {
    memoryStack = new ArrayDeque<>();
    fileMemoryStack = new ArrayDeque<>();
    instanceMemoryStack = new ArrayDeque<>();
    globalSpace = new MemorySpace();
    statementStack = new ArrayDeque<>();
    scenarioBuilderStorage = new ScenarioBuilderStorage();
    fileScopeToMemorySpace = new HashMap<>();
    memoryStack.push(globalSpace);
  }

  /**
   * Set the execution context for the DSLInterpreter (load the memoryspace associated with the
   * passed file with {@link Path}
   *
   * @param filePath the path of the file to set as context
   */
  public void setContextFileByPath(Path filePath) {
    IScope scope = this.environment.getFileScope(filePath);
    if (scope.equals(Scope.NULL)) {
      throw new RuntimeException(
          "No file scope associated with the passed filePath '" + filePath + "'");
    }
    FileScope fileScope = (FileScope) scope;
    setContextFileScope(fileScope);
  }

  /**
   * Set the execution context for the DSLInterpreter (load the memoryspace associated with the
   * passed {@link FileScope})
   *
   * @param fileScope the fileScope of the file to set as context
   */
  public void setContextFileScope(FileScope fileScope) {
    IMemorySpace ms = this.fileScopeToMemorySpace.get(fileScope);
    this.fileMemoryStack.push(ms);
    this.memoryStack.push(ms);
  }

  /**
   * Pops all file memory spaces and all memoryspaces on memory stack until only the global space
   * remains.
   */
  public void resetFileContext() {
    this.fileMemoryStack.clear();
    while (this.memoryStack.peek() != null && !this.memoryStack.peek().equals(globalSpace)) {
      this.memoryStack.pop();
    }
  }

  /**
   * Create a {@link DungeonConfig} instance for given {@link DSLEntryPoint}, this will reset the
   * environment of this {@link DSLInterpreter}
   *
   * @param entryPoint the {@link DSLEntryPoint} to interpret.
   * @return the interpreted {@link DungeonConfig}.
   */
  public DungeonConfig interpretEntryPoint(DSLEntryPoint entryPoint) {
    // TODO: could add information about the file being loaded from a jar file
    var environment = new GameEnvironment();
    return interpretEntryPoint(entryPoint, environment);
  }

  /**
   * Create a {@link DungeonConfig} instance for given {@link DSLEntryPoint}, this will reset the
   * environment of this {@link DSLInterpreter}
   *
   * @param entryPoint the {@link DSLEntryPoint} to interpret.
   * @param environment the {@link IEnvironment} to use
   * @return the interpreted {@link DungeonConfig}.
   */
  public DungeonConfig interpretEntryPoint(DSLEntryPoint entryPoint, IEnvironment environment) {
    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
    semanticAnalyzer.setup(environment);

    // TODO: scan lib path (hacky)..
    File libraryPath = new File(environment.libPath().toString());

    if (libraryPath.exists() && libraryPath.isDirectory()) {
      FileFilter scenarioDirFilter =
          file -> file.isDirectory() && file.getName().equals(environment.scenarioSubDirName());
      var optScenarioDir = Arrays.stream(libraryPath.listFiles(scenarioDirFilter)).findFirst();
      if (optScenarioDir.isPresent()) {
        FileFilter scenarioFileFilter = file -> file.isFile() && file.getPath().endsWith(".dng");
        var scenarioDir = optScenarioDir.get();
        var scenarioFiles = scenarioDir.listFiles(scenarioFileFilter);

        // add all scenario files up front for semantic analysis
        // all other files will be loaded from the lib-directory as needed
        for (File scenarioFile : scenarioFiles) {
          var filePath = scenarioFile.toPath();
          String content = DSLFileLoader.fileToString(filePath);
          var programAST = DungeonASTConverter.getProgramAST(content);
          ParsedFile parsedFile = new ParsedFile(filePath, programAST);

          environment.addFileScope(new FileScope(parsedFile, environment.getGlobalScope()));
          semanticAnalyzer.walk(parsedFile);
        }
      }
    }

    var result = semanticAnalyzer.walk(entryPoint.file());

    // at this point, all the symbolic and semantic data must be present in the environment
    initializeRuntime(environment, entryPoint.file().filePath());

    // scan the entrypoint file (the main .dng file) for scenario builder functions
    scanFileForScenarioBuilders(entryPoint.file().filePath());

    return generateQuestConfig(entryPoint.configDefinitionNode(), entryPoint.file());
  }

  /**
   * @return the runtime environment of the DSLInterpreter
   */
  public RuntimeEnvironment getRuntimeEnvironment() {
    return this.environment;
  }

  /**
   * @return the global memory space of the DSLInterpreter
   */
  public IMemorySpace getGlobalMemorySpace() {
    return this.globalSpace;
  }

  public IMemorySpace getEntryPointFileMemorySpace() {
    var fs = this.environment.entryPointFileScope();
    return this.fileScopeToMemorySpace.get(fs);
  }

  // region prototypes

  /**
   * Creates {@link PrototypeValue} instances for all `entity_type` and `item_type` definitions in
   * the global scope of the passed {@link IEnvironment}.
   *
   * @param environment the {@link IEnvironment} in which's global scope to search for prototype
   *     definitions.
   */
  public void createPrototypes(IEnvironment environment, IScope scope) {
    createGameObjectPrototypes(environment, scope);
    createItemPrototypes(environment, scope);
  }

  /**
   * Iterates over all types in the passed IEnvironment and creates a {@link PrototypeValue} for any
   * game object definition, which was defined by the user
   *
   * @param environment the environment to check for game object definitions
   */
  public void createGameObjectPrototypes(IEnvironment environment, IScope scope) {
    // TODO: needs to be scoped...

    // iterate over all types
    var types =
        scope.getSymbols().stream().filter(s -> s instanceof IType).map(s -> (IType) s).toList();
    for (var type : types) {
      if (type.getTypeKind().equals(IType.Kind.Aggregate)) {
        // if the type has a creation node, it is user defined, and we need to
        // create a prototype for it
        var creationAstNode = symbolTable().getCreationAstNode((Symbol) type);
        if (creationAstNode.type.equals(Node.Type.PrototypeDefinition)) {
          var prototype = new PrototypeValue(PrototypeValue.PROTOTYPE, (AggregateType) type);

          var gameObjDefNode = (PrototypeDefinitionNode) creationAstNode;
          for (var node : gameObjDefNode.getComponentDefinitionNodes()) {
            // add new component prototype to the enclosing game object prototype
            AggregateValueDefinitionNode compDefNode = (AggregateValueDefinitionNode) node;
            var componentPrototype = createComponentPrototype(compDefNode);
            prototype.addDefaultValue(compDefNode.getIdName(), componentPrototype);
          }
          // needs to be added to file-memoryspace, not the environment
          // this.environment.addPrototype(prototype);
          // TODO: needs to use current file-memoryspace
          this.getCurrentMemorySpace().bindValue(prototype.getName(), prototype);
          // this.getGlobalMemorySpace().bindValue(prototype.getName(), prototype);
        }
      }
    }
  }

  /**
   * Create {@link PrototypeValue} instances for {@link ItemPrototypeDefinitionNode}s in the global
   * scope of passed {@link IEnvironment}. The created prototypes will be registered in the {@link
   * RuntimeEnvironment} of this {@link DSLInterpreter} and is stored as a {@link Value} in the
   * global {@link IMemorySpace} of the interpreter.
   *
   * @param environment the {@link IEnvironment} to search for item prototype definitions
   */
  public void createItemPrototypes(IEnvironment environment, IScope scope) {
    // iterate over all types
    var types =
        scope.getSymbols().stream().filter(s -> s instanceof IType).map(s -> (IType) s).toList();
    for (var type : types) {
      if (type.getTypeKind().equals(IType.Kind.Aggregate)) {
        // if the type has a creation node, it is user defined, and we need to
        // create a prototype for it
        var creationAstNode = symbolTable().getCreationAstNode((Symbol) type);
        if (creationAstNode.type.equals(Node.Type.ItemPrototypeDefinition)) {
          var prototype = createItemPrototype((ItemPrototypeDefinitionNode) creationAstNode);

          // this.environment.addPrototype(prototype);
          // this.getGlobalMemorySpace().bindValue(prototype.getName(), prototype);
          this.getCurrentMemorySpace().bindValue(prototype.getName(), prototype);
        }
      }
    }
  }

  private PrototypeValue createItemPrototype(ItemPrototypeDefinitionNode node) {
    var itemPrototypeDefinitionSymbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
    assert itemPrototypeDefinitionSymbol instanceof AggregateType;
    AggregateType itemType = (AggregateType) itemPrototypeDefinitionSymbol;

    // the Prototype for a component does only live inside the
    // datatype definition, because it is part of the definition
    // evaluate rhs and store the value in the member of
    // the prototype
    AggregateType questItemType =
        (AggregateType) this.environment.resolveInGlobalScope("quest_item");
    PrototypeValue itemPrototype = new PrototypeValue(PrototypeValue.ITEM_PROTOTYPE, itemType);
    for (var propDef : node.getPropertyDefinitionNodes()) {
      var propertyDefNode = (PropertyDefNode) propDef;
      var rhsValue = (Value) propertyDefNode.getStmtNode().accept(this);

      // get type of lhs (the assignee)
      var propertyName = propertyDefNode.getIdName();
      Symbol propertySymbol = symbolTable().getSymbolsForAstNode(propDef).get(0);
      if (propertySymbol.equals(Symbol.NULL)) {
        throw new RuntimeException(
            "Property of name '"
                + propertyName
                + "' cannot be resolved in type '"
                + questItemType.getName()
                + "'");
      }
      var propertiesType = propertySymbol.getDataType();

      // clone value
      Value value = (Value) rhsValue.clone();

      // promote value to property's datatype
      // TODO: typechecking must be performed before this
      value.setDataType((IType) propertiesType);

      // indicate, that the value is "dirty", which means it was set
      // explicitly and needs to be set in the java object corresponding
      // to the component
      value.setDirty();

      var valueName = propertyDefNode.getIdName();
      itemPrototype.addDefaultValue(valueName, value);
    }
    return itemPrototype;
  }

  private PrototypeValue createComponentPrototype(AggregateValueDefinitionNode node) {
    var componentSymbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
    assert componentSymbol.getDataType() instanceof AggregateType;

    // the Prototype for a component does only live inside the
    // datatype definition, because it is part of the definition
    // evaluate rhs and store the value in the member of
    // the prototype
    AggregateType prototypesType = (AggregateType) componentSymbol.getDataType();
    PrototypeValue componentPrototype =
        new PrototypeValue(PrototypeValue.PROTOTYPE, prototypesType);
    for (var propDef : node.getPropertyDefinitionNodes()) {
      var propertyDefNode = (PropertyDefNode) propDef;
      var rhsValue = (Value) propertyDefNode.getStmtNode().accept(this);

      // get type of lhs (the assignee)
      var propertyName = propertyDefNode.getIdName();
      Symbol propertySymbol = prototypesType.resolve(propertyName);
      if (propertySymbol.equals(Symbol.NULL)) {
        throw new RuntimeException(
            "Property of name '"
                + propertyName
                + "' cannot be resolved in type '"
                + prototypesType.getName()
                + "'");
      }
      var propertiesType = propertySymbol.getDataType();

      // clone value
      Value value = (Value) rhsValue.clone();

      // promote value to property's datatype
      // TODO: typechecking must be performed before this
      value.setDataType((IType) propertiesType);

      // indicate, that the value is "dirty", which means it was set
      // explicitly and needs to be set in the java object corresponding
      // to the component
      value.setDirty();

      var valueName = propertyDefNode.getIdName();
      componentPrototype.addDefaultValue(valueName, value);
    }
    return componentPrototype;
  }

  // endregion

  protected void initializeScenarioBuilderStorage() {
    this.scenarioBuilderStorage.initializeScenarioBuilderStorage(this.environment);
  }

  protected Symbol getScenarioBuilderReturnType() {
    return this.environment.getGlobalScope().resolve("entity<><>");
  }

  protected void scanFileForScenarioBuilders(Path path) {
    IScope fileScope = this.environment.getFileScope(path);
    if (!fileScope.equals(Scope.NULL)) {
      scanScopeForScenarioBuilders(fileScope);
    }
  }

  protected void scanScopeForScenarioBuilders(IScope scope) {
    var symbols = scope.getSymbols();
    Set<IType> taskTypes = this.scenarioBuilderStorage.getTypesWithStorage();
    Symbol returnTypeSymbol = getScenarioBuilderReturnType();
    if (returnTypeSymbol == Symbol.NULL) {
      throw new RuntimeException("Cannot build return type of scenario builders!");
    }
    IType returnType = (IType) returnTypeSymbol;
    symbols.stream()
        .filter(
            symbol -> {
              if (symbol instanceof ICallable callable) {
                FunctionType functionType = callable.getFunctionType();
                if (!functionType.getReturnType().equals(returnType)) {
                  return false;
                }
                if (functionType.getParameterTypes().size() != 1) {
                  return false;
                }
                IType parameterType = functionType.getParameterTypes().get(0);
                if (!taskTypes.contains(parameterType)) {
                  return false;
                }
                return true;
              }
              return false;
            })
        .map(symbol -> (ICallable) symbol)
        .forEach(this.scenarioBuilderStorage::storeScenarioBuilder);
  }

  /**
   * Execute a scenario builder method for the given {@link Task}. The {@link DSLInterpreter} will
   * lookup a random scenario builder method in its internal {@link ScenarioBuilderStorage}
   * corresponding to the {@link Class} of the passed {@link Task}.
   *
   * @param task The {@link Task} to execute a scenario builder method for.
   * @return An {@link Optional} containing the Java-Object which was instantiated from the return
   *     value of the scenario builder. If no custom {@link IEnvironment} implementation apart from
   *     {@link GameEnvironment} is used (this is the default case), the content inside the {@link
   *     Optional} will be of type HashSet<HashSet<core.Entity>>. If the execution of the scenario
   *     builder method was unsuccessful or no fitting scenario builder method for the given {@link
   *     Task} could be found, an empty {@link Optional} will be returned.
   */
  public Optional<Object> buildTask(Task task) {
    var taskClass = task.getClass();

    IType type =
        this.environment
            .getTypeBuilder()
            .createDSLTypeForJavaTypeInScope(this.environment.getGlobalScope(), taskClass);
    String typeName = type.getName();

    Symbol potentialTaskType = this.environment.getGlobalScope().resolve(typeName);
    if (potentialTaskType == Symbol.NULL || !(potentialTaskType instanceof IType)) {
      throw new RuntimeException("Not a supported task type!");
    }

    ICallable scenarioBuilder;
    if (task.scenarioBuilderFunction() != null) {
      var cba = (CallbackAdapter) task.scenarioBuilderFunction();
      scenarioBuilder = cba.callable();
    } else {
      Optional<ICallable> optionalScenarioBuilder =
          this.scenarioBuilderStorage.retrieveRandomScenarioBuilderForType(
              (IType) potentialTaskType);
      if (optionalScenarioBuilder.isEmpty()) {
        return Optional.empty();
      }
      scenarioBuilder = optionalScenarioBuilder.get();
    }

    FileScope entryPointFS = this.environment.entryPointFileScope();
    setContextFileScope(entryPointFS);

    Value retValue = (Value) this.callCallableRawParameters(scenarioBuilder, List.of(task));
    var typeInstantiator = this.environment.getTypeInstantiator();

    resetFileContext();

    // create the java representation of the return Value
    return Optional.of(typeInstantiator.instantiate(retValue));
  }

  /**
   * Binds all function definitions, object definitions and data types in a global memory space.
   *
   * @param environment The environment to bind the functions, objects and data types from.
   */
  public void initializeRuntime(IEnvironment environment, Path entryPointFilePath) {
    // reinitialize global memory space
    this.memoryStack.clear();
    this.globalSpace = new MemorySpace();
    this.memoryStack.push(this.globalSpace);

    FileScope fs = environment.getFileScope(entryPointFilePath);
    this.environment = new RuntimeEnvironment(environment, this, fs);

    initializeScenarioBuilderStorage();

    // scan for scenario builders in scenario lib files
    var files = this.environment.getFileScopes().keySet();
    var scenarioFiles =
        files.stream()
            .filter(
                p ->
                    p != null
                        && p.toString().contains(this.environment.relScenarioPath().toString()))
            .toList();

    for (var scenarioFile : scenarioFiles) {
      IScope fileScope = this.environment.getFileScope(scenarioFile);
      scanScopeForScenarioBuilders(fileScope);
    }
  }

  private void evaluateGlobalSymbolsOfScope(IScope scope) {
    // bind all function definition and object definition symbols to values
    // in global memorySpace

    HashMap<Symbol, Value> globalValues = new HashMap<>();
    List<Symbol> globalSymbols = scope.getSymbols();
    for (var symbol : globalSymbols) {
      IType type = symbol.getDataType();
      if (type != null && type.getTypeKind().equals(IType.Kind.FunctionType)) {
        // we don't build global values for functions by default
        continue;
      }
      var value = bindFromSymbol(symbol, memoryStack.peek());
      if (value != Value.NONE) {
        globalValues.put(symbol, value);
      }
    }

    Set<Map.Entry<Symbol, Value>> graphDefinitions = new HashSet<>();
    for (var entry : globalValues.entrySet()) {
      Symbol symbol = entry.getKey();
      if (symbol.getDataType().equals(BuiltInType.graphType)) {
        // store the graph definition; currently, we cannot ensure,
        // that all tasks, which are referenced in the graph, are evaluated
        // at this point -> store the graph definition for later and
        // iterate over all graph definitions afterward, when all other
        // definitions where evaluated -> a 'clean' solution for this problem
        // requires some kind of symbolic execution in order to check for
        // dependencies between different kinds of object definitions in the
        // global scope
        graphDefinitions.add(entry);
        continue;
      }

      // TODO: this is a temporary solution
      String symbolsTypeName = symbol.getDataType().getName();
      if (!symbolsTypeName.equals("quest_config") && !symbolsTypeName.equals("dungeon_config")) {
        Node astNode = symbolTable().getCreationAstNode(symbol);
        if (astNode != Node.NONE) {
          Value valueToAssign = (Value) astNode.accept(this);
          Value assignee = entry.getValue();
          setValue(assignee, valueToAssign);
        }
      }
    }

    // evaluate global graph definitions
    for (var entry : graphDefinitions) {
      Symbol symbol = entry.getKey();
      Node astNode = symbolTable().getCreationAstNode(symbol);
      if (astNode != Node.NONE) {
        Value valueToAssign = (Value) astNode.accept(this);
        Value assignee = entry.getValue();
        setValue(assignee, valueToAssign);
      }
    }
  }

  private Value bindFromSymbol(Symbol symbol, IMemorySpace ms) {
    if (!(symbol instanceof IType) && !(symbol instanceof PropertySymbol)) {
      var value = createDefaultValue(symbol.getDataType());
      ms.bindValue(symbol.getName(), value);
      return value;
    }
    return Value.NONE;
  }

  /**
   * Creates a DSL level instantiation of a type, which means, that all fields of an aggregate type
   * are set to their default value.
   *
   * @param type
   * @return
   */
  public Value createDefaultValue(IType type) {
    if (type == null) {
      System.out.println("Tried to create default value for null type");
      return Value.NONE;
    }
    if (type.getTypeKind().equals(IType.Kind.Basic)) {
      if (type.equals(PrototypeValue.PROTOTYPE)) {
        return new PrototypeValue(PrototypeValue.PROTOTYPE, null);
      } else if (type.equals(PrototypeValue.ITEM_PROTOTYPE)) {
        return new PrototypeValue(PrototypeValue.ITEM_PROTOTYPE, null);
      } else {
        Object internalValue = Value.getDefaultValue(type);
        return new Value(type, internalValue);
      }
    } else if (type.getTypeKind().equals(IType.Kind.Aggregate)
        || type.getTypeKind().equals(IType.Kind.AggregateAdapted)) {
      AggregateValue value = new AggregateValue(type, getCurrentMemorySpace());

      this.memoryStack.push(value.getMemorySpace());
      for (var member : ((AggregateType) type).getSymbols()) {
        bindFromSymbol(member, memoryStack.peek());
      }
      this.memoryStack.pop();

      return value;
    } else if (type.getTypeKind().equals(IType.Kind.ListType)) {
      return new ListValue((ListType) type);
    } else if (type.getTypeKind().equals(IType.Kind.SetType)) {
      return new SetValue((SetType) type);
    } else if (type.getTypeKind().equals(IType.Kind.MapType)) {
      return new MapValue((MapType) type);
    } else if (type.getTypeKind().equals(IType.Kind.EnumType)) {
      return new EnumValue((EnumType) type, null);
    } else if (type.getTypeKind().equals(IType.Kind.FunctionType)) {
      return (Value) FunctionValue.NONE.clone();
    }
    return Value.NONE;
  }

  /**
   * Parse the config script and return the questConfig object, which serves as an entry point for
   * further evaluation and interpretation.
   *
   * @param configScript The script (in the DungeonDSL) to parse
   * @param environment The environment to use
   * @return The first questConfig object found in the configScript
   */
  public Object getQuestConfig(String configScript, IEnvironment environment) {
    // TODO: make relLibPath settable (or make the Environment settable)
    var stream = CharStreams.fromString(configScript);
    var lexer = new DungeonDSLLexer(stream);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new DungeonDSLParser(tokenStream);
    var programParseTree = parser.program();

    DungeonASTConverter astConverter = new DungeonASTConverter();
    var programAST = astConverter.walk(programParseTree);

    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
    semanticAnalyzer.setup(environment);
    var result = semanticAnalyzer.walk(programAST);
    ParsedFile pf = semanticAnalyzer.latestParsedFile;

    initializeRuntime(environment, pf.filePath());
    scanFileForScenarioBuilders(pf.filePath());

    Value questConfigValue = (Value) generateQuestConfig(programAST, pf);
    return questConfigValue.getInternalValue();
  }

  /**
   * Parse the config script and return the questConfig object, which serves as an entry point for
   * further evaluation and interpretation.
   *
   * @param configScript The script (in the DungeonDSL) to parse
   * @return The first questConfig object found in the configScript
   */
  public Object getQuestConfig(String configScript) {
    return this.getQuestConfig(configScript, new GameEnvironment());
  }

  /**
   * @param programAST The AST of the DSL program to generate a quest config object from
   * @return the object, which represents the quest config of the passed DSL program. The type of
   *     this object depends on the Class, which is set up as the 'quest_config' type in the {@link
   *     IEnvironment} used by the DSLInterpreter (set by initializeRuntime)
   */
  public Object generateQuestConfig(Node programAST, ParsedFile parsedFile) {
    IScope fs = this.environment.getFileScope(parsedFile.filePath());

    var filesMemorySpace = initializeFileMemorySpace((FileScope) fs);
    this.memoryStack.push(filesMemorySpace);
    this.fileMemoryStack.push(filesMemorySpace);

    Object questConfigObject = Value.NONE;
    // find quest_config definition
    for (var node : programAST.getChildren()) {
      if (node.type == Node.Type.ObjectDefinition) {
        var objDefNode = (ObjectDefNode) node;
        if (objDefNode.getTypeSpecifierName().equals("quest_config")
            || objDefNode.getTypeSpecifierName().equals("dungeon_config")) {
          questConfigObject = objDefNode.accept(this);
          break;
        }
      }
    }

    this.memoryStack.pop();
    this.fileMemoryStack.pop();

    return questConfigObject;
  }

  public IMemorySpace getFileMemorySpace(FileScope fs) {
    return this.fileScopeToMemorySpace.get(fs);
  }

  protected IMemorySpace initializeFileMemorySpace(FileScope fs) {
    IMemorySpace filesMemorySpace;
    if (this.fileScopeToMemorySpace.containsKey(fs)) {
      // if the fileScope is already in the hashmap, we assume, it is initialized
      filesMemorySpace = this.fileScopeToMemorySpace.get(fs);
    } else {
      filesMemorySpace = new MemorySpace(this.globalSpace);
      this.fileScopeToMemorySpace.put(fs, filesMemorySpace);

      this.memoryStack.push(filesMemorySpace);
      evaluateGlobalSymbolsOfScope(fs);
      createPrototypes(this.environment, fs);
      this.memoryStack.pop();
    }
    return filesMemorySpace;
  }

  protected DungeonConfig generateQuestConfig(ObjectDefNode configDefinitionNode, ParsedFile pf) {
    // TODO: this needs to be done for every "entry point into the interpreter"

    IScope scope = this.environment.getFileScope(pf.filePath());
    if (scope.equals(Scope.NULL)) {
      throw new RuntimeException("Scope for file '" + pf.filePath() + "' is null");
    }

    FileScope fs = (FileScope) scope;

    IMemorySpace fileMemorySpace = initializeFileMemorySpace(fs);
    this.memoryStack.push(fileMemorySpace);
    this.fileMemoryStack.push(fileMemorySpace);

    DungeonConfig dungeonConfig = null;
    Value configValue = (Value) configDefinitionNode.accept(this);
    Object config = configValue.getInternalValue();
    if (config instanceof DungeonConfig) {
      dungeonConfig = (DungeonConfig) config;
      if (dungeonConfig.displayName().isEmpty()) {
        String objectName = configDefinitionNode.getIdName();
        dungeonConfig = new DungeonConfig(dungeonConfig.dependencyGraph(), objectName);
      }
    }

    this.memoryStack.pop();
    this.fileMemoryStack.pop();
    return dungeonConfig;
  }

  protected Value instantiateDSLValue(AggregateType type) {
    AggregateValue instance = new AggregateValue(type, getCurrentMemorySpace());

    IMemorySpace memorySpace = instance.getMemorySpace();
    this.memoryStack.push(memorySpace);
    for (var member : type.getSymbols()) {
      // check, if type defines default for member
      var defaultValue = createDefaultValue(member.getDataType());
      memorySpace.bindValue(member.getName(), defaultValue);
    }
    this.memoryStack.pop();

    return instance;
  }

  /**
   * Instantiate a dsl prototype (which is an aggregate type with defaults) as a new Value
   *
   * @param prototype the {@link PrototypeValue} to instantiate
   * @return A new {@link Value} created from the {@link PrototypeValue}
   */
  public Value instantiateDSLValue(PrototypeValue prototype) {
    // create memory space to store the values in
    AggregateValue instance = new AggregateValue(prototype, getCurrentMemorySpace());

    // TODO: how to handle function calls here?
    //  we should evaluate functions as soon as possible, and only allow
    //  functions as objects to be passed to members, which actually expect a
    //  callback function
    IMemorySpace memorySpace = instance.getMemorySpace();
    this.memoryStack.push(memorySpace);
    var internalType = (AggregateType) prototype.getInternalType();
    for (var member : internalType.getSymbols()) {
      // check, if type defines default for member
      var defaultValue = prototype.getDefaultValue(member.getName());
      if (defaultValue instanceof PrototypeValue) {
        defaultValue = instantiateDSLValue((PrototypeValue) defaultValue);
      } else if (!defaultValue.equals(Value.NONE)) {
        // copy value (this is a copy of the DSL-Value, not the internal Object of the
        // value)
        defaultValue = (Value) defaultValue.clone();
      } else {
        // no default value, generate default ourselves
        defaultValue = createDefaultValue(member.getDataType());
      }
      memorySpace.bindValue(member.getName(), defaultValue);
    }
    this.memoryStack.pop();

    return instance;
  }

  public AggregateType getOriginalTypeOfPrototype(PrototypeValue type) {
    IType returnType = type;
    while (returnType instanceof PrototypeValue) {
      returnType = ((PrototypeValue) returnType).getInternalType();
    }
    return (AggregateType) returnType;
  }

  static boolean isBooleanTrue(Value value) {
    var valuesType = value.getDataType();
    var typeKind = valuesType.getTypeKind();

    if (value.equals(Value.NONE)) {
      // NONE = false
      return false;
    } else if (typeKind.equals(IType.Kind.Aggregate)) {
      // if it is empty = false
      return !((AggregateValue) value).isEmpty();
    } else if (typeKind.equals(IType.Kind.EnumType)) {
      // if the internal value is null = false
      return value.getInternalValue() != null;
    } else if (typeKind.equals(IType.Kind.Basic)) {
      // basically check if zero
      return ((BuiltInType) valuesType).asBooleanFunction.run(value);
    } else {
      // in any other case, true
      return true;
    }
  }

  // this is the evaluation side of things
  //
  @Override
  public Object visit(PrototypeDefinitionNode node) {
    // TODO: does this need to be specially treated? could this not be the normal resolving of
    // types?
    // return this.environment.lookupPrototype(node.getIdName());
    boolean b = true;
    return this.getCurrentMemorySpace().resolve(node.getIdName());
  }

  public Object instantiateRuntimeValue(AggregateValue dslValue, AggregateType asType) {
    var typeInstantiator = this.environment.getTypeInstantiator();
    return typeInstantiator.instantiateAsType(dslValue, asType);
  }

  @Override
  public Object visit(ObjectDefNode node) {
    // resolve name of object in memory space
    IMemorySpace ms;
    var objectsValue = getCurrentMemorySpace().resolve(node.getIdName());
    if (objectsValue instanceof AggregateValue) {
      ms = ((AggregateValue) objectsValue).getMemorySpace();
    } else {
      throw new RuntimeException("Defined object is not an aggregate Value");
    }
    if (!(ms instanceof EncapsulatedObject)) {
      memoryStack.push(ms);

      if (objectsValue.getDataType().getName().equals("assign_task")) {
        // create "_" Value
        ms.bindValue("_", new Value(BuiltInType.stringType, DSLAssignTask.EMPTY_ELEMENT_NAME));
      }

      // accept every propertyDefinition
      for (var propDefNode : node.getPropertyDefinitions()) {
        propDefNode.accept(this);
      }

      memoryStack.pop();

      Value nameValue = objectsValue.getMemorySpace().resolve(AggregateType.NAME_SYMBOL_NAME);
      if (nameValue != Value.NONE) {
        Value nameToSet = new Value(BuiltInType.stringType, node.getIdName());
        setValue(nameValue, nameToSet);
      }

      // convert from memorySpace to concrete object
      Object createdObject = createObjectFromValue((AggregateValue) objectsValue);
      EncapsulatedObject encapsulatedObject =
          new EncapsulatedObject(
              createdObject, (AggregateType) objectsValue.getDataType(), this.environment);
      ((AggregateValue) objectsValue).setMemorySpace(encapsulatedObject);
      objectsValue.setInternalValue(createdObject);
    }
    return objectsValue;
  }

  private Object createObjectFromValue(AggregateValue value) {
    TypeInstantiator ti = this.environment.getTypeInstantiator();
    return ti.instantiate(value);
  }

  @Override
  public Object visit(AggregateValueDefinitionNode node) {
    // create instance of dsl data type
    var type = this.symbolTable().globalScope().resolve(node.getIdName());
    assert type instanceof AggregateType;

    var value = (AggregateValue) instantiateDSLValue((AggregateType) type);

    // interpret the property definitions
    this.memoryStack.push(value.getMemorySpace());
    for (var member : node.getPropertyDefinitionNodes()) {
      member.accept(this);
    }
    this.memoryStack.pop();

    return value;
  }

  @Override
  public Object visit(PropertyDefNode node) {
    var propertyName = node.getIdName();
    Value value = (Value) node.getStmtNode().accept(this);
    Value assigneeValue = getCurrentMemorySpace().resolve(propertyName);
    boolean setValue = setValue(assigneeValue, value);
    if (!setValue) {
      // TODO: handle, errormsg
    }
    return null;
  }

  @Override
  public Object visit(NumNode node) {
    return new Value(BuiltInType.intType, node.getValue() /*, -1*/);
  }

  @Override
  public Object visit(DecNumNode node) {
    return new Value(BuiltInType.floatType, node.getValue());
  }

  @Override
  public Object visit(StringNode node) {
    return new Value(BuiltInType.stringType, node.getValue());
  }

  // this is used for resolving object references
  @Override
  public Object visit(IdNode node) {
    var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
    if (symbol instanceof NativeFunction nativeFunction) {
      return new FunctionValue(nativeFunction.getFunctionType(), nativeFunction);
    }
    if (symbol instanceof FunctionSymbol functionSymbol) {
      return new FunctionValue(functionSymbol.getFunctionType(), functionSymbol);
    }
    if (symbol instanceof ImportAggregateTypeSymbol aggregateTypeSymbol) {
      // get file associated memory space of original definition
      AggregateType originalType = aggregateTypeSymbol.originalTypeSymbol();
      IScope originalScope = originalType.getScope();
      assert originalScope instanceof FileScope;
      FileScope originalFileScope = (FileScope) originalScope;

      // at this point, the memory space of the original file could still be uninitialized
      IMemorySpace originalFileMemorySpace;
      if (!this.fileScopeToMemorySpace.containsKey(originalFileScope)) {
        originalFileMemorySpace = initializeFileMemorySpace(originalFileScope);
      } else {
        originalFileMemorySpace = this.fileScopeToMemorySpace.get(originalFileScope);
      }
      return originalFileMemorySpace.resolve(originalType.getName());
    }

    return this.getCurrentMemorySpace().resolve(node.getName(), true);
  }

  @Override
  public Object visit(FuncDefNode node) {
    // return function reference as value
    var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
    return new Value(symbol.getDataType(), symbol);
  }

  @Override
  public Object visit(DotDefNode node) {
    Interpreter dotInterpreter = new Interpreter(this);
    var ms = getGlobalMemorySpace();
    var graph = dotInterpreter.getGraph(node);
    return new Value(BuiltInType.graphType, graph);
  }

  @Override
  public Object visit(StmtBlockNode node) {
    ArrayList<Node> statements = node.getStmts();

    // push scope exit mark
    statementStack.addFirst(new Node(Node.Type.ScopeExitMark));

    // push new MemorySpace on top of memory stack
    MemorySpace ms = new MemorySpace(this.getCurrentMemorySpace());
    this.memoryStack.push(ms);

    // push statements in reverse order onto the statement stack
    // (as execution is done by popping the topmost statement from the stack)
    var iter = statements.listIterator(statements.size());
    while (iter.hasPrevious()) {
      Node stmt = iter.previous();
      statementStack.addFirst(stmt);
    }
    return null;
  }

  @Override
  public Object visit(FuncCallNode node) {
    var funcName = node.getIdName();

    var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);

    if (!(symbol instanceof ICallable callable)) {
      throw new RuntimeException("Symbol for name '" + funcName + "' is not callable!");
    } else {
      // execute function call
      return callCallable(callable, node.getParameters());
    }
  }

  @Override
  public Object visit(ReturnStmtNode node) {
    Value value = (Value) node.getInnerStmtNode().accept(this);

    if (value != Value.NONE) {
      // walk the memorystack, find the first return value
      // and set it according to the evaluated value
      for (var ms : this.memoryStack) {
        Value returnValue = ms.resolve(RETURN_VALUE_NAME);
        if (returnValue != Value.NONE) {
          setValue(returnValue, value);
          break;
        }
      }
    }

    // unroll the statement stack until we find a return mark
    while (statementStack.peek() != null && statementStack.peek().type != Node.Type.ReturnMark) {
      // we still need to clean up the memory stack, if we find a ScopeExitMark
      Node poppedNode = statementStack.pop();
      if (poppedNode.type.equals(Node.Type.ScopeExitMark)) {
        poppedNode.accept(this);
      }
    }

    return null;
  }

  @Override
  public Object visit(ConditionalStmtNodeIf node) {
    Value conditionValue = (Value) node.getCondition().accept(this);
    if (isBooleanTrue(conditionValue)) {
      // if we only got one statement (no block), we need to create a new MemorySpace
      // here
      if (!node.getIfStmt().type.equals(Node.Type.Block)) {
        MemorySpace ms = new MemorySpace(this.getCurrentMemorySpace());
        memoryStack.push(ms);
        statementStack.push(new Node(Node.Type.ScopeExitMark));
      }

      statementStack.addFirst(node.getIfStmt());
    }

    return null;
  }

  @Override
  public Object visit(ConditionalStmtNodeIfElse node) {
    Value conditionValue = (Value) node.getCondition().accept(this);
    if (isBooleanTrue(conditionValue)) {
      // if we only got one statement (no block), we need to create a new MemorySpace
      // here
      if (!node.getIfStmt().type.equals(Node.Type.Block)) {
        MemorySpace ms = new MemorySpace(this.getCurrentMemorySpace());
        memoryStack.push(ms);
        statementStack.push(new Node(Node.Type.ScopeExitMark));
      }
      statementStack.addFirst(node.getIfStmt());
    } else {
      // if we only got one statement (no block), we need to create a new MemorySpace
      // here
      if (!node.getElseStmt().type.equals(Node.Type.Block)) {
        MemorySpace ms = new MemorySpace(this.getCurrentMemorySpace());
        memoryStack.push(ms);
        statementStack.push(new Node(Node.Type.ScopeExitMark));
      }
      statementStack.addFirst(node.getElseStmt());
    }

    return null;
  }

  @Override
  public Object visit(BoolNode node) {
    return new Value(BuiltInType.boolType, node.getValue());
  }

  @Override
  public Object visit(MemberAccessNode node) {
    Node currentNode = node;
    Node lhs;
    Node rhs = Node.NONE;
    IMemorySpace memorySpaceToUse = this.getCurrentMemorySpace();

    Value lhsValue = Value.NONE;
    Value rhsValue = Value.NONE;
    while (currentNode.type.equals(Node.Type.MemberAccess)) {
      lhs = ((MemberAccessNode) currentNode).getLhs();
      rhs = ((MemberAccessNode) currentNode).getRhs();

      Symbol lhsSymbol = symbolTable().getSymbolsForAstNode(lhs).get(0);
      if (lhsSymbol != Symbol.NULL && lhsSymbol instanceof EnumType enumType) {
        Symbol rhsSymbol = symbolTable().getSymbolsForAstNode(rhs).get(0);
        if (rhsSymbol == Symbol.NULL) {
          throw new RuntimeException("Could not find enum variant for Node: " + rhs);
        }
        assert rhsSymbol.getDataType().getTypeKind().equals(IType.Kind.EnumType);
        rhsValue = new EnumValue(enumType, rhsSymbol);
      } else if (lhs.type.equals(Node.Type.Identifier)) {
        String nameToResolve = ((IdNode) lhs).getName();
        lhsValue = memorySpaceToUse.resolve(nameToResolve);
      } else if (lhs.type.equals(Node.Type.FuncCall)) {
        this.instanceMemoryStack.push(memorySpaceToUse);
        lhsValue = (Value) lhs.accept(this);
        this.instanceMemoryStack.pop();
      }

      currentNode = rhs;
      memorySpaceToUse = lhsValue.getMemorySpace();
    }

    if (rhsValue == Value.NONE) {
      // if we arrive here, we have got two options:
      // 1. we resolve an IdNode at the rhs of the MemberAccessNode
      // 2. we resolve an FuncCallNode at the rhs of the MemberAccessNode
      if (rhs.type.equals(Node.Type.Identifier)) {
        this.memoryStack.push(memorySpaceToUse);
        rhsValue = (Value) rhs.accept(this);
        this.memoryStack.pop();
      } else if (rhs.type.equals(Node.Type.FuncCall)) {
        this.instanceMemoryStack.push(memorySpaceToUse);
        rhsValue = (Value) rhs.accept(this);
        this.instanceMemoryStack.pop();
      }
    }

    return rhsValue;
  }

  @Override
  public Object visit(VarDeclNode node) {
    String variableName = node.getVariableName();

    // check, if the current memory space already contains a value of the same name
    Value value = getCurrentMemorySpace().resolve(variableName, false);
    if (!value.equals(Value.NONE)) {
      getCurrentMemorySpace().delete(variableName);
      value = Value.NONE;
    }

    // get variable symbol
    // Node variableIdentifierNode = node.getIdentifier();
    Symbol variableSymbol = symbolTable().getSymbolsForAstNode(node).get(0);
    value = bindFromSymbol(variableSymbol, this.getCurrentMemorySpace());
    if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
        Value rhsValue = (Value)node.getRhs().accept(this);
        setValue(value, rhsValue);
    }
    return value;
  }

  @Override
  public Object visit(LogicOrNode node) {
    Value lhs = (Value) node.getLhs().accept(this);

    // short-circuiting implementation
    boolean lhsBool = isBooleanTrue(lhs);
    if (lhsBool) {
      return new Value(BuiltInType.boolType, true);
    }

    Value rhs = (Value) node.getRhs().accept(this);
    return new Value(BuiltInType.boolType, isBooleanTrue(rhs));
  }

  @Override
  public Object visit(LogicAndNode node) {
    Value lhs = (Value) node.getLhs().accept(this);

    // short-circuiting implementation
    boolean lhsBool = isBooleanTrue(lhs);
    if (!lhsBool) {
      return new Value(BuiltInType.boolType, false);
    }

    Value rhs = (Value) node.getRhs().accept(this);
    return new Value(BuiltInType.boolType, isBooleanTrue(rhs));
  }

  @Override
  public Object visit(EqualityNode node) {
    var lhsValue = (Value) node.getLhs().accept(this);
    var rhsValue = (Value) node.getRhs().accept(this);
    boolean equals = lhsValue.equals(rhsValue);
    return switch (node.getEqualityType()) {
      case equals -> new Value(BuiltInType.boolType, equals);
      case notEquals -> new Value(BuiltInType.boolType, !equals);
    };
  }

  @Override
  public Object visit(ComparisonNode node) {
    Value lhs = (Value) node.getLhs().accept(this);
    Value rhs = (Value) node.getRhs().accept(this);

    assert lhs.getDataType() == rhs.getDataType();
    IType valueType = lhs.getDataType();
    switch (node.getComparisonType()) {
      case lessThan:
        if (valueType.equals(BuiltInType.intType)) {
          Integer lhsInt = (Integer) lhs.getInternalValue();
          Integer rhsInt = (Integer) rhs.getInternalValue();
          boolean comp = lhsInt < rhsInt;
          return new Value(BuiltInType.boolType, comp);
        } else if (valueType.equals(BuiltInType.floatType)) {
          Float lhsFloat = (Float) lhs.getInternalValue();
          Float rhsFloat = (Float) rhs.getInternalValue();
          boolean comp = lhsFloat < rhsFloat;
          return new Value(BuiltInType.boolType, comp);
        } else {
          throw new RuntimeException("Invalid type '" + valueType + "' for comparison!");
        }
      case lessEquals:
        if (valueType.equals(BuiltInType.intType)) {
          Integer lhsInt = (Integer) lhs.getInternalValue();
          Integer rhsInt = (Integer) rhs.getInternalValue();
          boolean comp = lhsInt <= rhsInt;
          return new Value(BuiltInType.boolType, comp);
        } else if (valueType.equals(BuiltInType.floatType)) {
          Float lhsFloat = (Float) lhs.getInternalValue();
          Float rhsFloat = (Float) rhs.getInternalValue();
          boolean comp = lhsFloat <= rhsFloat;
          return new Value(BuiltInType.boolType, comp);
        } else {
          throw new RuntimeException("Invalid type '" + valueType + "' for comparison!");
        }
      case greaterThan:
        if (valueType.equals(BuiltInType.intType)) {
          Integer lhsInt = (Integer) lhs.getInternalValue();
          Integer rhsInt = (Integer) rhs.getInternalValue();
          boolean comp = lhsInt > rhsInt;
          return new Value(BuiltInType.boolType, comp);
        } else if (valueType.equals(BuiltInType.floatType)) {
          Float lhsFloat = (Float) lhs.getInternalValue();
          Float rhsFloat = (Float) rhs.getInternalValue();
          boolean comp = lhsFloat > rhsFloat;
          return new Value(BuiltInType.boolType, comp);
        } else {
          throw new RuntimeException("Invalid type '" + valueType + "' for comparison!");
        }
      case greaterEquals:
        if (valueType.equals(BuiltInType.intType)) {
          Integer lhsInt = (Integer) lhs.getInternalValue();
          Integer rhsInt = (Integer) rhs.getInternalValue();
          boolean comp = lhsInt >= rhsInt;
          return new Value(BuiltInType.boolType, comp);
        } else if (valueType.equals(BuiltInType.floatType)) {
          Float lhsFloat = (Float) lhs.getInternalValue();
          Float rhsFloat = (Float) rhs.getInternalValue();
          boolean comp = lhsFloat >= rhsFloat;
          return new Value(BuiltInType.boolType, comp);
        } else {
          throw new RuntimeException("Invalid type '" + valueType + "' for comparison!");
        }
    }
    return Value.NONE;
  }

  @Override
  public Object visit(TermNode node) {
    Value lhs = (Value) node.getLhs().accept(this);
    Value rhs = (Value) node.getRhs().accept(this);

    assert lhs.getDataType() == rhs.getDataType();
    IType valueType = lhs.getDataType();

    // we just assume, that lhs and rhs have the same datatype, checked in semantic analysis
    switch (node.getTermType()) {
      case plus:
        {
          if (valueType.equals(BuiltInType.intType)) {
            Integer lhsInt = (Integer) lhs.getInternalValue();
            Integer rhsInt = (Integer) rhs.getInternalValue();
            Integer sum = lhsInt + rhsInt;
            return new Value(BuiltInType.intType, sum);
          } else if (valueType.equals(BuiltInType.floatType)) {
            Float lhsFloat = (Float) lhs.getInternalValue();
            Float rhsFloat = (Float) rhs.getInternalValue();
            Float sum = lhsFloat + rhsFloat;
            return new Value(BuiltInType.floatType, sum);
          } else if (valueType.equals(BuiltInType.stringType)) {
            // concatenate strings
            String lhsString = (String) lhs.getInternalValue();
            String rhsString = (String) rhs.getInternalValue();
            String concat = lhsString + rhsString;
            return new Value(BuiltInType.stringType, concat);
          } else {
            throw new RuntimeException("Invalid type '" + valueType + "' for addition!");
          }
        }
      case minus:
        {
          if (valueType.equals(BuiltInType.intType)) {
            Integer lhsInt = (Integer) lhs.getInternalValue();
            Integer rhsInt = (Integer) rhs.getInternalValue();
            Integer sub = lhsInt - rhsInt;
            return new Value(BuiltInType.intType, sub);
          } else if (valueType.equals(BuiltInType.floatType)) {
            Float lhsFloat = (Float) lhs.getInternalValue();
            Float rhsFloat = (Float) rhs.getInternalValue();
            Float sub = lhsFloat - rhsFloat;
            return new Value(BuiltInType.floatType, sub);
          } else {
            throw new RuntimeException("Invalid type '" + valueType + "' for subtraction!");
          }
        }
    }
    return Value.NONE;
  }

  @Override
  public Object visit(FactorNode node) {
    Value lhs = (Value) node.getLhs().accept(this);
    Value rhs = (Value) node.getRhs().accept(this);

    assert lhs.getDataType() == rhs.getDataType();
    IType valueType = lhs.getDataType();

    // we just assume, that lhs and rhs have the same datatype, checked in semantic analysis
    switch (node.getFactorType()) {
      case divide:
        {
          if (valueType.equals(BuiltInType.intType)) {
            Integer lhsInt = (Integer) lhs.getInternalValue();
            Integer rhsInt = (Integer) rhs.getInternalValue();
            Integer div = lhsInt / rhsInt;
            return new Value(BuiltInType.intType, div);
          } else if (valueType.equals(BuiltInType.floatType)) {
            Float lhsFloat = (Float) lhs.getInternalValue();
            Float rhsFloat = (Float) rhs.getInternalValue();
            Float div = lhsFloat / rhsFloat;
            return new Value(BuiltInType.floatType, div);
          } else {
            throw new RuntimeException("Invalid type '" + valueType + "' for division!");
          }
        }
      case multiply:
        {
          if (valueType.equals(BuiltInType.intType)) {
            Integer lhsInt = (Integer) lhs.getInternalValue();
            Integer rhsInt = (Integer) rhs.getInternalValue();
            Integer mul = lhsInt * rhsInt;
            return new Value(BuiltInType.intType, mul);
          } else if (valueType.equals(BuiltInType.floatType)) {
            Float lhsFloat = (Float) lhs.getInternalValue();
            Float rhsFloat = (Float) rhs.getInternalValue();
            Float mul = lhsFloat * rhsFloat;
            return new Value(BuiltInType.floatType, mul);
          } else {
            throw new RuntimeException("Invalid type '" + valueType + "' for multiplication!");
          }
        }
    }
    return Value.NONE;
  }

  @Override
  public Object visit(UnaryNode node) {
    Value innerValue = (Value) node.getInnerNode().accept(this);
    IType valueType = innerValue.getDataType();

    // we just assume, that lhs and rhs have the same datatype, checked in semantic analysis
    switch (node.getUnaryType()) {
      case not:
        {
          boolean invertedBooleanValue = !isBooleanTrue(innerValue);
          return new Value(BuiltInType.boolType, invertedBooleanValue);
        }
      case minus:
        {
          if (valueType.equals(BuiltInType.intType)) {
            Integer internalValue = (Integer) innerValue.getInternalValue();
            Integer res = -internalValue;
            return new Value(BuiltInType.intType, res);
          } else if (valueType.equals(BuiltInType.floatType)) {
            Float internalValue = (Float) innerValue.getInternalValue();
            Float res = -internalValue;
            return new Value(BuiltInType.floatType, res);
          } else {
            throw new RuntimeException("Invalid type '" + valueType + "' for unary minus!");
          }
        }
    }
    return Value.NONE;
  }

  @Override
  public Object visit(AssignmentNode node) {
    Value lhsValue = (Value) node.getLhs().accept(this);
    Value rhsValue = (Value) node.getRhs().accept(this);
    setValue(lhsValue, rhsValue);

    return lhsValue;
  }

  @Override
  public Object visit(ListDefinitionNode node) {
    // collect evaluated Values in an ArrayList
    ArrayList<Value> entries = new ArrayList<>(node.getEntries().size());
    for (Node expressionNode : node.getEntries()) {
      Value value = (Value) expressionNode.accept(this);
      entries.add(value);
    }

    // TODO: this is a temporary solution, once Typechecking is implemented, the type would be
    //  inferred before this
    IType entryType = BuiltInType.noType;
    if (entries.size() != 0) {
      entryType = entries.get(0).getDataType();
    }
    // create list type
    String listTypeName = ListType.getListTypeName(entryType);
    // TODO: list_type is not properly put in environment beforehand, requires changing of
    //  environment<->typebuilder interaction
    Symbol listType = this.environment.resolveInGlobalScope(listTypeName);
    if (listType == Symbol.NULL) {
      listType = new ListType(entryType, this.environment.getGlobalScope());
      this.environment.getGlobalScope().bind(listType);
    }
    ListValue listValue = new ListValue((ListType) listType);
    for (Value listEntry : entries) {
      listValue.addValue(listEntry);
    }
    return listValue;
  }

  @Override
  public Object visit(SetDefinitionNode node) {
    // collect evaluated Values in an ArrayList
    ArrayList<Value> entries = new ArrayList<>(node.getEntries().size());
    for (Node expressionNode : node.getEntries()) {
      Value value = (Value) expressionNode.accept(this);
      entries.add(value);
    }

    // TODO: this is a temporary solution, once Typechecking is implemented, the type would be
    //  inferred before this
    IType entryType = BuiltInType.noType;
    if (entries.size() != 0) {
      entryType = entries.get(0).getDataType();
    }

    // create list type
    String setTypeName = SetType.getSetTypeName(entryType);
    // TODO: list_type is not properly put in environment beforehand, requires changing of
    //  environment<->typebuilder interaction
    Symbol setType = this.environment.resolveInGlobalScope(setTypeName);
    if (setType == Symbol.NULL) {
      setType = new SetType(entryType, this.environment.getGlobalScope());
      this.environment.getGlobalScope().bind(setType);
    }
    SetValue setValue = new SetValue((SetType) setType);
    for (Value setEntry : entries) {
      setValue.addValue(setEntry);
    }
    return setValue;
  }

  @Override
  public Object visit(Node node) {
    switch (node.type) {
      case ScopeExitMark:
        this.memoryStack.pop();
      case GroupedExpression:
        return node.getChild(0).accept(this);
      default:
        break;
    }
    return null;
  }

  // region value-setting
  private void setAggregateValue(AggregateValue aggregateAssignee, Value valueToAssign) {
    AggregateValue aggregateValueToAssign;
    if (!(valueToAssign instanceof AggregateValue)) {
      // if the value to assign is not an aggregate value, we might have
      // the case, where we want to assign a basic Value to a Content object

      IType assigneesType = aggregateAssignee.getDataType();
      Object objectToEncapsulate;
      if (assigneesType.getName().equals("content")) {
        // TODO: this is a temporary solution for "casting" the value to a content
        //  once typechecking is implemented, this will be refactored

        String stringValue = valueToAssign.getInternalValue().toString();
        objectToEncapsulate = new Quiz.Content(stringValue);
      } else if (assigneesType.getName().equals("element")) {
        String stringValue = valueToAssign.getInternalValue().toString();
        objectToEncapsulate = new Element<>(stringValue);
      } else {
        throw new RuntimeException(
            "Can't assign Value of type "
                + valueToAssign.getDataType()
                + " to Value of "
                + assigneesType);
      }

      // do the encapsulation
      EncapsulatedObject encapsulatedObject =
          new EncapsulatedObject(
              objectToEncapsulate, (AggregateType) assigneesType, this.environment);
      aggregateValueToAssign =
          AggregateValue.fromEncapsulatedObject(this.getCurrentMemorySpace(), encapsulatedObject);
      aggregateAssignee.setFrom(aggregateValueToAssign);
    } else {
      aggregateValueToAssign = (AggregateValue) valueToAssign;
    }
    aggregateAssignee.setFrom(aggregateValueToAssign);
  }

  private boolean setSetValue(SetValue assignee, Value valueToAssign) {
    if (!(valueToAssign instanceof SetValue setValueToAssign)) {
      throw new RuntimeException(
          "Can't assign value " + valueToAssign + " to SetValue, it is not a SetValue itself!");
    }

    IType assigneeEntryType = assignee.getDataType().getElementType();
    IType newValueEntryType = setValueToAssign.getDataType().getElementType();
    if (assigneeEntryType.equals(newValueEntryType)) {
      return assignee.setFrom(setValueToAssign);
    } else {
      assignee.clearSet();
      // TODO: this should not be done implicitly but done specifically, if the
      //  semantic analysis leads to the conclusion that the types are different
      Set<Value> valuesToAdd = setValueToAssign.getValues();
      for (Value valueToAdd : valuesToAdd) {
        Value entryAssigneeValue = createDefaultValue(assigneeEntryType);

        // we cannot directly set the entryValueToAssign, because we potentially
        // have to do type conversions (convert a String into a Content-Object)
        setValue(entryAssigneeValue, valueToAdd);

        assignee.addValue(entryAssigneeValue);
      }
      return true;
    }
  }

  private boolean setMapValue(MapValue assignee, Value valueToAssign) {
    if (!(valueToAssign instanceof MapValue mapValueToAssign)) {
      throw new RuntimeException(
          "Can't assign value " + valueToAssign + " to MapValue, it is not a MapValue itself!");
    }

    IType assigneeKeyType = assignee.getDataType().getKeyType();
    IType assigneeEntryType = assignee.getDataType().getElementType();
    IType valueKeyType = mapValueToAssign.getDataType().getKeyType();
    IType valueEntryType = mapValueToAssign.getDataType().getElementType();

    if (assigneeKeyType.equals(valueKeyType) && assigneeEntryType.equals(valueEntryType)) {
      return assignee.setFrom(mapValueToAssign);
    } else {
      assignee.clearMap();
      // TODO: this should not be done implicitly but done specifically, if the
      //  semantic analysis leads to the conclusion that the types are different

      Map<Value, Value> valuesToAdd = mapValueToAssign.internalMap();
      for (var entryToAdd : valuesToAdd.entrySet()) {

        Value entryKeyValue = createDefaultValue(assigneeKeyType);
        Value entryElementValue = createDefaultValue(assigneeEntryType);

        // we cannot directly set the entryValueToAssign, because we potentially
        // have to do type conversions (convert a String into a Content-Object)
        setValue(entryKeyValue, entryToAdd.getKey());
        setValue(entryElementValue, entryToAdd.getValue());

        assignee.addValue(entryKeyValue, entryElementValue);
      }
      return true;
    }
  }

  private boolean setListValue(ListValue assignee, Value valueToAssign) {
    if (!(valueToAssign instanceof ListValue listValueToAssign)) {
      throw new RuntimeException(
          "Can't assign value " + valueToAssign + " to ListValue, it is not a ListValue itself!");
    }

    // TODO: should just implement the cloning-behaviour for this
    IType assigneeEntryType = assignee.getDataType().getElementType();
    IType newValueEntryType = listValueToAssign.getDataType().getElementType();
    if (assigneeEntryType.equals(newValueEntryType)) {
      return assignee.setFrom(listValueToAssign);
    } else {
      // TODO: this should not be done implicitly but done specifically, if the
      //  semantic analysis leads to the conclusion that the types are different
      assignee.clearList();
      for (var valueToAdd : listValueToAssign.getValues()) {
        Value entryAssigneeValue = createDefaultValue(assigneeEntryType);

        // we cannot directly set the entryValueToAssign, because we potentially
        // have to do type conversions (convert a String into a Content-Object)
        setValue(entryAssigneeValue, valueToAdd);

        assignee.addValue(entryAssigneeValue);
      }
      return true;
    }
  }

  private boolean setFunctionValue(FunctionValue assignee, Value valueToAssign) {
    if (!(valueToAssign instanceof FunctionValue functionValueToAssign)) {
      throw new RuntimeException(
          "Can't assign value "
              + valueToAssign
              + " to FunctionValue, it is not a FunctionValue itself!");
    }

    assignee.setDataType(functionValueToAssign.getDataType());
    assignee.setInternalValue(functionValueToAssign.getCallable());

    return true;
  }

  public boolean setValue(Value assignee, Value valueToAssign) {
    if (assignee == Value.NONE) {
      return false;
    }
    // if the lhs is a pod, set the internal value, otherwise, set the MemorySpace of the
    // returned value
    if (assignee instanceof AggregateValue aggregateAssignee) {
      setAggregateValue(aggregateAssignee, valueToAssign);
    } else if (assignee instanceof ListValue assigneeListValue) {
      setListValue(assigneeListValue, valueToAssign);
    } else if (assignee instanceof SetValue assigneeSetValue) {
      setSetValue(assigneeSetValue, valueToAssign);
    } else if (assignee instanceof MapValue assigneeMapValue) {
      setMapValue(assigneeMapValue, valueToAssign);
    } else if (assignee instanceof FunctionValue assigneeFunctionValue) {
      setFunctionValue(assigneeFunctionValue, valueToAssign);
    } else if (assignee instanceof PropertyValue propertyValue) {
      var instantiatedValue = this.environment.getTypeInstantiator().instantiate(valueToAssign);
      assignee.setInternalValue(instantiatedValue);
    } else if (assignee instanceof EncapsulatedField encapsulatedField) {
      if (assignee.getDataType().getTypeKind().equals(IType.Kind.FunctionType)) {
        // instantiate a new callback adapter to encapsulate the function call
        var callbackAdapter = this.environment.getTypeInstantiator().instantiate(valueToAssign);
        assignee.setInternalValue(callbackAdapter);
      } else {
        assignee.setFrom(valueToAssign);
      }
    } else {
      return assignee.setFrom(valueToAssign);
    }
    return true;
  }

  @Override
  public Object visit(WhileLoopStmtNode node) {
    MemorySpace loopsMemorySpace = new MemorySpace(this.getCurrentMemorySpace());
    this.memoryStack.push(loopsMemorySpace);

    // add loop-bottom-mark node for checking
    // and updating the loop condition and variable(s)
    LoopBottomMark loopBottomMark = new LoopBottomMark(node);
    this.statementStack.push(loopBottomMark);

    return null;
  }

  protected void setupForLoopExecution(LoopStmtNode node) {
    var loopType = node.loopType();
    assert loopType.equals(LoopStmtNode.LoopType.forLoop)
        || loopType.equals(LoopStmtNode.LoopType.countingForLoop);

    ForLoopStmtNode forLoopStmtNode = (ForLoopStmtNode) node;

    // evaluate iterable expression
    Value iterableValue = (Value) forLoopStmtNode.getIterableIdNode().accept(this);
    IType iterableType = iterableValue.getDataType();

    Iterator<Value> internalIterator;
    if (iterableType.getTypeKind().equals(IType.Kind.ListType)) {
      var listValue = (ListValue) iterableValue;
      List<Value> internalList = listValue.internalList();
      internalIterator = internalList.iterator();
    } else if (iterableType.getTypeKind().equals(IType.Kind.SetType)) {
      var setValue = (SetValue) iterableValue;
      Set<Value> internalSet = setValue.getValues();
      internalIterator = internalSet.iterator();
    } else {
      throw new RuntimeException("Non iterable type '" + iterableType + "' used in for loop!");
    }

    // create new loop-variable in surrounding (or loops?) memoryspace
    MemorySpace loopMemorySpace = new MemorySpace(this.getCurrentMemorySpace());
    this.memoryStack.push(loopMemorySpace);

    // get the symbol for the loop variable
    Node variableIdNode = forLoopStmtNode.getVarIdNode();
    Symbol variableSymbol = this.symbolTable().getSymbolsForAstNode(variableIdNode).get(0);

    Symbol counterVariableSymbol = Symbol.NULL;
    if (node.loopType().equals(LoopStmtNode.LoopType.countingForLoop)) {
      // get the symbol for the counter variable
      Node counterIdNode = ((CountingLoopStmtNode) node).getCounterIdNode();
      counterVariableSymbol = this.symbolTable().getSymbolsForAstNode(counterIdNode).get(0);
      // initialize counter variable
      Value counterValue = bindFromSymbol(counterVariableSymbol, loopMemorySpace);
      counterValue.setInternalValue(-1);
    }

    // add loop-bottom-mark node for checking
    // and updating the loop condition and variable(s)
    LoopBottomMark loopBottomMark =
        new LoopBottomMark(node, internalIterator, variableSymbol, counterVariableSymbol);
    this.statementStack.push(loopBottomMark);
  }

  @Override
  public Object visit(CountingLoopStmtNode node) {
    setupForLoopExecution(node);
    return null;
  }

  @Override
  public Object visit(ForLoopStmtNode node) {
    setupForLoopExecution(node);
    return null;
  }

  protected void updateForLoopState(
      IMemorySpace previosIterationsLoopMemorySpace, LoopBottomMark node) {
    LoopStmtNode loopNode = node.getLoopStmtNode();
    var loopType = loopNode.loopType();
    assert loopType.equals(LoopStmtNode.LoopType.forLoop)
        || loopType.equals(LoopStmtNode.LoopType.countingForLoop);

    Iterator<Value> loopIterator = node.getInternalIterator();
    if (loopIterator.hasNext()) {
      // create loops-memory space for next iteration
      MemorySpace newLoopMemorySpace = new MemorySpace(this.getCurrentMemorySpace());

      // update loop variable
      Value nextIterationValue = loopIterator.next();
      Value valueInMemorySpace = bindFromSymbol(node.getLoopVariableSymbol(), newLoopMemorySpace);
      setValue(valueInMemorySpace, nextIterationValue);

      if (loopType.equals(LoopStmtNode.LoopType.countingForLoop)) {
        // update counter variable
        Symbol counterSymbol = node.getCounterVariableSymbol();
        Value counterValue = previosIterationsLoopMemorySpace.resolve(counterSymbol.getName());
        counterValue.setInternalValue((Integer) counterValue.getInternalValue() + 1);
        newLoopMemorySpace.bindValue(counterSymbol.getName(), counterValue);
      }

      // prepare next iteration
      this.memoryStack.push(newLoopMemorySpace);
      this.statementStack.push(node);
      this.statementStack.push(loopNode.getStmtNode());
    }
  }

  @Override
  public Object visit(LoopBottomMark node) {
    LoopStmtNode loopNode = node.getLoopStmtNode();

    // clean up the memoryspace
    IMemorySpace loopsMemorySpace = this.memoryStack.pop();
    switch (loopNode.loopType()) {
      case whileLoop -> {
        WhileLoopStmtNode whileLoopStmtNode = (WhileLoopStmtNode) loopNode;

        // evaluate condition
        Value conditionValue = (Value) whileLoopStmtNode.getExpressionNode().accept(this);
        if (isBooleanTrue(conditionValue)) {
          // setup memory space for next iteration
          MemorySpace newIterationMemorySpace = new MemorySpace(this.getCurrentMemorySpace());
          this.memoryStack.push(newIterationMemorySpace);

          // prepare execution of next iteration
          this.statementStack.push(node);
          this.statementStack.push(loopNode.getStmtNode());
        }
      }
      case forLoop, countingForLoop -> updateForLoopState(loopsMemorySpace, node);
      default -> {}
    }

    return null;
  }

  // endregion

  // region function execution

  /**
   * Implements the call of a {@link ICallable} with parameters given as a {@link List} of {@link
   * Node}s representing the parameters of the call. This function will automatically package the
   * returned object of the call into an {@link Value} instance, if the call did return an arbitrary
   * {@link Object}.
   *
   * @param callable The {@link ICallable} to call.
   * @param parameterNodes The list of parameter {@link Node}s to call the {@link ICallable} with.
   * @return The returned {@link Value} of the call.
   */
  protected Value callCallable(ICallable callable, List<Node> parameterNodes) {
    // execute function call
    var returnObject = callable.call(this, parameterNodes);
    Value returnValue = Value.NONE;
    if (returnObject == null) {
      return returnValue;
    }

    if (!(returnObject instanceof Value returnObjectAsValue)) {
      // package it into value
      IType targetType = callable.getFunctionType().getReturnType();
      returnValue =
          (Value)
              this.environment.translateRuntimeObject(
                  returnObject, this.getCurrentMemorySpace(), targetType);
    } else {
      returnValue = returnObjectAsValue;
    }

    return returnValue;
  }

  /**
   * Implement a call of an {@link ICallable} with raw {@link Object}s for the parameters. This
   * method will create a new {@link IMemorySpace}, create {@link Value}s for each parameter of the
   * {@link ICallable} and set these {@link Value}s to the passed {@link Objects}. It will also
   * create new {@link IdNode}s with names, which match the parameter names of the {@link ICallable}
   * and pass these IdNodes to the call.
   *
   * @param callable The {@link ICallable} to call.
   * @param parameterObjects The raw {@link Object}s to call the callable with.
   * @return The returned {@link Object} of the call.
   */
  public Object callCallableRawParameters(ICallable callable, List<Object> parameterObjects) {
    if (callable.getCallableType().equals(ICallable.Type.Native)) {
      NativeFunction func = (NativeFunction) callable;

      IMemorySpace functionMemorySpace =
          createFunctionMemorySpace(func, this.getCurrentMemorySpace());
      setupFunctionParametersRaw(func, functionMemorySpace, parameterObjects);

      // create mock ID-nodes
      var parameterSymbols = func.getSymbols();
      List<Node> mockIdNodes = new ArrayList<>(parameterSymbols.size());
      for (Symbol parameterSymbol : parameterSymbols) {
        mockIdNodes.add(new IdNode(parameterSymbol.getName(), SourceFileReference.NULL));
      }

      this.memoryStack.push(functionMemorySpace);
      // call callable
      Value returnValue = callCallable(callable, mockIdNodes);
      this.memoryStack.pop();

      return returnValue;
    } else if (callable.getCallableType().equals(ICallable.Type.UserDefined)) {
      FunctionSymbol functionSymbol = (FunctionSymbol) callable;

      Object retObject = executeUserDefinedFunctionRawParameters(functionSymbol, parameterObjects);
      return retObject;
    }
    return null;
  }

  /**
   * This implements a call to a user defined dsl-function
   *
   * @param symbol The symbol corresponding to the function to call
   * @param parameterObjects The concrete raw objects to use as parameters of the function call
   * @return The return value of the function call
   */
  protected Object executeUserDefinedFunctionRawParameters(
      FunctionSymbol symbol, List<Object> parameterObjects) {
    IScope scope = symbol.getScope();
    assert scope instanceof FileScope;
    FileScope fs = (FileScope) scope;
    boolean otherFileMSOnTop = isDifferentMemorySpaceOnTop(fs);

    IMemorySpace functionsParentMS;
    if (otherFileMSOnTop) {
      functionsParentMS = initializeFileMemorySpace(fs);
    } else {
      functionsParentMS = this.getCurrentMemorySpace();
    }

    // check, whether file scopes memory space is on top of file memory stack
    IMemorySpace functionMemorySpace = createFunctionMemorySpace(symbol, functionsParentMS);
    setupFunctionParametersRaw(symbol, functionMemorySpace, parameterObjects);

    if (otherFileMSOnTop) {
      this.memoryStack.push(functionsParentMS);
      this.fileMemoryStack.push(functionsParentMS);
    }

    this.memoryStack.push(functionMemorySpace);
    executeUserDefinedFunctionBody(symbol);
    functionMemorySpace = memoryStack.pop();

    if (otherFileMSOnTop) {
      this.memoryStack.pop();
      this.fileMemoryStack.pop();
    }

    return getReturnValueFromMemorySpace(functionMemorySpace);
  }

  /**
   * This implements a call to a user defined dsl-function
   *
   * @param symbol The symbol corresponding to the function to call
   * @param parameterNodes The ASTNodes of the parameters of the function call
   * @return The return value of the function call
   */
  public Object executeUserDefinedFunction(FunctionSymbol symbol, List<Node> parameterNodes) {
    // TODO PROBLEM: parameterNodes must be evaluated in call memory space, then the files
    //  memory space must be pushed, then the function memory space must be pushed
    //  -> current creation of function memory space does not work with this, order of
    //  operations is wrong -> memory space checking could be done in createFunctionMemorySpace

    // TODO: File Memory Space checking
    // TODO: check client code for duplicate file memory space checking
    IScope scope = symbol.getScope();
    assert scope instanceof FileScope;
    FileScope fs = (FileScope) scope;
    boolean otherFileMSOnTop = isDifferentMemorySpaceOnTop(fs);

    IMemorySpace functionsParentMS;
    if (otherFileMSOnTop) {
      functionsParentMS = initializeFileMemorySpace(fs);
    } else {
      functionsParentMS = this.getCurrentMemorySpace();
    }

    IMemorySpace functionMemorySpace = createFunctionMemorySpace(symbol, functionsParentMS);
    // can't push memory space yet! If a passed argument has the same identifier
    // as a parameter, the name will be resolved in the new memory space and not
    // the enclosing memory space, containing the argument
    setupFunctionParameters(symbol, functionMemorySpace, parameterNodes);

    if (otherFileMSOnTop) {
      this.memoryStack.push(functionsParentMS);
      this.fileMemoryStack.push(functionsParentMS);
    }

    this.memoryStack.push(functionMemorySpace);
    executeUserDefinedFunctionBody(symbol);
    functionMemorySpace = memoryStack.pop();

    if (otherFileMSOnTop) {
      this.memoryStack.pop();
      this.fileMemoryStack.pop();
    }

    return getReturnValueFromMemorySpace(functionMemorySpace);
  }

  /**
   * This function translates all passed parameters into DSL-Values and binds them as parameters in
   * the current memory space
   *
   * @param functionSymbol The symbol corresponding to the function definition
   * @param parameterObjects Raw objects to use as values for the function's parameters
   */
  private void setupFunctionParametersRaw(
      ScopedSymbol functionSymbol,
      IMemorySpace functionsMemorySpace,
      List<Object> parameterObjects) {
    var currentMemorySpace = getCurrentMemorySpace();
    // bind all parameter-symbols as values in the function's memory space and set their values
    var parameterSymbols = functionSymbol.getSymbols();
    for (int i = 0; i < parameterObjects.size(); i++) {
      var parameterSymbol = parameterSymbols.get(i);
      bindFromSymbol(parameterSymbol, functionsMemorySpace);

      Object parameterObject = parameterObjects.get(i);
      Value paramValue =
          (Value)
              this.environment.translateRuntimeObject(
                  parameterObject, currentMemorySpace, parameterSymbol.getDataType());
      Value assigneeValue = functionsMemorySpace.resolve(parameterSymbol.getName());
      setValue(assigneeValue, paramValue);
    }
  }

  /**
   * This function evaluates all passed nodes as values and binds them as parameters in the current
   * memory space
   *
   * @param functionSymbol The symbol corresponding to the function definition
   * @param parameterNodes AST-Nodes representing the passed parameters
   */
  private void setupFunctionParameters(
      FunctionSymbol functionSymbol, IMemorySpace functionsMemorySpace, List<Node> parameterNodes) {
    // bind all parameter-symbols as values in the function's memory space and set their values
    var parameterSymbols = functionSymbol.getSymbols();
    for (int i = 0; i < parameterNodes.size(); i++) {
      var parameterSymbol = parameterSymbols.get(i);
      bindFromSymbol(parameterSymbol, functionsMemorySpace);

      var paramValueNode = parameterNodes.get(i);
      Value paramValue = (Value) paramValueNode.accept(this);

      Value assigneeValue = functionsMemorySpace.resolve(parameterSymbol.getName());
      setValue(assigneeValue, paramValue);
    }
  }

  private boolean isDifferentMemorySpaceOnTop(FileScope fs) {
    IMemorySpace filesMemorySpace = initializeFileMemorySpace(fs);

    IMemorySpace topMS = this.fileMemoryStack.peek();
    return topMS == null || !topMS.equals(filesMemorySpace);
  }

  /**
   * Create a new IMemorySpace for a function call and bind the return Value, if the function has a
   * return type
   *
   * @param functionSymbol The Symbol representing the function definition
   * @return The created IMemorySpace
   */
  private IMemorySpace createFunctionMemorySpace(
      ScopedSymbol functionSymbol, IMemorySpace parentSpace) {

    // push new memorySpace and parameters on spaceStack
    var functionMemSpace = new MemorySpace(parentSpace);

    // create and bind the return value
    var functionType = (FunctionType) functionSymbol.getDataType();
    if (functionType.getReturnType() != BuiltInType.noType) {
      var returnValue = createDefaultValue(functionType.getReturnType());
      functionMemSpace.bindValue(RETURN_VALUE_NAME, returnValue);
    }
    return functionMemSpace;
  }

  /**
   * Extract a return value from a IMemorySpace
   *
   * @param ms The given memorySpace to resolve the return value in
   * @return The resolved return value
   */
  private Value getReturnValueFromMemorySpace(IMemorySpace ms) {
    // only lookup the return value in the current memory space,
    // if a function does not define a return value, we don't want to
    // walk up into other memory spaces and falsely return a return value
    // defined by another function further up in the callstack
    return ms.resolve(RETURN_VALUE_NAME, false);
  }

  /**
   * Execute Statements in a functions body
   *
   * @param symbol The symbol representing the function definition
   */
  private void executeUserDefinedFunctionBody(FunctionSymbol symbol) {
    // add return mark
    statementStack.addFirst(new Node(Node.Type.ReturnMark));

    // put statement block on statement stack
    var funcRootNode = symbol.getAstRootNode();
    var stmtBlock = (StmtBlockNode) funcRootNode.getStmtBlock();
    if (stmtBlock != Node.NONE) {
      statementStack.addFirst(stmtBlock);
    }

    while (statementStack.peek() != null && statementStack.peek().type != Node.Type.ReturnMark) {
      var stmt = statementStack.pop();
      stmt.accept(this);
    }

    // pop the return mark
    assert Objects.requireNonNull(statementStack.peek()).type == Node.Type.ReturnMark;
    statementStack.pop();
  }

  // endregion

  // region ASTVisitor implementation for nodes which do not need to be interpreted

  @Override
  public Object visit(BinaryNode node) {
    return null;
  }

  @Override
  public Object visit(EdgeRhsNode node) {
    return null;
  }

  @Override
  public Object visit(DotEdgeStmtNode node) {
    return null;
  }

  @Override
  public Object visit(EdgeOpNode node) {
    return null;
  }

  @Override
  public Object visit(ParamDefNode node) {
    return null;
  }

  // endregion

  // region helpers for native functions/methods

  /**
   * Evaluates a List of {@link Node}s in the current {@link IMemorySpace} of this {@link
   * DSLInterpreter}.
   *
   * @param nodes The {@link Node}s to evaluate.
   * @return a List of {@link Value}s, which holds the evaluated Nodes. In the same order as passed
   *     Nodes.
   */
  public List<Value> evaluateNodes(List<Node> nodes) {
    ArrayList<Value> values = new ArrayList<>(nodes.size());
    for (Node node : nodes) {
      Value value = (Value) node.accept(this);
      values.add(value);
    }
    return values;
  }

  /**
   * Convert a List of {@link Value}s to Objects by using the internal {@link TypeInstantiator}.
   *
   * @param values The List of {@link Value}s to convert.
   * @return a List of Objects, which holds the converted values. In the same order as passed
   *     Values.
   */
  public List<Object> translateValuesToObjects(List<Value> values) {
    ArrayList<Object> objects = new ArrayList<>(values.size());
    var instantiator = this.getRuntimeEnvironment().getTypeInstantiator();
    for (Value value : values) {
      Object object = instantiator.instantiate(value);
      objects.add(object);
    }
    return objects;
  }
  // endregion
}
