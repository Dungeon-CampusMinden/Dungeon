package interpreter;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import dslToGame.QuestConfigBuilder;
import interpreter.dot.Interpreter;
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
import semanticAnalysis.types.*;

// TODO: specify EXACT semantics of value copying and setting

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {

    private QuestConfigBuilder questConfigBuilder;
    private RuntimeEnvironment environment;
    private final Stack<IMemorySpace> memoryStack;
    private IMemorySpace globalSpace;

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

    public IMemorySpace getGlobalMemorySpace() {
        return this.globalSpace;
    }

    // TODO: refactor
    public void evaluateTypeDefinitions(IEvironment environment) {
        // TODO: could we just iterate over the types?
        var globalScope = environment.getGlobalScope();
        for (var symbol : globalScope.getSymbols()) {
            if (symbol instanceof AggregateType) {
                var creationAstNode = symbolTable().getCreationAstNode(symbol);
                if (creationAstNode.type.equals(Node.Type.GameObjectDefinition)) {
                    var gameObjDefNode = (GameObjectDefinitionNode) creationAstNode;

                    var gameObjTypeWithDefaults =
                            new Prototype((AggregateType) symbol, symbol.getIdx());

                    for (var node : gameObjDefNode.getComponentDefinitionNodes()) {
                        var componentNode = (ComponentDefinitionNode) node;
                        var componentSymbol =
                                this.symbolTable().getSymbolsForAstNode(componentNode).get(0);

                        assert componentSymbol.getDataType() instanceof AggregateType;

                        //  the AggregateTypeWithDefaults for a component does only live inside the
                        //  datatype
                        //  definition, because it is part of the definition

                        // evaluate rhs and store the value in the member of
                        // aggregateTypeWithDefaults
                        Prototype componentTypeWithDefaults =
                                new Prototype(
                                        (AggregateType) componentSymbol.getDataType(),
                                        componentSymbol.getIdx());
                        for (var propDef : componentNode.getPropertyDefinitionNodes()) {
                            var propertyDefNode = (PropertyDefNode) propDef;

                            var rhsValue = (Value) propertyDefNode.getStmtNode().accept(this);
                            System.out.println(rhsValue);

                            var propertySymbol = symbolTable().getSymbolsForAstNode(propDef).get(0);
                            // typechecking is happened at this point
                            var propertyType = propertySymbol.getDataType();
                            Value value =
                                    new Value(
                                            propertySymbol.getDataType(),
                                            rhsValue.getInternalObject(),
                                            propertySymbol.getIdx());
                            value.setDirty();

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
                    bindFromSymbol(symbol, memoryStack.peek());
                } else if (callableType == ICallable.Type.UserDefined) {
                    // TODO: if userDefined -> reference AST -> how to?
                    //  subclass of value? -> do it by symbol-reference
                }
            }
            // bind all global definitions
            else {
                bindFromSymbol(symbol, memoryStack.peek());
            }
        }
    }

    private boolean bindFromSymbol(Symbol symbol, IMemorySpace ms) {
        if (!(symbol instanceof IType)) {
            var value = createDefaultValue(symbol.getDataType(), symbol.getIdx());
            ms.bindValue(symbol.getName(), value);
            return true;
        }
        return false;
    }

    /**
     * Creates a DSL level instantiation of a type, which means, that all fields of an aggregate
     * type are set to their default value.
     *
     * @param type
     * @param symbolIdx
     * @return
     */
    private Value createDefaultValue(IType type, int symbolIdx) {
        if (type.getTypeKind().equals(IType.Kind.Basic)) {
            Object internalValue = Value.getDefaultValue(type);
            return new Value(type, internalValue, symbolIdx);
        } else {
            var topMostMs = this.memoryStack.peek();
            AggregateValue value = new AggregateValue((AggregateType) type, symbolIdx, topMostMs);
            this.memoryStack.push(value.getMemorySpace());
            for (var member : ((AggregateType) type).getSymbols()) {
                bindFromSymbol(member, memoryStack.peek());
            }
            this.memoryStack.pop();
            return value;
        }
    }

    /**
     * Parse the config script and return the questConfig object, which serves as an entry point for
     * further evaluation and interpretation.
     *
     * @param configScript The script (in the DungeonDSL) to parse
     * @return The first questConfig object found in the configScript
     */
    public Object getQuestConfig(String configScript) {
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

    public Object generateQuestConfig(Node programAST) {
        this.questConfigBuilder = new QuestConfigBuilder();

        evaluateTypeDefinitions(this.environment);

        // find quest_config definition
        for (var node : programAST.getChildren()) {
            if (node.type == Node.Type.ObjectDefinition) {
                var objDefNode = (ObjectDefNode) node;
                if (objDefNode.getTypeSpecifierName().equals("quest_config")) {
                    return objDefNode.accept(this);
                }
                break;
            }
        }
        return this.questConfigBuilder.build();
    }

    /**
     * Instantiate a dsl prototype (which is an aggregate type with defaults) as a new Value
     *
     * @param type
     * @param symbolIdx
     * @return
     */
    protected Value instantiatePrototype(Prototype type, int symbolIdx) {
        // create memory space to store the values in
        AggregateValue instance = new AggregateValue(type, symbolIdx, this.memoryStack.peek());

        // TODO: how to handle function calls here?
        //  we should evaluate functions as soon as possible, and only allow
        //  functions as objects to be passed to members, which actually expect a
        //  callback function
        IMemorySpace ms = instance.getMemorySpace();
        this.memoryStack.push(ms);
        var internalType = (AggregateType) type.getDataType();
        for (var member : internalType.getSymbols()) {
            // check, if type defines default for member
            var defaultValue = type.getDefaultValue(member.getName());
            if (defaultValue instanceof Prototype) {
                defaultValue = instantiatePrototype((Prototype) defaultValue, symbolIdx);
            } else if (!defaultValue.equals(Value.NONE)) {
                // copy value (this is a copy of the DSL-Value, not the internal Object of the
                // value)
                defaultValue = (Value) defaultValue.clone();
            } else {
                // no default value, generate default ourselves
                defaultValue = createDefaultValue(member.getDataType(), member.getIdx());
            }

            ms.bindValue(member.getName(), defaultValue);
        }
        this.memoryStack.pop();

        return instance;
    }

    private AggregateType getOriginalTypeOfTypeWithDefaults(Prototype type) {
        IType returnType = type;
        while (returnType instanceof Prototype) {
            returnType = ((Prototype) returnType).getDataType();
        }
        return (AggregateType) returnType;
    }

    // this is the evaluation side of things
    @Override
    public Object visit(GameObjectDefinitionNode node) {
        var typeWithDefaults = this.environment.lookupTypeWithDefaults(node.getIdName());
        var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        var instance = (AggregateValue) instantiatePrototype(typeWithDefaults, symbol.getIdx());

        // instantiate entity
        TypeInstantiator ti = new TypeInstantiator();
        var type = (AggregateType) this.symbolTable().getGlobalScope().resolve("game_object");
        var entityObject = ti.instantiateFromMemorySpace(type, instance.getMemorySpace());

        // TODO: this should be done automatically in the TypeInstantiator, this is a proof of
        // concept
        // push entity as context in TypeInstantiator
        var annot = type.getOriginalJavaClass().getAnnotation(DSLContextPush.class);
        if (annot != null) {
            String contextName =
                    annot.name().equals("") ? type.getOriginalJavaClass().getName() : annot.name();
            ti.pushContextMember(contextName, entityObject);
        }

        AggregateValue entityValue =
                new AggregateValue(type, symbol.getIdx(), this.memoryStack.peek(), entityObject);

        // an entity-object itself has no members, so add the components as "artificial members"
        // to the aggregate dsl value of the entity
        for (var memberEntry : instance.getMemorySpace().getValueSet()) {
            String memberName = memberEntry.getKey();
            Value memberValue = memberEntry.getValue();
            if (memberValue instanceof AggregateValue) {
                AggregateType membersOriginalType =
                        getOriginalTypeOfTypeWithDefaults((Prototype) memberValue.getDataType());

                // instantiate object
                Object memberObject =
                        ti.instantiateFromMemorySpace(
                                membersOriginalType,
                                ((AggregateValue) memberValue).getMemorySpace());

                // put the memberObject inside an encapsulated memory space
                EncapsulatedObject encapsulatedObject =
                        new EncapsulatedObject(
                                memberObject,
                                membersOriginalType,
                                this.memoryStack.peek(),
                                this.environment);

                // add the memory space to an aggregateValue
                AggregateValue aggregateMemberValue =
                        new AggregateValue(
                                memberValue.getDataType(), -1, memberObject, encapsulatedObject);

                entityValue.getMemorySpace().bindValue(memberName, aggregateMemberValue);
            }
        }
        return entityValue;
    }

    @Override
    public Object visit(ObjectDefNode node) {
        // resolve name of object in memory space
        IMemorySpace ms;
        var objectsValue = this.memoryStack.peek().resolve(node.getIdName());
        if (objectsValue instanceof AggregateValue) {
            ms = ((AggregateValue) objectsValue).getMemorySpace();
        } else {
            throw new RuntimeException("Defined object is not an aggregate Value");
        }
        memoryStack.push(ms);

        // accept every propertyDefinition
        for (var propDefNode : node.getPropertyDefinitions()) {
            propDefNode.accept(this);
        }

        // convert from memorySpace to concrete object
        ms = memoryStack.pop();
        var objectSymbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        return createObjectFromMemorySpace(ms, objectSymbol.getDataType());
    }

    private Object createObjectFromMemorySpace(IMemorySpace ms, IType type) {
        if (type.getName().equals("quest_config")) {
            TypeInstantiator ti = new TypeInstantiator();
            return ti.instantiateFromMemorySpace((AggregateType) type, ms);
        }
        return null;
    }

    @Override
    public Object visit(PropertyDefNode node) {
        var value = (Value) node.getStmtNode().accept(this);
        var propertyName = node.getIdName();
        boolean setValue = setValue(propertyName, value);
        if (!setValue) {
            // TODO: handle, errormsg
        }
        return null;
    }

    @Override
    public Object visit(NumNode node) {
        return new Value(BuiltInType.intType, node.getValue(), -1);
    }

    @Override
    public Object visit(StringNode node) {
        return new Value(BuiltInType.stringType, node.getValue(), -1);
    }

    // this is used for resolving object references
    @Override
    public Object visit(IdNode node) {
        // how to get from id to the symbol?

        var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        var creationASTNode = this.symbolTable().getCreationAstNode(symbol);

        return creationASTNode.accept(this);
    }

    @Override
    public Object visit(DotDefNode node) {
        Interpreter dotInterpreter = new Interpreter();
        var graph = dotInterpreter.getGraph(node);
        return new Value(BuiltInType.graphType, graph, -1);
    }

    // TODO: this should probably check for type compatibility
    private boolean setValue(String name, Object value) {
        var ms = memoryStack.peek();
        var valueInMemorySpace = ms.resolve(name);
        if (valueInMemorySpace == Value.NONE) {
            return false;
        }
        // if the lhs is a pod, set the internal value, otherwise, set the MemorySpace of the
        // returned value..
        if (valueInMemorySpace instanceof AggregateValue) {
            AggregateValue valueToSet = (AggregateValue) value;
            ((AggregateValue) valueInMemorySpace).setMemorySpace(valueToSet.getMemorySpace());
            valueInMemorySpace.setInternalValue(valueToSet.getInternalObject());
        } else {
            valueInMemorySpace.setInternalValue(((Value) value).getInternalObject());
        }
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
        this.memoryStack.push(functionMemSpace);

        var funcAsScope = (ScopedSymbol) symbol;

        // TODO: push parameter for return value and actually return it
        var parameterSymbols = funcAsScope.getSymbols();
        for (int i = 0; i < parameterNodes.size(); i++) {
            var parameterSymbol = parameterSymbols.get(i);
            bindFromSymbol(parameterSymbol, memoryStack.peek());

            var paramValueNode = parameterNodes.get(i);
            var paramValue = paramValueNode.accept(this);

            setValue(parameterSymbol.getName(), paramValue);
        }

        // visit function AST
        var funcAstNode = this.symbolTable().getCreationAstNode(symbol);
        funcAstNode.accept(this);

        memoryStack.pop();
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
