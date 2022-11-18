package interpreter;

import antlr.main.*;
import dslToGame.QuestConfigBuilder;
import interpreter.dot.Interpreter;
import org.antlr.v4.runtime.*;
import parser.AST.*;
import parser.DungeonASTConverter;
import symboltable.SymbolTable;
import symboltable.SymbolTableParser;

public class DSLInterpreter implements AstVisitor<Object> {

    QuestConfigBuilder questConfigBuilder;
    SymbolTable symbolTable;

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

        return generateQuestConfig(programAST, result.symbolTable);
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
