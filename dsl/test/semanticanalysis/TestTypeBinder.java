package semanticanalysis;

import helpers.Helpers;

import org.junit.Assert;
import org.junit.Test;

import parser.ast.EntityTypeDefinitionNode;

import runtime.GameEnvironment;

import semanticanalysis.types.*;

public class TestTypeBinder {
    @DSLType
    private record TestComponent(
            @DSLTypeMember int member1,
            @DSLTypeMember String member2,
            @DSLTypeMember float member3) {}

    @Test
    public void testAggregateTypeBinding() {
        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);

        String program =
                """
            entity_type o {
                test_component{
                    member1: 42,
                    member2: "Hello",
                    member3: 3.14
                }
            }
            """;

        var ast = Helpers.getASTFromString(program);
        var symTableParser = new SemanticAnalyzer();

        var env = new GameEnvironment();
        env.loadTypes(testCompType);
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
        var member3 = ((AggregateType) testComponentDataType).resolve("member3");
        Assert.assertNotSame(Symbol.NULL, member3);
    }

    @Test
    public void testAggregateTypeBindingAstNodeRelation() {
        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);

        String program =
                """
        entity_type o {
            test_component{
                member1: 42,
                member2: "Hello"
            }
        }
        """;

        var ast = Helpers.getASTFromString(program);
        var symTableParser = new SemanticAnalyzer();

        var env = new GameEnvironment();
        env.loadTypes(testCompType);
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
                ((EntityTypeDefinitionNode) gameObjectDefNodeFromAST)
                        .getComponentDefinitionNodes()
                        .get(0);
        Assert.assertEquals(testComponentDefNodeFromAST, testComponentDefNode);
    }

    @Test
    public void testAdapterBinding() {

        String program =
                """
            entity_type o {
                test_record_user {
                    component_member: "Hello"
                }
            }
            """;

        var ast = Helpers.getASTFromString(program);
        var symTableParser = new SemanticAnalyzer();

        var env = new GameEnvironment();

        env.getTypeBuilder().registerTypeAdapter(RecordBuilder.class, Scope.NULL);
        var type = env.getTypeBuilder().createTypeFromClass(new Scope(), TestRecordUser.class);

        env.loadTypes(type);
        symTableParser.setup(env);

        SymbolTable symbolTable = symTableParser.walk(ast).symbolTable;

        var gameObjectDefinition = symbolTable.globalScope.resolve("o");
        var testRecordUser = ((AggregateType) gameObjectDefinition).resolve("test_record_user");
        var testRecordUserType = (AggregateType) testRecordUser.getDataType();
        var member = testRecordUserType.resolve("component_member");
        var memberType = member.getDataType();
        Assert.assertTrue(memberType instanceof AdaptedType);

        var adaptedType = (AdaptedType) memberType;
        Assert.assertEquals(TestRecordComponent.class, adaptedType.getOriginType());
        Assert.assertEquals(BuiltInType.stringType, adaptedType.getBuildParameterType());
    }
}
