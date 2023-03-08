package semanticAnalysis;

import dslToGame.graph.Graph;
import helpers.Helpers;
import org.junit.Assert;
import org.junit.Test;
import parser.AST.GameObjectDefinitionNode;
import parser.AST.Node;
import runtime.GameEnvironment;
import runtime.nativeFunctions.NativePrint;
import semanticAnalysis.types.*;

public class TestSymbolTableParser {

    /** Test, if the name of symbols is set correctly */
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

    @DSLType
    private record TestComponent(@DSLTypeMember Graph<String> levelGraph) {}

    /**
     * Test, if the reference to a symbol is correctly resolved and that the symbol is linked to the
     * identifier
     */
    @Test
    public void testSymbolReferenceComponent() {
        String program =
                """
            graph g {
                A -- B
            }

            game_object c {
                test_component{
                    level_graph: g
                }
            }
            """;

        // setup
        var ast = Helpers.getASTFromString(program);
        SymbolTableParser symbolTableParser = new SymbolTableParser();

        TypeBuilder tb = new TypeBuilder();
        var testComponentType = tb.createTypeFromClass(Scope.NULL, TestComponent.class);

        var env = new GameEnvironment();
        env.loadTypes(new IType[] {testComponentType});
        symbolTableParser.setup(env);
        var symbolTable = symbolTableParser.walk(ast).symbolTable;

        // check the name of the symbol corresponding to the graph definition
        var graphDefAstNode = ast.getChild(0);
        var symbolForDotDefNode = symbolTable.getSymbolsForAstNode(graphDefAstNode).get(0);

        // check, if the stmt of the propertyDefinition references the symbol of the graph
        // definition
        var gameObjDefNode = ast.getChild(1);
        var componentDefNode =
                ((GameObjectDefinitionNode) gameObjDefNode).getComponentDefinitionNodes().get(0);
        var propertyDefList = componentDefNode.getChild(1);

        var firstPropertyDef = propertyDefList.getChild(0);
        var firstPropertyStmtNode = firstPropertyDef.getChild(1);
        assert (firstPropertyStmtNode.type == Node.Type.Identifier);
        var symbolForStmtNode = symbolTable.getSymbolsForAstNode(firstPropertyStmtNode).get(0);
        Assert.assertEquals("g", symbolForStmtNode.name);
        Assert.assertEquals(symbolForDotDefNode, symbolForStmtNode);
    }

    /**
     * Test, if the reference to a symbol is correctly resolved and that the symbol is linked to the
     * identifier
     */
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

    /** Test, if native functions are correctly setup and linked to function call */
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

    /** Test, if a native function call is correctly resolved */
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

    // TODO: is this even correct? should it be linked? this currently prevents
    //  multiple instances of the same datatype...
    /**
     * Test, if symbol of property of aggregate datatype is correctly linked to the symbol inside of
     * the datatype
     */
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
            quest_config d {
                level_graph: g
            }
                """;

        // generate symbol table
        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        // get property definition list of the object definition
        var objDefNode = ast.getChild(1);
        var propertyDefList = objDefNode.getChild(2);

        // get the first property definition of the property definition list
        var firstPropertyDef = propertyDefList.getChild(0);
        var firstPropertyIdNode = firstPropertyDef.getChild(0);
        assert (firstPropertyIdNode.type == Node.Type.Identifier);

        // resolve 'level_graph' property of quest_config type in the datatype
        var questConfigType = symtableResult.symbolTable.globalScope.resolve("quest_config");
        var levelGraphPropertySymbol = ((AggregateType) questConfigType).resolve("level_graph");
        Assert.assertNotEquals(Symbol.NULL, levelGraphPropertySymbol);

        var symbolForPropertyIdNode =
                symtableResult.symbolTable.getSymbolsForAstNode(firstPropertyDef).get(0);

        Assert.assertEquals(levelGraphPropertySymbol, symbolForPropertyIdNode);
    }
}
