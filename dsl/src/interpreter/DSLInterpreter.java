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

import java.util.ArrayDeque;
import java.util.List;

// TODO: specify EXACT semantics of value copying and setting

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {

    private RuntimeEnvironment environment;
    private final ArrayDeque<IMemorySpace> memoryStack;
    private final IMemorySpace globalSpace;
    private boolean hitReturnStmt;

    private SymbolTable symbolTable() {
        return environment.getSymbolTable();
    }

    private IMemorySpace currentMemorySpace() {
        return this.memoryStack.peek();
    }

    private static final String RETURN_VALUE_NAME = "$return_value$";

    // TODO: add entry-point for game-object traversal

    /** Constructor. */
    public DSLInterpreter() {
        memoryStack = new ArrayDeque<>();
        globalSpace = new MemorySpace();
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
                if (creationAstNode.type.equals(Node.Type.GameObjectDefinition)) {
                    var prototype = new Prototype((AggregateType) type);

                    var gameObjDefNode = (EntityTypeDefinitionNode) creationAstNode;
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
        this.environment = new RuntimeEnvironment(environment);

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
        } else {
            AggregateValue value = new AggregateValue(type, currentMemorySpace());

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

    protected Value instantiate(AggregateType type) {
        AggregateValue instance = new AggregateValue(type, currentMemorySpace());

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
    protected Value instantiate(Prototype prototype) {
        // create memory space to store the values in
        AggregateValue instance = new AggregateValue(prototype, currentMemorySpace());

        // TODO: how to handle function calls here?
        //  we should evaluate functions as soon as possible, and only allow
        //  functions as objects to be passed to members, which actually expect a
        //  callback function
        IMemorySpace memorySpace = instance.getMemorySpace();
        this.memoryStack.push(memorySpace);
        var internalType = (AggregateType) prototype.getDataType();
        for (var member : internalType.getSymbols()) {
            // check, if type defines default for member
            var defaultValue = prototype.getDefaultValue(member.getName());
            if (defaultValue instanceof Prototype) {
                defaultValue = instantiate((Prototype) defaultValue);
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
            returnType = ((Prototype) returnType).getDataType();
        }
        return (AggregateType) returnType;
    }

    // this is the evaluation side of things
    @Override
    public Object visit(EntityTypeDefinitionNode node) {
        var prototype = this.environment.lookupPrototype(node.getIdName());
        var instance = (AggregateValue) instantiate(prototype);

        // instantiate entity_type
        TypeInstantiator typeInstantiator = new TypeInstantiator();
        var resolvedType = this.symbolTable().getGlobalScope().resolve("entity");
        var type = (AggregateType) resolvedType;
        var entityObject = typeInstantiator.instantiate(type, instance.getMemorySpace());

        // TODO: this should be done automatically in the TypeInstantiator, this is a proof of
        // concept
        // push entity as context in TypeInstantiator
        var annot = type.getOriginType().getAnnotation(DSLContextPush.class);
        if (annot != null) {
            String contextName =
                    annot.name().equals("") ? type.getOriginType().getName() : annot.name();
            typeInstantiator.pushContextMember(contextName, entityObject);
        }

        AggregateValue entityValue = new AggregateValue(type, currentMemorySpace(), entityObject);

        // an entity-object itself has no members, so add the components as "artificial members"
        // to the aggregate dsl value of the entity
        for (var memberEntry : instance.getValueSet()) {
            String memberName = memberEntry.getKey();
            Value memberValue = memberEntry.getValue();
            if (memberValue instanceof AggregateValue) {
                // TODO: this is needed, because Prototype does not extend AggregateType currently,
                //  which should be fixed
                AggregateType membersOriginalType =
                        getOriginalTypeOfPrototype((Prototype) memberValue.getDataType());

                // instantiate object as a new java Object
                Object memberObject =
                        typeInstantiator.instantiate(
                                membersOriginalType,
                                ((AggregateValue) memberValue).getMemorySpace());

                // put the memberObject inside an encapsulated memory space
                EncapsulatedObject encapsulatedObject =
                        new EncapsulatedObject(
                                memberObject,
                                membersOriginalType,
                                currentMemorySpace(),
                                this.environment);

                // add the memory space to an aggregateValue
                AggregateValue aggregateMemberValue =
                        new AggregateValue(
                                memberValue.getDataType(), currentMemorySpace(), memberObject);

                // TODO: this is a temporary fix; an AggregateValue with an encapsulated object as a
                //  memory space should be a separate class
                aggregateMemberValue.setMemorySpace(encapsulatedObject);

                entityValue.getMemorySpace().bindValue(memberName, aggregateMemberValue);
            }
        }
        return entityValue;
    }

    @Override
    public Object visit(ObjectDefNode node) {
        // resolve name of object in memory space
        IMemorySpace ms;
        var objectsValue = currentMemorySpace().resolve(node.getIdName());
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
            return ti.instantiate((AggregateType) type, ms);
        }
        return null;
    }

    @Override
    public Object visit(AggregateValueDefinitionNode node) {
        // create instance of dsl data type
        var type = this.symbolTable().getGlobalScope().resolve(node.getIdName());
        assert type instanceof AggregateType;

        var value = (AggregateValue) instantiate((AggregateType) type);

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

        return creationASTNode.accept(this);
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

        // bind all parameter-symbols as values in the function's memory space and set their values
        var parameterSymbols = symbol.getSymbols();
        for (int i = 0; i < parameterNodes.size(); i++) {
            var parameterSymbol = parameterSymbols.get(i);
            bindFromSymbol(parameterSymbol, memoryStack.peek());

            var paramValueNode = parameterNodes.get(i);
            var paramValue = paramValueNode.accept(this);

            setValue(parameterSymbol.getName(), paramValue);
        }

        // create and bind the return value
        var functionType = (FunctionType) symbol.getDataType();
        if (functionType.getReturnType() != BuiltInType.noType) {
            var returnValue = createDefaultValue(functionType.getReturnType());
            memoryStack.peek().bindValue(RETURN_VALUE_NAME, returnValue);
        }

        // visit function AST
        var funcRootNode = symbol.getAstRootNode();
        var stmtList = funcRootNode.getStmts();

        // reset return stmt flag
        this.hitReturnStmt = false;

        // execute function's statements one by one
        for (var stmt : stmtList) {
            stmt.accept(this);
            // check, if a return statement was hit
            // if so: stop function execution
            if (hitReturnStmt) {
                hitReturnStmt = false;
                break;
            }
        }

        memoryStack.pop();
        if (functionType.getReturnType() != BuiltInType.noType) {
            return functionMemSpace.resolve(RETURN_VALUE_NAME);
        }
        return Value.NONE;
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
            var dslType = TypeBuilder.getDSLTypeForClass(valueClass);
            if (dslType == null) {
                // lookup the objects type in the java to dsl type map (created during type
                // building)
                dslType = this.environment.javaTypeToDSLTypeMap().get(valueClass);
                if (dslType == null) {
                    throw new RuntimeException(
                            "No DSL Type representation for java type '" + valueClass + "'");
                }
            }
            returnValue = new Value(dslType, returnValue);
        }
        return returnValue;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        Value value = (Value) node.getInnerStmtNode().accept(this);

        // walk the memorystack, find the first return value
        // and set it according to the evaluated value
        for (var ms : this.memoryStack) {
            Value returnValue = ms.resolve(RETURN_VALUE_NAME);
            if (returnValue != Value.NONE) {
                returnValue.setInternalValue(value.getInternalObject());
                break;
            }
        }

        // signal, that a return statement was hit
        this.hitReturnStmt = true;
        return null;
    }
}
