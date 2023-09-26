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

import task.Quiz;

import java.util.*;

// TODO: specify EXACT semantics of value copying and setting

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {
    private RuntimeEnvironment environment;
    private final ArrayDeque<IMemorySpace> memoryStack;
    private final ArrayDeque<IMemorySpace> instanceMemoryStack;
    private final IMemorySpace globalSpace;

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

    /** Constructor. */
    public DSLInterpreter() {
        memoryStack = new ArrayDeque<>();
        instanceMemoryStack = new ArrayDeque<>();
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
                    this.getGlobalMemorySpace().bindValue(prototype.getName(), prototype);
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

    /**
     * Binds all function definitions, object definitions and data types in a global memory space.
     *
     * @param environment The environment to bind the functions, objects and data types from.
     */
    public void initializeRuntime(IEvironment environment) {
        this.environment = new RuntimeEnvironment(environment, this);

        // bind all function definition and object definition symbols to values
        // in global memorySpace
        // TODO: this should potentially done on a file basis, not globally for the whole
        // DSLInterpreter
        //  should define a file-scope...
        HashMap<Symbol, Value> globalValues = new HashMap<>();
        for (var symbol : symbolTable().getGlobalScope().getSymbols()) {
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
            if (!symbol.getDataType().getName().equals("quest_config")
                    && !symbol.getDataType().getName().equals("dungeon_config")) {
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
     * Creates a DSL level instantiation of a type, which means, that all fields of an aggregate
     * type are set to their default value.
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
            Object internalValue = Value.getDefaultValue(type);
            return new Value(type, internalValue);
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
        } else if (type.getTypeKind().equals(IType.Kind.FunctionType)) {
            return Value.NONE;
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

        Value questConfigValue = (Value) generateQuestConfig(programAST);
        return questConfigValue.getInternalValue();
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
                if (objDefNode.getTypeSpecifierName().equals("quest_config")
                        || objDefNode.getTypeSpecifierName().equals("dungeon_config")) {
                    return objDefNode.accept(this);
                }
            }
        }
        return Value.NONE;
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

    public AggregateType getOriginalTypeOfPrototype(Prototype type) {
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

            // accept every propertyDefinition
            for (var propDefNode : node.getPropertyDefinitions()) {
                propDefNode.accept(this);
            }

            memoryStack.pop();
            // convert from memorySpace to concrete object
            Object createdObject = createObjectFromValue((AggregateValue) objectsValue);
            EncapsulatedObject encapsulatedObject =
                    new EncapsulatedObject(
                            createdObject,
                            (AggregateType) objectsValue.getDataType(),
                            this.environment);
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
        Value value = (Value) node.getStmtNode().accept(this);
        var propertyName = node.getIdName();
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
        if (symbol instanceof FunctionSymbol functionSymbol) {
            return new FunctionValue(
                    functionSymbol.getFunctionType().getReturnType(), functionSymbol);
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
            var returnValue = callable.call(this, node.getParameters());
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
        while (statementStack.peek() != null
                && statementStack.peek().type != Node.Type.ReturnMark) {
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
        while (currentNode.type.equals(Node.Type.MemberAccess)) {
            lhs = ((MemberAccessNode) currentNode).getLhs();
            rhs = ((MemberAccessNode) currentNode).getRhs();

            if (lhs.type.equals(Node.Type.Identifier)) {
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

        Value rhsValue = Value.NONE;
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

        return rhsValue;
    }

    @Override
    public Object visit(VarDeclNode node) {
        String variableName = ((IdNode) node.getIdentifier()).getName();

        // check, if the current memory space already contains a value of the same name
        Value value = getCurrentMemorySpace().resolve(variableName, false);
        if (!value.equals(Value.NONE)) {
            getCurrentMemorySpace().delete(variableName);
            value = Value.NONE;
        }

        // create new Value in memory space (overwrite existing one)
        if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
            throw new UnsupportedOperationException(
                    "Assignment declaration currently not supported");
        } else {
            // get datatype
            Symbol variableSymbol = symbolTable().getSymbolsForAstNode(node).get(0);
            value = bindFromSymbol(variableSymbol, this.getCurrentMemorySpace());
        }
        return value;
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
        if (node.type.equals(Node.Type.ScopeExitMark)) {
            this.memoryStack.pop();
        }
        return null;
    }

    // region value-setting
    private void setAggregateValue(AggregateValue aggregateAssignee, Value valueToAssign) {
        if (!(valueToAssign instanceof AggregateValue aggregateValueToAssign)) {
            // if the value to assign is not an aggregate value, we might have
            // the case, where we want to assign a basic Value to a Content object

            IType assigneesType = aggregateAssignee.getDataType();
            if (assigneesType.getName().equals("content")) {
                // TODO: this is a temporary solution for "casting" the value to a content
                //  once typechecking is implemented, this will be refactored

                String stringValue = valueToAssign.getInternalValue().toString();
                Quiz.Content content = new Quiz.Content(stringValue);
                EncapsulatedObject encapsulatedObject =
                        new EncapsulatedObject(
                                content, (AggregateType) assigneesType, this.environment);

                aggregateAssignee.setMemorySpace(encapsulatedObject);
                aggregateAssignee.setInternalValue(content);
            } else {
                throw new RuntimeException(
                        "Can't assign Value of type "
                                + valueToAssign.getDataType()
                                + " to Value of "
                                + assigneesType);
            }
        } else {
            aggregateAssignee.setMemorySpace(aggregateValueToAssign.getMemorySpace());
            aggregateAssignee.setInternalValue(aggregateValueToAssign.getInternalValue());
        }
    }

    private boolean setSetValue(SetValue assignee, Value valueToAssign) {
        if (!(valueToAssign instanceof SetValue setValueToAssign)) {
            throw new RuntimeException(
                    "Can't assign value "
                            + valueToAssign
                            + " to SetValue, it is not a SetValue itself!");
        }

        assignee.clearSet();

        IType entryType = assignee.getDataType().getElementType();
        Set<Value> valuesToAdd = setValueToAssign.getValues();
        for (Value valueToAdd : valuesToAdd) {
            Value entryAssigneeValue = createDefaultValue(entryType);

            // we cannot directly set the entryValueToAssign, because we potentially
            // have to do type conversions (convert a String into a Content-Object)
            setValue(entryAssigneeValue, valueToAdd);

            assignee.addValue(entryAssigneeValue);
        }
        return true;
    }

    private boolean setListValue(ListValue assignee, Value valueToAssign) {
        if (!(valueToAssign instanceof ListValue listValueToAssign)) {
            throw new RuntimeException(
                    "Can't assign value "
                            + valueToAssign
                            + " to ListValue, it is not a ListValue itself!");
        }

        assignee.clearList();

        IType entryType = assignee.getDataType().getElementType();
        for (var valueToAdd : listValueToAssign.getValues()) {
            Value entryAssigneeValue = createDefaultValue(entryType);

            // we cannot directly set the entryValueToAssign, because we potentially
            // have to do type conversions (convert a String into a Content-Object)
            setValue(entryAssigneeValue, valueToAdd);

            assignee.addValue(entryAssigneeValue);
        }
        return true;
    }

    private boolean setValue(Value assignee, Value valueToAssign) {
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
        } else {
            assignee.setInternalValue(valueToAssign.getInternalValue());
        }
        return true;
    }
    // endregion

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
        setupFunctionParametersRaw(symbol, functionMemorySpace, parameterObjects);

        this.memoryStack.push(functionMemorySpace);
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
        // can't push memory space yet! If a passed argument has the same identifier
        // as a parameter, the name will be resolved in the new memory space and not
        // the enclosing memory space, containing the argument
        setupFunctionParameters(symbol, functionMemorySpace, parameterNodes);

        this.memoryStack.push(functionMemorySpace);
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
            FunctionSymbol functionSymbol,
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
                                    parameterObject,
                                    currentMemorySpace,
                                    parameterSymbol.getDataType());
            Value assigneeValue = functionsMemorySpace.resolve(parameterSymbol.getName());
            setValue(assigneeValue, paramValue);
        }
    }

    /**
     * This function evaluates all passed nodes as values and binds them as parameters in the
     * current memory space
     *
     * @param functionSymbol The symbol corresponding to the function definition
     * @param parameterNodes AST-Nodes representing the passed parameters
     */
    private void setupFunctionParameters(
            FunctionSymbol functionSymbol,
            IMemorySpace functionsMemorySpace,
            List<Node> parameterNodes) {
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
}
