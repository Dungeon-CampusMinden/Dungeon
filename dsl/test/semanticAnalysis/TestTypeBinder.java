package semanticAnalysis;

import helpers.Helpers;
import org.junit.Assert;
import org.junit.Test;
import parser.AST.GameObjectDefinitionNode;
import runtime.GameEnvironment;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;
import semanticAnalysis.types.TypeBuilder;

public class TestTypeBinder {
    @DSLType
    private record TestComponent(@DSLTypeMember int member1, @DSLTypeMember String member2) {}

    @Test
    public void testAggregateTypeBinding() {
        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);

        String program =
                """
            game_object o {
                test_component{
                    member1: 42,
                    member2: "Hello"
                }
            }
            """;

        var ast = Helpers.getASTFromString(program);
        var symTableParser = new SymbolTableParser();

        var env = new GameEnvironment();
        var types = new AggregateType[] {testCompType};
        env.loadTypes(types);
        symTableParser.setup(env);

        SymbolTable symbolTable = symTableParser.walk(ast).symbolTable;

        // test, that type 'o' was correctly bound in global scope
        var gameObjectDefinition = symbolTable.globalScope.resolve("o");
        Assert.assertNotSame(Symbol.NULL, gameObjectDefinition);
        Assert.assertTrue(gameObjectDefinition instanceof AggregateType);

        var testComponent = ((AggregateType) gameObjectDefinition).resolve("test_component");
        Assert.assertNotSame(Symbol.NULL, testComponent);
        var testComponentDataType = symbolTable.globalScope.resolve("test_component");
        Assert.assertEquals(testComponentDataType, testComponent.getDataType());

        var member1 = ((AggregateType) testComponentDataType).resolve("member1");
        Assert.assertNotSame(Symbol.NULL, member1);
        var member2 = ((AggregateType) testComponentDataType).resolve("member2");
        Assert.assertNotSame(Symbol.NULL, member2);
    }

    @Test
    public void testAggregateTypeBindingAstNodeRelation() {
        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);

        String program =
                """
        game_object o {
            test_component{
                member1: 42,
                member2: "Hello"
            }
        }
        """;

        var ast = Helpers.getASTFromString(program);
        var symTableParser = new SymbolTableParser();

        var env = new GameEnvironment();
        var types = new AggregateType[] {testCompType};
        env.loadTypes(types);
        symTableParser.setup(env);

        SymbolTable symbolTable = symTableParser.walk(ast).symbolTable;

        // check, that the creation node of the datatype matches the AST node
        var gameObjectDefinition = symbolTable.globalScope.resolve("o");
        var gameObjectDefNode = symbolTable.getCreationAstNode(gameObjectDefinition);
        var gameObjectDefNodeFromAST = ast.getChild(0);
        Assert.assertEquals(gameObjectDefNodeFromAST, gameObjectDefNode);

        // check, that the creation node of the datatype-member matches the AST node
        var testComponent = ((AggregateType) gameObjectDefinition).resolve("test_component");
        var testComponentDefNode = symbolTable.getCreationAstNode(testComponent);
        var testComponentDefNodeFromAST =
                ((GameObjectDefinitionNode) gameObjectDefNodeFromAST)
                        .getComponentDefinitionNodes()
                        .get(0);
        Assert.assertEquals(testComponentDefNodeFromAST, testComponentDefNode);
    }
}
