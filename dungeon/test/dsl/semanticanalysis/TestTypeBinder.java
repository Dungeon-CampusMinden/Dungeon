package dsl.semanticanalysis;

import static org.junit.jupiter.api.Assertions.*;

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
import dsl.semanticanalysis.typesystem.RecordBuilder;
import dsl.semanticanalysis.typesystem.TestRecordComponent;
import dsl.semanticanalysis.typesystem.TestRecordUser;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateTypeAdapter;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class TestTypeBinder {
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
    assertNotSame(Symbol.NULL, gameObjectDefinition);
    assertInstanceOf(AggregateType.class, gameObjectDefinition);

    var testComponent = ((AggregateType) gameObjectDefinition).resolve("test_component");
    assertNotSame(Symbol.NULL, testComponent);
    var testComponentDataType = symbolTable.globalScope.resolve("test_component");
    assertEquals(testComponentDataType, testComponent.getDataType());

    var member1 = ((AggregateType) testComponentDataType).resolve("member1");
    assertNotSame(Symbol.NULL, member1);
    var member2 = ((AggregateType) testComponentDataType).resolve("member2");
    assertNotSame(Symbol.NULL, member2);
    var member3 = ((AggregateType) testComponentDataType).resolve("member3");
    assertNotSame(Symbol.NULL, member3);
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
    assertEquals(gameObjectDefNodeFromAST, gameObjectDefNode);

    // check, that the creation node of the datatype-member matches the AST node
    var testComponent = ((AggregateType) gameObjectDefinition).resolve("test_component");
    var testComponentDefNode = symbolTable.getCreationAstNode(testComponent);
    var testComponentDefNodeFromAST =
        ((PrototypeDefinitionNode) gameObjectDefNodeFromAST).getComponentDefinitionNodes().get(0);
    assertEquals(testComponentDefNodeFromAST, testComponentDefNode);
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
    assertInstanceOf(AggregateTypeAdapter.class, memberType);

    var adaptedType = (AggregateTypeAdapter) memberType;
    assertEquals(TestRecordComponent.class, adaptedType.getOriginType());
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

    assertNotSame(Symbol.NULL, globalScope.resolve("bool<>"));
    assertNotSame(Symbol.NULL, globalScope.resolve("float<><>"));
    assertNotSame(Symbol.NULL, globalScope.resolve("inventory_component<>"));
  }

  @DSLType
  private record TestComponent(
      @DSLTypeMember int member1, @DSLTypeMember String member2, @DSLTypeMember float member3) {}
}
