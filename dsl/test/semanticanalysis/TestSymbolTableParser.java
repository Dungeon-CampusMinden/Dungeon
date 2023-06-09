package semanticanalysis;

import dslToGame.graph.Graph;

import helpers.Helpers;

import interpreter.DummyNativeFunction;
import interpreter.TestEnvironment;

import org.junit.Assert;
import org.junit.Test;

import parser.ast.EntityTypeDefinitionNode;
import parser.ast.Node;

import runtime.GameEnvironment;
import runtime.nativefunctions.NativePrint;

import semanticanalysis.types.*;

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

                entity_type c {
                    test_component{
                        level_graph: g
                    }
                }
                """;

        // setup
        var ast = Helpers.getASTFromString(program);
        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();

        TypeBuilder tb = new TypeBuilder();
        var testComponentType = tb.createTypeFromClass(Scope.NULL, TestComponent.class);

        var env = new GameEnvironment();
        env.loadTypes(testComponentType);
        symbolTableParser.setup(env);
        var symbolTable = symbolTableParser.walk(ast).symbolTable;

        // check the name of the symbol corresponding to the graph definition
        var graphDefAstNode = ast.getChild(0);
        var symbolForDotDefNode = symbolTable.getSymbolsForAstNode(graphDefAstNode).get(0);

        // check, if the stmt of the propertyDefinition references the symbol of the graph
        // definition
        var gameObjDefNode = ast.getChild(1);
        var componentDefNode =
                ((EntityTypeDefinitionNode) gameObjDefNode).getComponentDefinitionNodes().get(0);
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

    /** Test, if a native function call is correctly resolved */
    @Test
    public void funcDef() {
        String program =
                """
                fn test_func(int param1, float param2, string param3) -> int {
                    print(param1);
                }
                """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var funcSymbol =
                (FunctionSymbol) symtableResult.symbolTable.globalScope.resolve("test_func");
        Assert.assertEquals("test_func", funcSymbol.getName());

        IType functionType =
                (IType)
                        symtableResult.symbolTable.globalScope.resolve(
                                "$fn(int, float, string) -> int$");
        Assert.assertEquals(functionType, funcSymbol.getDataType());
        Assert.assertEquals(ICallable.Type.UserDefined, funcSymbol.getCallableType());
        Assert.assertNotEquals(Symbol.NULL, funcSymbol.resolve("param1"));
        Assert.assertNotEquals(Symbol.NULL, funcSymbol.resolve("param2"));
        Assert.assertNotEquals(Symbol.NULL, funcSymbol.resolve("param3"));
    }

    @Test
    public void resolveParameterInFunctionBody() {
        String program =
                """
                fn test_func(int param1, float param2, string param3) -> int {
                    print(param1);
                }
                """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var funcDefNode = ast.getChild(0);
        var stmtList = funcDefNode.getChild(3);
        var funcCallStmt = stmtList.getChild(0);
        var paramList = funcCallStmt.getChild(1);
        var firstParam = paramList.getChild(0);

        var symbolForParam1 = symtableResult.symbolTable.getSymbolsForAstNode(firstParam).get(0);
        var funcDef = symtableResult.symbolTable.globalScope.resolve("test_func");
        var parameterSymbolFromFunctionSymbol = ((FunctionSymbol) funcDef).resolve("param1");
        Assert.assertEquals(parameterSymbolFromFunctionSymbol, symbolForParam1);
    }

    @Test
    public void funcDefFuncType() {
        String program =
                """
                fn test_func_1(int param1, float param2, string param3) -> int {
                    print(param1);
                }
                fn test_func_2(int param4, float param5, string param6) -> int {
                    print(param4);
                }
                """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var funcSymbol1 =
                (FunctionSymbol) symtableResult.symbolTable.globalScope.resolve("test_func_1");
        var funcSymbol2 =
                (FunctionSymbol) symtableResult.symbolTable.globalScope.resolve("test_func_2");
        Assert.assertEquals(funcSymbol1.getDataType(), funcSymbol2.getDataType());
    }

    @Test
    public void funcTypeObjectEquality() {
        String program =
                """
            fn test_func_1(int param1, float param2, string param3) -> int {
                print(param1);
            }
            fn test_func_2(int param4, float param5, string param6) -> int {
                print(param4);
            }
            """;

        var ast = Helpers.getASTFromString(program);
        var symtableResult = Helpers.getSymtableForAST(ast);

        var funcSymbol1 =
                (FunctionSymbol) symtableResult.symbolTable.globalScope.resolve("test_func_1");
        var funcType1 = funcSymbol1.getDataType();
        var funcSymbol2 =
                (FunctionSymbol) symtableResult.symbolTable.globalScope.resolve("test_func_2");
        var funcType2 = funcSymbol2.getDataType();
        Assert.assertEquals(funcType1.hashCode(), funcType2.hashCode());
    }

    @Test
    public void funcTypeNativeFunction() {
        var env = new GameEnvironment();

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);

        var symbolTableParserEnvironment = symbolTableParser.getEnvironment();
        var nativePrintSymbol = symbolTableParserEnvironment.getGlobalScope().resolve("print");
        Assert.assertTrue(nativePrintSymbol.getDataType() instanceof FunctionType);
    }

    @Test
    public void removeFuncTypeRedundancy() {
        String program = """
        fn test_func_1(string param) -> int {

        }
        """;

        var env = new TestEnvironment();

        // load two dummy functions with the same semantic function type as the defined function in
        // the dsl input and check, if they are all using the same FunctionType OBJECT after setup
        // of the symbolTableParser
        var dummyFunc1 =
                new DummyNativeFunction(
                        "dummyFunc1",
                        new FunctionType(BuiltInType.intType, BuiltInType.stringType));
        var dummyFunc2 =
                new DummyNativeFunction(
                        "dummyFunc2",
                        new FunctionType(BuiltInType.intType, BuiltInType.stringType));

        env.loadFunctions(dummyFunc1, dummyFunc2);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);

        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        var symbolTableParserEnvironment = symbolTableParser.getEnvironment();

        var dummyFunc1Sym = symbolTableParserEnvironment.getGlobalScope().resolve("dummyFunc1");
        var dummyFunc2Sym = symbolTableParserEnvironment.getGlobalScope().resolve("dummyFunc2");
        var testFunc1 = symbolTableParserEnvironment.getGlobalScope().resolve("test_func_1");
        Assert.assertEquals(
                dummyFunc1Sym.getDataType().hashCode(), dummyFunc2Sym.getDataType().hashCode());
        Assert.assertEquals(
                dummyFunc1Sym.getDataType().hashCode(), testFunc1.getDataType().hashCode());
    }
}
