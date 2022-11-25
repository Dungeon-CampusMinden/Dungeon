package interpreter;

import antlr.main.*;
import dslToGame.QuestConfigBuilder;
import interpreter.dot.Interpreter;
import java.util.Stack;
import org.antlr.v4.runtime.*;
import parser.AST.*;
import parser.DungeonASTConverter;
import runtime.MemorySpace;
import symboltable.ICallable;
import symboltable.SymbolTable;
import symboltable.SymbolTableParser;

public class DSLInterpreter implements AstVisitor<Object> {

    private QuestConfigBuilder questConfigBuilder;
    private SymbolTable symbolTable;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace globalSpace;

    // TODO: add entry-point for game-object traversal
    public DSLInterpreter() {
        memoryStack = new Stack<>();
        globalSpace = new MemorySpace();
        memoryStack.push(globalSpace);
    }

    // TODO: how to handle globally defined objects?
    //  statisch alles auswerten, was geht? und dann erst auswerten, wenn abgefragt (lazyeval?)
    //  wie wird order of operation vorgegeben? einfach von oben nach unten? oder nach referenz von
    //  objekt?
    // TODO: associate object in memorySpace with symbol(by symbol idx?)!!
    //  We could assume, that
    //  the memory space just mirrors the structure of the symbol table, but it's
    //  better to be specific and somehow self-contained in this context
    public void initializeRuntime(SymbolTable symbolTable) {
        // bind all function definition and object definition symbols to objects
        // in global memorySpace
        for (var symbol : symbolTable.GetGlobalScope().GetSymbols()) {
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
        System.out.println("Test");
    }

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

        var questConfig = generateQuestConfig(programAST, result.symbolTable);
        initializeRuntime(symbolTable);
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
                    // interpret everything -> set fields in questConfig object
                    for (var propertyDef : objDefNode.getPropertyDefinitions()) {
                        propertyDef.accept(this);
                    }
                }
                break;
            }
        }
        return this.questConfigBuilder.build();
    }

    @Override
    public Object visit(PropertyDefNode node) {
        var value = node.getStmtNode().accept(this);

        // TODO: this should not be done here; handle property-definitions like
        //  MemorySpace; create class, which handles building the "end-user"-class
        //  (the conrete QuestConfig-object) from the propertyDefinitions
        switch (node.getIdName()) {
            case "level_graph":
                try {
                    graph.Graph<String> graphValue = (graph.Graph<String>) value;
                    this.questConfigBuilder.setGraph(graphValue);
                } catch (ClassCastException ex) {
                    // oh well
                }
                break;
            case "quest_points":
                try {
                    int intValue = (int) value;
                    this.questConfigBuilder.setPoints(intValue);
                } catch (ClassCastException ex) {
                    // oh well
                }
                break;
            case "password":
                try {
                    String strValue = (String) value;
                    this.questConfigBuilder.setPassword(strValue);
                } catch (ClassCastException ex) {
                    // oh well
                }
                break;
            case "quest_desc":
                try {
                    String strValue = (String) value;
                    this.questConfigBuilder.setDescription(strValue);
                } catch (ClassCastException ex) {
                    // oh well
                }
                break;
            default:
                break;
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
}
