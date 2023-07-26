package interpreter;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;

import interpreter.dot.Interpreter;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
// importing all required classes from parser.AST will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport

import parser.DungeonASTConverter;
import parser.ast.*;
// CHECKSTYLE:ON: AvoidStarImport

import runtime.*;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport

import semanticanalysis.*;
// CHECKSTYLE:ON: AvoidStarImport
import semanticanalysis.types.*;

import java.util.*;

// TODO: specify EXACT semantics of value copying and setting

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {
    private RuntimeEnvironment environment;
    private final ArrayDeque<IMemorySpace> memoryStack;
    private final IMemorySpace globalSpace;

    private SymbolTable symbolTable() {
        return environment.getSymbolTable();
    }

    public IMemorySpace getCurrentMemorySpace() {
        return this.memoryStack.peek();
    }

    private final ArrayDeque<Node> statementStack;

    private static final String RETURN_VALUE_NAME = "$return_value$";

    /** Constructor. */
    public DSLInterpreter() {
        memoryStack = new ArrayDeque<>();
        globalSpace = new MemorySpace();
        statementStack = new ArrayDeque<>();
        memoryStack.push(globalSpace);
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

    /**
     * Iterates over all types in the passed IEnvironment and creates a {@link Prototype} for any
     * game object definition, which was defined by the user
     *
     * @param environment the environment to check for game object definitions
     */
    public void createGameObjectPrototypes(IEvironment environment) {
        // iterate over all types
        for (var type : environment.getTypes()) {
            if (type.getTypeKind().equals(IType.Kind.Aggregate)) {
                // if the type has a creation node, it is user defined, and we need to
                // create a prototype for it
                var creationAstNode = symbolTable().getCreationAstNode((Symbol) type);
                if (creationAstNode.type.equals(Node.Type.PrototypeDefinition)) {
                    var prototype = new Prototype((AggregateType) type);

                    var gameObjDefNode = (PrototypeDefinitionNode) creationAstNode;
                    for (var node : gameObjDefNode.getComponentDefinitionNodes()) {
                        // add new component prototype to the enclosing game object prototype
                        AggregateValueDefinitionNode compDefNode =
                                (AggregateValueDefinitionNode) node;
                        var componentPrototype = createComponentPrototype(compDefNode);
                        prototype.addDefaultValue(compDefNode.getIdName(), componentPrototype);
                    }
                    this.environment.addPrototype(prototype);
                }
            }
        }
    }

    private Prototype createComponentPrototype(AggregateValueDefinitionNode node) {
        var componentSymbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        assert componentSymbol.getDataType() instanceof AggregateType;

        // the Prototype for a component does only live inside the
        // datatype definition, because it is part of the definition
        // evaluate rhs and store the value in the member of
        // the prototype
        AggregateType prototypesType = (AggregateType) componentSymbol.getDataType();
        Prototype componentPrototype = new Prototype(prototypesType);
        for (var propDef : node.getPropertyDefinitionNodes()) {
            var propertyDefNode = (PropertyDefNode) propDef;
            var rhsValue = (Value) propertyDefNode.getStmtNode().accept(this);

            // get type of lhs (the assignee)
            var propName = propertyDefNode.getIdName();
            var propertiesType = prototypesType.resolve(propName).getDataType();

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

    /**
     * Binds all function definitions, object definitions and data types in a global memory space.
     *
     * @param environment The environment to bind the functions, objects and data types from.
     */
    public void initializeRuntime(IEvironment environment) {
        this.environment = new RuntimeEnvironment(environment, this);

        // bind all function definition and object definition symbols to objects
        // in global memorySpace
        for (var symbol : symbolTable().getGlobalScope().getSymbols()) {
            bindFromSymbol(symbol, memoryStack.peek());
        }
    }

    private boolean bindFromSymbol(Symbol symbol, IMemorySpace ms) {
        if (symbol instanceof ICallable) {
            var value = new FuncCallValue(symbol.getDataType(), symbol.getIdx());
            ms.bindValue(symbol.getName(), value);
            return true;
        }
        if (!(symbol instanceof IType)) {
            var value = createDefaultValue(symbol.getDataType());
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
     * @return
     */
    private Value createDefaultValue(IType type) {
        if (type == null) {
            System.out.println("Tried to create default value for null type");
            return Value.NONE;
        }
        if (type.getTypeKind().equals(IType.Kind.Basic)) {
            Object internalValue = Value.getDefaultValue(type);
            return new Value(type, internalValue);
        } else if (type.getTypeKind().equals(IType.Kind.Aggregate)
                || type.getTypeKind().equals(IType.Kind.PODAdapted)
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
        }
        return Value.NONE;
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

        SemanticAnalyzer symTableParser = new SemanticAnalyzer();
        var environment = new GameEnvironment();
        symTableParser.setup(environment);
        var result = symTableParser.walk(programAST);

        initializeRuntime(environment);

        var questConfig = generateQuestConfig(programAST);
        return questConfig;
    }

    /**
     * @param programAST The AST of the DSL program to generate a quest config object from
     * @return the object, which represents the quest config of the passed DSL program. The type of
     *     this object depends on the Class, which is set up as the 'quest_config' type in the
     *     {@link IEvironment} used by the DSLInterpreter (set by {@link
     *     #initializeRuntime(IEvironment)})
     */
    public Object generateQuestConfig(Node programAST) {
        createGameObjectPrototypes(this.environment);

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
        return null;
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
     * @param prototype the {@link Prototype} to instantiate
     * @return A new {@link Value} created from the {@link Prototype}
     */
    public Value instantiateDSLValue(Prototype prototype) {
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
            if (defaultValue instanceof Prototype) {
                defaultValue = instantiateDSLValue((Prototype) defaultValue);
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

    private AggregateType getOriginalTypeOfPrototype(Prototype type) {
        IType returnType = type;
        while (returnType instanceof Prototype) {
            returnType = ((Prototype) returnType).getInternalType();
        }
        return (AggregateType) returnType;
    }

    static boolean isBooleanTrue(Value value) {
        var valuesType = value.getDataType();
        var typeKind = valuesType.getTypeKind();
        if (!typeKind.equals(IType.Kind.Basic) && !value.equals(Value.NONE)) {
            return true;
        } else if (value.equals(Value.NONE)) {
            return false;
        } else {
            // basically check if zero
            return ((BuiltInType) valuesType).asBooleanFunction.run(value);
        }
    }

    // this is the evaluation side of things
    //
    @Override
    public Object visit(PrototypeDefinitionNode node) {
        return this.environment.lookupPrototype(node.getIdName());
    }

    public Object instantiateRuntimeValue(AggregateValue dslValue, AggregateType asType) {
        // instantiate entity_type
        var typeInstantiator = this.environment.getTypeInstantiator();
        var entityObject = typeInstantiator.instantiate(asType, dslValue.getMemorySpace());

        // TODO: substitute the whole DSLContextMember-stuff with Builder-Methods, which would
        //  enable creation of components with different parameters -> requires the ability to
        //  store multiple builder-methods for one type, distinguished by their
        //  signature
        var annot = asType.getOriginType().getAnnotation(DSLContextPush.class);
        String contextName = "";
        if (annot != null) {
            contextName = annot.name().equals("") ? asType.getOriginType().getName() : annot.name();
            typeInstantiator.pushContextMember(contextName, entityObject);
        }

        // an entity-object itself has no members, so add the components as "artificial members"
        // to the aggregate dsl value of the entity
        for (var memberEntry : dslValue.getValueSet()) {
            Value memberValue = memberEntry.getValue();
            if (memberValue instanceof AggregateValue) {
                // TODO: this is needed, because Prototype does not extend AggregateType currently,
                //  which should be fixed
                AggregateType membersOriginalType =
                        getOriginalTypeOfPrototype((Prototype) memberValue.getDataType());

                // instantiate object as a new java Object
                typeInstantiator.instantiate(
                        membersOriginalType, ((AggregateValue) memberValue).getMemorySpace());
            }
        }

        if (annot != null) {
            typeInstantiator.removeContextMember(contextName);
        }

        return entityObject;
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
            TypeInstantiator ti = this.environment.getTypeInstantiator();
            return ti.instantiate((AggregateType) type, ms);
        }
        return null;
    }

    @Override
    public Object visit(AggregateValueDefinitionNode node) {
        // create instance of dsl data type
        var type = this.symbolTable().getGlobalScope().resolve(node.getIdName());
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
        var creationASTNode = this.symbolTable().getCreationAstNode(symbol);

        // if the creationASTNode does not equal the node we are currently interpreting,
        // then the IdNode is just a reference node to some other structure
        if (creationASTNode != node) {
            return creationASTNode.accept(this);
        } else {
            // if the creationASTNode of the resolved symbol is the currently interpreted
            // IdNode, then we have to resolve the IdNode in the current memory space,
            // because it is used in an expression
            return this.getCurrentMemorySpace().resolve(node.getName(), true);
        }
    }

    @Override
    public Object visit(FuncDefNode node) {
        // return function reference as value
        var symbol = this.symbolTable().getSymbolsForAstNode(node).get(0);
        return new Value(symbol.getDataType(), symbol);
    }

    @Override
    public Object visit(DotDefNode node) {
        Interpreter dotInterpreter = new Interpreter();
        var graph = dotInterpreter.getGraph(node);
        return new Value(BuiltInType.graphType, graph);
    }

    // TODO: this should probably check for type compatibility
    private boolean setValue(String name, Object value) {
        var ms = memoryStack.peek();
        var valueInMemorySpace = ms.resolve(name);
        if (valueInMemorySpace == Value.NONE) {
            return false;
        }
        // if the lhs is a pod, set the internal value, otherwise, set the MemorySpace of the
        // returned value
        if (valueInMemorySpace instanceof AggregateValue) {
            AggregateValue valueToSet = (AggregateValue) value;
            ((AggregateValue) valueInMemorySpace).setMemorySpace(valueToSet.getMemorySpace());
            valueInMemorySpace.setInternalValue(valueToSet.getInternalValue());
        } else {
            // TODO: handle Lists and sets
            valueInMemorySpace.setInternalValue(((Value) value).getInternalValue());
        }
        return true;
    }

    @Override
    public Object visit(StmtBlockNode node) {
        ArrayList<Node> statements = node.getStmts();

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
        // resolve function name in global memory-space
        var funcName = node.getIdName();
        var funcValue = this.globalSpace.resolve(funcName);
        assert funcValue instanceof FuncCallValue;

        // get the function symbol by symbolIdx from funcValue
        int functionSymbolIndex = ((FuncCallValue) funcValue).getFunctionSymbolIdx();
        var funcSymbol = this.symbolTable().getSymbolByIdx(functionSymbolIndex);
        assert funcSymbol instanceof ICallable;
        var funcCallable = (ICallable) funcSymbol;

        // execute the function call
        var returnValue = funcCallable.call(this, node.getParameters());
        if (returnValue == null) {
            return Value.NONE;
        }

        if (!(returnValue instanceof Value)) {
            // package it into value
            var valueClass = returnValue.getClass();

            // try to resolve the objects type as primitive built in type
            var dslType = this.environment.getDSLTypeForClass(valueClass);
            if (dslType == null) {
                throw new RuntimeException(
                        "No DSL Type representation for java type '" + valueClass + "'");
            }
            returnValue = new Value(dslType, returnValue);
        }
        return returnValue;
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
                    returnValue.setInternalValue(value.getInternalValue());
                    break;
                }
            }
        }

        // unroll the statement stack until we find a return mark
        while (statementStack.peek() != null
                && statementStack.peek().type != Node.Type.ReturnMark) {
            statementStack.pop();
        }

        return null;
    }

    @Override
    public Object visit(ConditionalStmtNodeIf node) {
        Value conditionValue = (Value) node.getCondition().accept(this);
        if (isBooleanTrue(conditionValue)) {
            statementStack.addFirst(node.getIfStmt());
        }

        return null;
    }

    @Override
    public Object visit(ConditionalStmtNodeIfElse node) {
        Value conditionValue = (Value) node.getCondition().accept(this);
        if (isBooleanTrue(conditionValue)) {
            statementStack.addFirst(node.getIfStmt());
        } else {
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
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(LogicOrNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(LogicAndNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(EqualityNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ComparisonNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(TermNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(FactorNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(UnaryNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(AssignmentNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
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

    // region user defined function execution

    /**
     * This implements a call to a user defined dsl-function
     *
     * @param symbol The symbol corresponding to the function to call
     * @param parameterObjects The concrete raw objects to use as parameters of the function call
     * @return The return value of the function call
     */
    public Object executeUserDefinedFunctionRawParameters(
            FunctionSymbol symbol, List<Object> parameterObjects) {
        IMemorySpace functionMemorySpace = createFunctionMemorySpace(symbol);
        this.memoryStack.push(functionMemorySpace);

        setupFunctionParametersRaw(symbol, parameterObjects);
        executeUserDefinedFunctionBody(symbol);

        functionMemorySpace = memoryStack.pop();
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
        IMemorySpace functionMemorySpace = createFunctionMemorySpace(symbol);
        this.memoryStack.push(functionMemorySpace);

        setupFunctionParameters(symbol, parameterNodes);
        executeUserDefinedFunctionBody(symbol);

        functionMemorySpace = memoryStack.pop();
        return getReturnValueFromMemorySpace(functionMemorySpace);
    }

    /**
     * This function translates all passed parameters into DSL-Values and binds them as parameters
     * in the current memory space
     *
     * @param functionSymbol The symbol corresponding to the function definition
     * @param parameterObjects Raw objects to use as values for the function's parameters
     */
    private void setupFunctionParametersRaw(
            FunctionSymbol functionSymbol, List<Object> parameterObjects) {
        var currentMemorySpace = getCurrentMemorySpace();
        // bind all parameter-symbols as values in the function's memory space and set their values
        var parameterSymbols = functionSymbol.getSymbols();
        for (int i = 0; i < parameterObjects.size(); i++) {
            var parameterSymbol = parameterSymbols.get(i);
            bindFromSymbol(parameterSymbol, memoryStack.peek());

            Object parameterObject = parameterObjects.get(i);
            Value paramValue =
                    (Value)
                            this.environment.translateRuntimeObject(
                                    parameterObject,
                                    currentMemorySpace,
                                    parameterSymbol.getDataType());
            setValue(parameterSymbol.getName(), paramValue);
        }
    }

    /**
     * This function evaluates all passed nodes as values and binds them as parameters in the
     * current memory space
     *
     * @param functionSymbol The symbol corresponding to the function definition
     * @param parameterNodes AST-Nodes representing the passed parameters
     */
    private void setupFunctionParameters(FunctionSymbol functionSymbol, List<Node> parameterNodes) {
        // bind all parameter-symbols as values in the function's memory space and set their values
        var parameterSymbols = functionSymbol.getSymbols();
        for (int i = 0; i < parameterNodes.size(); i++) {
            var parameterSymbol = parameterSymbols.get(i);
            bindFromSymbol(parameterSymbol, memoryStack.peek());

            var paramValueNode = parameterNodes.get(i);
            var paramValue = paramValueNode.accept(this);

            setValue(parameterSymbol.getName(), paramValue);
        }
    }

    /**
     * Create a new IMemorySpace for a function call and bind the return Value, if the function has
     * a return type
     *
     * @param functionSymbol The Symbol representing the function definition
     * @return The created IMemorySpace
     */
    private IMemorySpace createFunctionMemorySpace(FunctionSymbol functionSymbol) {
        // push new memorySpace and parameters on spaceStack
        var functionMemSpace = new MemorySpace(memoryStack.peek());

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

        while (statementStack.peek() != null
                && statementStack.peek().type != Node.Type.ReturnMark) {
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
    public Object visit(Node node) {
        return null;
    }

    @Override
    public Object visit(BinaryNode node) {
        return null;
    }

    @Override
    public Object visit(EdgeRhsNode node) {
        return null;
    }

    @Override
    public Object visit(EdgeStmtNode node) {
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
}
