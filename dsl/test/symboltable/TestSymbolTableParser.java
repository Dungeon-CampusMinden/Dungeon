package symboltable;

import helpers.Helpers;
import org.junit.Assert;
import org.junit.Test;
import parser.AST.Node;
import runtime.nativeFunctions.NativePrint;

public class TestSymbolTableParser {

    @Test
    public void testSymbolName() {
        String program =
                """
                graph g {
                    A -- B
                }
                quest_config c {
                    level_graph: g
                }
                """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        // check the name of the symbol corresponding to the graph definition
        var graphDefAstNode = ast.getChild(0);
        var symbolForDotDefNode =
                symtableResult.symbolTable.getSymbolsForAstNode(graphDefAstNode).get(0);
        Assert.assertEquals("g", symbolForDotDefNode.name);

        // check the name of the symbol corresponding to the object definition
        var objDefNode = ast.getChild(1);
        var symbolForObjDefNode =
                symtableResult.symbolTable.getSymbolsForAstNode(objDefNode).get(0);
        Assert.assertEquals("c", symbolForObjDefNode.name);
    }

    @Test
    public void testSymbolReference() {
        String program =
                """
                graph g {
                    A -- B
                }
                quest_config c {
                    level_graph: g
                }
                """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        // check the name of the symbol corresponding to the graph definition
        var graphDefAstNode = ast.getChild(0);
        var symbolForDotDefNode =
                symtableResult.symbolTable.getSymbolsForAstNode(graphDefAstNode).get(0);

        // check, if the stmt of the propertyDefinition references the symbol of the graph
        // definition
        var objDefNode = ast.getChild(1);
        var propertyDefList = objDefNode.getChild(2);

        var firstPropertyDef = propertyDefList.getChild(0);
        var firstPropertyStmtNode = firstPropertyDef.getChild(1);
        assert (firstPropertyStmtNode.type == Node.Type.Identifier);
        var symbolForStmtNode =
                symtableResult.symbolTable.getSymbolsForAstNode(firstPropertyStmtNode).get(0);
        Assert.assertEquals("g", symbolForStmtNode.name);
        Assert.assertEquals(symbolForDotDefNode, symbolForStmtNode);
    }

    @Test
    public void testSetupNativeFunctions() {
        String program =
                """
        quest_config c {
            points: print("Hello")
        }
        """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var printFuncDefSymbol = symtableResult.symbolTable.globalScope.resolve("print");
        Assert.assertNotNull(printFuncDefSymbol);
        Assert.assertEquals(Symbol.Type.Scoped, printFuncDefSymbol.getSymbolType());
        Assert.assertTrue(printFuncDefSymbol instanceof NativePrint);
    }

    @Test
    public void testResolveNativeFunction() {
        String program =
                """
        quest_config c {
            points: print("Hello")
        }
        """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var printFuncDefSymbol = symtableResult.symbolTable.globalScope.resolve("print");

        var questConfig = ast.getChild(0);
        var propDefList = questConfig.getChild(2);
        var propDef = propDefList.getChild(0);
        var funcCallNode = propDef.getChild(1);

        Assert.assertEquals(Node.Type.FuncCall, funcCallNode.type);

        var symbolForFuncCallNode =
                symtableResult.symbolTable.getSymbolsForAstNode(funcCallNode).get(0);
        Assert.assertEquals(symbolForFuncCallNode, printFuncDefSymbol);
    }

    @Test
    public void testPropertyReference() {
        String program =
                """
            graph g {
                A -- B
            }
            quest_config c {
                level_graph: g
            }
            """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var objDefNode = ast.getChild(1);
        var propertyDefList = objDefNode.getChild(2);

        var firstPropertyDef = propertyDefList.getChild(0);
        var firstPropertyIdNode = firstPropertyDef.getChild(0);
        assert (firstPropertyIdNode.type == Node.Type.Identifier);

        var questConfigType = symtableResult.symbolTable.globalScope.resolve("quest_config");
        var levelGraphPropertySymbol = ((AggregateType) questConfigType).resolve("level_graph");
        Assert.assertNotEquals(Symbol.NULL, levelGraphPropertySymbol);

        var symbolForPropertyIdNode =
                symtableResult.symbolTable.getSymbolsForAstNode(firstPropertyIdNode).get(0);

        Assert.assertEquals(levelGraphPropertySymbol, symbolForPropertyIdNode);
    }
}
