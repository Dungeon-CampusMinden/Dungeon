package interpreter;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import dslToGame.QuestConfig;
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
import runtime.MemorySpace;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import runtime.Value;
// CHECKSTYLE:ON: AvoidStarImport
import symboltable.*;

// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DSLInterpreter implements AstVisitor<Object> {

    private QuestConfigBuilder questConfigBuilder;
    private SymbolTable symbolTable;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace globalSpace;

    // TODO: add entry-point for game-object traversal

    /** Constructor. */
    public DSLInterpreter() {
        memoryStack = new Stack<>();
        globalSpace = new MemorySpace();
        memoryStack.push(globalSpace);
    }

    // TODO: how to handle globally defined objects?
    //  statisch alles auswerten, was geht? und dann erst auswerten, wenn abgefragt (lazyeval?)
    //  wie wird order of operation vorgegeben? einfach von oben nach unten? oder nach referenz von
    //  objekt?

    /**
     * Binds all function definitions and object definitions in a global memory space.
     *
     * @param symbolTable The symbol table to bind the functions and objects from.
     */
    public void initializeRuntime(SymbolTable symbolTable) {
        // bind all function definition and object definition symbols to objects
        // in global memorySpace
        for (var symbol : symbolTable.getGlobalScope().getSymbols()) {
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
        var result = symTableParser.walk(programAST);
        symbolTable = result.symbolTable;

        initializeRuntime(symbolTable);
        var questConfig = generateQuestConfig(programAST, result.symbolTable);
        return questConfig;
    }

    private dslToGame.QuestConfig generateQuestConfig(Node programAST, SymbolTable symbolTable) {
        this.questConfigBuilder = new QuestConfigBuilder();
        this.symbolTable = symbolTable;

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
        return null;
        // return this.questConfigBuilder.build();
    }

    @Override
    public Object visit(ObjectDefNode node) {
        // push new memory space
        var objectDefSpace = new MemorySpace(this.memoryStack.peek());
        memoryStack.push(objectDefSpace);

        // bind new value for every property
        for (var propDefNode : node.getPropertyDefinitions()) {
            var propertyId = ((PropertyDefNode) propDefNode).getIdNode();
            var propDefSymbol = this.symbolTable.getSymbolsForAstNode(propertyId).get(0);
            if (propDefSymbol == Symbol.NULL) {
                // TODO: handle
            } else {
                objectDefSpace.bindFromSymbol(propDefSymbol);
            }
        }

        // accept every propertyDefinition
        for (var propDefNode : node.getPropertyDefinitions()) {
            propDefNode.accept(this);
        }

        // convert from memorySpace to concrete object
        objectDefSpace = memoryStack.pop();
        var objectSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
        return createObjectFromMemorySpace(objectDefSpace, objectSymbol.getDataType());
    }

    // TODO: refactor - likely to be refactored, when component based architecture will
    //  be implemented
    private Object createObjectFromMemorySpace(MemorySpace ms, IType type) {
        if (type.getName().equals("quest_config")) {
            QuestConfigBuilder builder = new QuestConfigBuilder();
            for (var keyValue : ms.getAllValues()) {
                var value = keyValue.getValue();
                switch (keyValue.getKey()) {
                    case "level_graph":
                        try {
                            graph.Graph<String> graphValue =
                                    (graph.Graph<String>) value.getInternalValue();
                            builder.setGraph(graphValue);
                        } catch (ClassCastException ex) {
                            // oh well
                        }
                        break;
                    case "quest_points":
                        try {
                            int intValue = (int) value.getInternalValue();
                            builder.setPoints(intValue);
                        } catch (ClassCastException ex) {
                            // oh well
                        }
                        break;
                    case "password":
                        try {
                            String strValue = (String) value.getInternalValue();
                            builder.setPassword(strValue);
                        } catch (ClassCastException ex) {
                            // oh well
                        }
                        break;
                    case "quest_desc":
                        try {
                            String strValue = (String) value.getInternalValue();
                            builder.setDescription(strValue);
                        } catch (ClassCastException ex) {
                            // oh well
                        }
                        break;
                    default:
                        break;
                }
            }
            return builder.build();
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

        var symbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
        var creationASTNode = this.symbolTable.getCreationAstNode(symbol);

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
        var funcAstNode = this.symbolTable.getCreationAstNode(symbol);
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
        var funcSymbol = this.symbolTable.getSymbolByIdx(funcValue.getSymbolIdx());
        assert funcSymbol instanceof ICallable;
        var funcCallable = (ICallable) funcSymbol;

        return funcCallable.call(this, node.getParameters());
    }
}
