package dsl.semanticanalysis;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import dsl.helpers.Helpers;
import dsl.parser.ast.PrototypeDefinitionNode;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.analyzer.TypeBinder;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.*;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateTypeAdapter;
import org.junit.Assert;
import org.junit.Test;

/** WTF? . */
public class TestTypeBinder {
  @DSLType
  private record TestComponent(
      @DSLTypeMember int member1, @DSLTypeMember String member2, @DSLTypeMember float member3) {}

  /** WTF? . */
  @Test
  public void testAggregateTypeBinding() {
    TypeBuilder tb = new TypeBuilder();
    var testCompType = tb.createDSLTypeForJavaTypeInScope(new Scope(), TestComponent.class);

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

  /** WTF? . */
  @Test
  public void testAggregateTypeBindingAstNodeRelation() {
    TypeBuilder tb = new TypeBuilder();
    var testCompType = tb.createDSLTypeForJavaTypeInScope(new Scope(), TestComponent.class);

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
        ((PrototypeDefinitionNode) gameObjectDefNodeFromAST).getComponentDefinitionNodes().get(0);
    Assert.assertEquals(testComponentDefNodeFromAST, testComponentDefNode);
  }

  /** WTF? . */
  @Test
  public void testAdapterBinding() {

    String program =
        """
            entity_type o {
                test_record_user {
                    component_member: test_record_component { param: "Hello"}
                }
            }
            """;

    var ast = Helpers.getASTFromString(program);
    var symTableParser = new SemanticAnalyzer();

    var env = new GameEnvironment();

    env.getTypeBuilder().registerTypeAdapter(RecordBuilder.class, env.getGlobalScope());
    var type =
        env.getTypeBuilder().createDSLTypeForJavaTypeInScope(new Scope(), TestRecordUser.class);

    env.loadTypes(type);
    symTableParser.setup(env);

    SymbolTable symbolTable = symTableParser.walk(ast).symbolTable;

    var gameObjectDefinition = symbolTable.globalScope.resolve("o");
    var testRecordUser = ((AggregateType) gameObjectDefinition).resolve("test_record_user");
    var testRecordUserType = (AggregateType) testRecordUser.getDataType();
    var member = testRecordUserType.resolve("component_member");
    var memberType = member.getDataType();
    Assert.assertTrue(memberType instanceof AggregateTypeAdapter);

    var adaptedType = (AggregateTypeAdapter) memberType;
    Assert.assertEquals(TestRecordComponent.class, adaptedType.getOriginType());
  }

  /** WTF? . */
  @Test
  public void testSetTypeBinding() {
    TypeBuilder typeBuilder = new TypeBuilder();
    var testCompType =
        typeBuilder.createDSLTypeForJavaTypeInScope(new Scope(), TestComponent.class);

    String program =
        """
        entity_type o {
            test_component{
                member1: 42,
                member2: "Hello",
                member3: 3.14
            }
        }

        fn test(inventory_component<> compSet) -> bool<> {
            var floatSetSet : float<><>;
        }
        """;

    var ast = Helpers.getASTFromString(program);
    var symTableParser = new SemanticAnalyzer();

    var env = new GameEnvironment();
    env.loadTypes(testCompType);
    symTableParser.setup(env);

    TypeBinder typeBinder = new TypeBinder();
    typeBinder.bindTypes(env, ast, new StringBuilder());
    SymbolTable symTable = env.getSymbolTable();

    IScope globalScope = symTable.globalScope();

    Assert.assertNotSame(Symbol.NULL, globalScope.resolve("bool<>"));
    Assert.assertNotSame(Symbol.NULL, globalScope.resolve("float<><>"));
    Assert.assertNotSame(Symbol.NULL, globalScope.resolve("inventory_component<>"));
  }
}
