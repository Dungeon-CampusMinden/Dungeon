package interpreter;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import dslToGame.QuestConfig;
import dslToGame.QuestConfigBuilder;
import interpreter.dot.Interpreter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
// importing all required classes from parser.AST will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import parser.AST.*;
// CHECKSTYLE:ON: AvoidStarImport
import parser.DungeonASTConverter;
import runtime.*;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import semanticAnalysis.*;
// CHECKSTYLE:ON: AvoidStarImport
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.IType;
import semanticAnalysis.types.TypeInstantiator;

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {

    private QuestConfigBuilder questConfigBuilder;
    private RuntimeEnvironment environment;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace globalSpace;

    private SymbolTable symbolTable() {
        return environment.getSymbolTable();
    }

    // TODO: add entry-point for game-object traversal

    /** Constructor. */
    public DSLInterpreter() {
        memoryStack = new Stack<>();
        globalSpace = new MemorySpace();
        memoryStack.push(globalSpace);
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return this.environment;
    }

    public MemorySpace getGlobalMemorySpace() {
        return this.globalSpace;
    }

    // TODO: how to handle globally defined objects?
    //  statisch alles auswerten, was geht? und dann erst auswerten, wenn abgefragt (lazyeval?)
    //  wie wird order of operation vorgegeben? einfach von oben nach unten? oder nach referenz von
    //  objekt?

    // TODO: visit all datatype-definitions and evaluate for default-values
    //  this requires some kind of specialization of datatype-class
    //  -> lazyeval
    //  -> detect recursive definitions
    //  ..
    //  Anderer Ansatz: Erst, wenn ein Objekt instanziiert wird (also bspw. auf der rhs einer
    // Property-Zuweisung steht)
    //  die konkrete Instanz erstellen und dafür über den AST der Definition iterieren
    //  ..
    //  Bauchgefühl: Es wäre sauberer, da einen Zwischenschritt einzubauen und den "Datentypen"
    // einmal zu erstellen und
    //  anschließend nur noch zu instanziieren
    //  Problem: auch die Werte der Komponenten-Member müssen gespeichert werden.. das würde dann
    // bedeuten, dass für
    //  jede Typdefinition auch eine Instanz der Komponente konfiguriert werden muss.. was ja aber
    // sowieso passieren muss
    public void evaluateTypeDefinitions(IEvironment environment) {

        // TODO: could we just iterate over the types?
        var globalScope = environment.getGlobalScope();
        for (var symbol : globalScope.getSymbols()) {
            if (symbol instanceof AggregateType) {
                var creationAstNode = symbolTable().getCreationAstNode(symbol);
                if (creationAstNode.type.equals(Node.Type.GameObjectDefinition)) {
                    var gameObjDefNode = (GameObjectDefinitionNode) creationAstNode;

                    var gameObjTypeWithDefaults =
                            new AggregateTypeWithDefaults((AggregateType) symbol, symbol.getIdx());
                    // TODO: extend annotations to include the default value for each member and
                    // store a reference of
                    //  java class in the data type of generated component datatype

                    // TODO: create new AggregateTypeWithDefaults for each component
                    //  we actually need to iterate over the ast-node, not just over the symbols
                    for (var node : gameObjDefNode.getComponentDefinitionNodes()) {
                        var componentNode = (ComponentDefinitionNode) node;
                        var componentSymbol =
                                this.symbolTable().getSymbolsForAstNode(componentNode).get(0);

                        assert componentSymbol.getDataType() instanceof AggregateType;

                        // TODO: what to do, if the field is another aggregate datatype? how to get
                        //  a default-value for
                        //  that? -> requires storage of all typeDefinitions (with default values)
                        //  either in an
                        //  environment or the memorySpace.. but memorySpace is not really suited
                        //  for that, because
                        //  it holds values.. so the environment it is
                        //  the AggregateTypeWithDefaults for a component does only live inside the
                        //  datatype
                        //  definition, because it is part of the definition

                        // evaluate rhs and store the value in the member of aggrWithDefaulst
                        // TODO: how to get the rhs expression?
                        AggregateTypeWithDefaults componentTypeWithDefaults =
                                new AggregateTypeWithDefaults(
                                        (AggregateType) componentSymbol.getDataType(),
                                        componentSymbol.getIdx());
                        for (var propDef : componentNode.getPropertyDefinitionNodes()) {
                            var propertyDefNode = (PropertyDefNode) propDef;

                            // TODO: this should return a `Value`...
                            //  just calling accept(this) will likely end up very confusing..
                            //  should define specific expression evaluator for cases, in which the
                            //  value of an expression
                            //  should be calculated.. or the template argument of THE
                            //  DSLInterpreter is set to Value?
                            //  This would entail, that all is just a value.. even the returned
                            //  quest_config
                            //  but is this a good move? currently the `Value` class is the
                            //  runtime equivalent of a symbol in semantic analysis. If all
                            //  returns a value, this analogy is broken.. but is this a problem?
                            var rhsValue = propertyDefNode.getStmtNode().accept(this);
                            System.out.println(rhsValue);

                            // TODO: this is currently null
                            var propertySymbol = symbolTable().getSymbolsForAstNode(propDef).get(0);
                            // typechecking is happened at this point
                            var propertyType = propertySymbol.getDataType();
                            Value value =
                                    new Value(
                                            propertySymbol.getDataType(),
                                            rhsValue,
                                            propertySymbol.getIdx());

                            var valueName = propertyDefNode.getIdName();
                            componentTypeWithDefaults.addDefaultValue(valueName, value);
                        }

                        // add new component type with defaults to the enclosing game object type
                        // with defaults
                        gameObjTypeWithDefaults.addDefaultValue(
                                componentNode.getIdName(), componentTypeWithDefaults);
                    }
                    this.environment.addTypeWithDefaults(gameObjTypeWithDefaults);
                }
            }
        }
    }

    /**
     * Binds all function definitions and object definitions in a global memory space.
     *
     * @param environment The environment to bind the functions and objects from.
     */
    public void initializeRuntime(IEvironment environment) {

        this.environment = new RuntimeEnvironment(environment);

        // bind all function definition and object definition symbols to objects
        // in global memorySpace
        for (var symbol : symbolTable().getGlobalScope().getSymbols()) {
            if (symbol instanceof ICallable) {
                var callableType = ((ICallable) symbol).getCallableType();
                if (callableType == ICallable.Type.Native) {
                    this.globalSpace.bindFromSymbol(symbol);
                } else if (callableType == ICallable.Type.UserDefined) {
                    // TODO: if userDefined -> reference AST -> how to?
                    //  subclass of value? -> do it by symbol-reference
                }
            }
            // bind all global definitions
            else {
                this.globalSpace.bindFromSymbol(symbol);
            }
        }
    }

    /**
     * Parse the config script and return the questConfig object, which serves as an entry point for
     * further evaluation and interpretation.
     *
     * @param configScript The script (in the DungeonDSL) to parse
     * @return The first questConfig object found in the configScript
     */
    public dslToGame.QuestConfig getQuestConfig(String configScript) {
        var stream = CharStreams.fromString(configScript);
        var lexer = new DungeonDSLLexer(stream);

        var tokenStream = new CommonTokenStream(lexer);
        var parser = new DungeonDSLParser(tokenStream);
        var programParseTree = parser.program();

        DungeonASTConverter astConverter = new DungeonASTConverter();
        var programAST = astConverter.walk(programParseTree);

        SymbolTableParser symTableParser = new SymbolTableParser();
        var environment = new GameEnvironment();
        symTableParser.setup(environment);
        var result = symTableParser.walk(programAST);

        initializeRuntime(environment);

        var questConfig = generateQuestConfig(programAST);
        return questConfig;
    }

    public dslToGame.QuestConfig generateQuestConfig(Node programAST) {
        this.questConfigBuilder = new QuestConfigBuilder();

        evaluateTypeDefinitions(this.environment);

        // find quest_config definition
        for (var node : programAST.getChildren()) {
            if (node.type == Node.Type.ObjectDefinition) {
                var objDefNode = (ObjectDefNode) node;
                if (objDefNode.getTypeSpecifierName().equals("quest_config")) {
                    return (QuestConfig) objDefNode.accept(this);
                }
                break;
            }
        }
        return this.questConfigBuilder.build();
    }

    @Override
    public Object visit(ObjectDefNode node) {
        // resolve name of object in memory space
        MemorySpace ms;
        var objectsValue = this.memoryStack.peek().resolve(node.getIdName());
        if (objectsValue instanceof AggregateValue) {
            ms = ((AggregateValue) objectsValue).getMemorySpace();
        } else {
            throw new RuntimeException("Defined object is not an aggregate Value");
        }

        memoryStack.push(ms);

        // bind new value for every property
        for (var propDefNode : node.getPropertyDefinitions()) {
            var propDefSymbol = this.symbolTable().getSymbolsForAstNode(propDefNode).get(0);
            if (propDefSymbol == Symbol.NULL) {
                // TODO: handle
            } else {
                ms.bindFromSymbol(propDefSymbol);
            }
        }

        // accept every propertyDefinition
        for (var propDefNode : node.getPropertyDefinitions()) {
            propDefNode.accept(this);
        }

        // convert from memorySpace to concrete object
        ms = memoryStack.pop();
        var objectSymbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        return createObjectFromMemorySpace(ms, objectSymbol.getDataType());
    }

    private Object createObjectFromMemorySpace(MemorySpace ms, IType type) {
        if (type.getName().equals("quest_config")) {
            TypeInstantiator ti = new TypeInstantiator();
            Object instance;
            try {
                instance = ti.instantiateFromMemorySpace((AggregateType) type, ms);
                // TODO: handle more gracefully
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return instance;
        }
        return null;
    }

    @Override
    public Object visit(PropertyDefNode node) {
        var value = node.getStmtNode().accept(this);
        var propertyName = node.getIdName();
        boolean setValue = setInternalValue(propertyName, value);
        if (!setValue) {
            // TODO: handle, errormsg
        }
        return null;
    }

    @Override
    public Object visit(NumNode node) {
        return node.getValue();
    }

    @Override
    public Object visit(StringNode node) {
        return node.getValue();
    }

    // this is used for resolving object references
    @Override
    public Object visit(IdNode node) {
        // how to get from id to the symbol?

        var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        var creationASTNode = this.symbolTable().getCreationAstNode(symbol);

        assert creationASTNode.type == Node.Type.DotDefinition;
        return creationASTNode.accept(this);
    }

    @Override
    public Object visit(DotDefNode node) {
        Interpreter dotInterpreter = new Interpreter();
        return dotInterpreter.getGraph(node);
    }

    // TODO: this should probably check for type compatibility
    // TODO: should this create a new value, if one with the same name does not exist? nah..
    private boolean setInternalValue(String name, Object value) {
        var ms = memoryStack.peek();
        var valueInMemorySpace = ms.resolve(name);
        if (valueInMemorySpace == Value.NONE) {
            return false;
        }
        valueInMemorySpace.setInternalValue(value);
        return true;
    }

    /**
     * This handles parameter evaluation an binding and walking the AST of the function symbol
     *
     * @param symbol The symbol corresponding to the function to call
     * @param parameterNodes The ASTNodes of the parameters of the function call
     * @return The return value of the function call
     */
    public Object executeUserDefinedFunction(FunctionSymbol symbol, List<Node> parameterNodes) {
        // push new memorySpace and parameters on spaceStack
        var functionMemSpace = new MemorySpace(memoryStack.peek());
        var funcAsScope = (ScopedSymbol) symbol;

        // TODO: push parameter for return value
        var parameterSymbols = funcAsScope.getSymbols();
        for (int i = 0; i < parameterNodes.size(); i++) {
            var parameterSymbol = parameterSymbols.get(i);
            functionMemSpace.bindFromSymbol(parameterSymbol);

            var paramValueNode = parameterNodes.get(i);
            var paramValue = paramValueNode.accept(this);

            setInternalValue(parameterSymbol.getName(), paramValue);
        }

        memoryStack.push(functionMemSpace);

        // visit function AST
        var funcAstNode = this.symbolTable().getCreationAstNode(symbol);
        funcAstNode.accept(this);

        memoryStack.pop();
        // TODO: handle return value
        return null;
    }

    @Override
    public Object visit(FuncCallNode node) {
        // resolve function name in global memory-space
        var funcName = node.getIdName();
        var funcValue = this.globalSpace.resolve(funcName);

        // get the function symbol by symbolIdx from funcValue
        var funcSymbol = this.symbolTable().getSymbolByIdx(funcValue.getSymbolIdx());
        assert funcSymbol instanceof ICallable;
        var funcCallable = (ICallable) funcSymbol;

        return funcCallable.call(this, node.getParameters());
    }
}
