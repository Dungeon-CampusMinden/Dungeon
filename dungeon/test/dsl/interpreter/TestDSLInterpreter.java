package dsl.interpreter;

import static org.junit.Assert.*;

import contrib.components.CollideComponent;
import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import dsl.helpers.Helpers;
import dsl.interpreter.mockecs.*;
import dsl.parser.ast.IdNode;
import dsl.parser.ast.Node;
import dsl.runtime.memoryspace.EncapsulatedObject;
import dsl.runtime.value.AggregateValue;
import dsl.runtime.value.PrototypeValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.typesystem.*;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import dslinterop.dslnativefunction.NativeInstantiate;
import entrypoint.DungeonConfig;
import graph.taskdependencygraph.TaskDependencyGraph;
import graph.taskdependencygraph.TaskEdge;
import graph.taskdependencygraph.TaskNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import task.*;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;
import task.tasktype.AssignTask;
import task.tasktype.Element;
import task.tasktype.Quiz;

/** WTF? . */
public class TestDSLInterpreter {
  /** Tests, if a native function call is evaluated by the DSLInterpreter. */
  @Test
  public void funcCall() {
    String program =
        """
                dungeon_config c {
                    test: print("Hello, World!")
                }
                    """;
    DSLInterpreter interpreter = new DSLInterpreter();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    interpreter.getQuestConfig(program);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallReturn() {
    String program =
        """
                quest_config c {
                    test: print(testReturnHelloWorld())
                }
                    """;
    TestEnvironment env = new TestEnvironment();
    env.loadFunctions(TestFunctionReturnHelloWorld.func);

    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(env);
    var ast = Helpers.getASTFromString(program);
    symbolTableParser.walk(ast);

    DSLInterpreter interpreter = new DSLInterpreter();
    interpreter.initializeRuntime(env);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    interpreter.generateQuestConfig(ast);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallDoubleReturnUserFunc() {
    String program =
        """
                fn ret_string2() -> string {
                    return "Hello, World!";
                }

                fn ret_string1() -> string {
                    return ret_string2();
                }

                quest_config c {
                    test: print(ret_string1())
                }
                    """;
    TestEnvironment env = new TestEnvironment();
    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(env);
    var ast = Helpers.getASTFromString(program);
    symbolTableParser.walk(ast);

    DSLInterpreter interpreter = new DSLInterpreter();
    interpreter.initializeRuntime(env);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    interpreter.generateQuestConfig(ast);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallDoubleReturnUserFuncDifferentValues() {
    String program =
        """
                    fn ret_string2() -> string {
                        return "Moin";
                    }

                    fn ret_string1() -> string {
                        ret_string2();
                        return "Hello, World!";
                    }

                    quest_config c {
                        test: print(ret_string1())
                    }
                """;
    TestEnvironment env = new TestEnvironment();
    SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
    symbolTableParser.setup(env);
    var ast = Helpers.getASTFromString(program);
    symbolTableParser.walk(ast);

    DSLInterpreter interpreter = new DSLInterpreter();
    interpreter.initializeRuntime(env);

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    interpreter.generateQuestConfig(ast);

    assertTrue(outputStream.toString().contains("Hello, World!"));
    assertFalse(outputStream.toString().contains("Moin"));
  }

  /** WTF? . */
  @Test
  public void funcCallReturnUserFunc() {
    String program =
        """
                    fn ret_string() -> string {
                        return "Hello, World!";
                    }

                    quest_config c {
                        test: print(ret_string())
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(
        program, env, interpreter, TestFunctionReturnHelloWorld.func);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallReturnUserFuncNestedBlock() {
    String program =
        """
                    fn ret_string() -> string {
                        {
                            {
                                return "Hello, World!";
                            }
                        }
                    }

                    quest_config c {
                        test: print(ret_string())
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallNestedStmtBlock() {
    String program =
        """
                    fn print_string() {
                        {
                            {
                                print("Hello, World!");
                            }
                        }
                    }

                    quest_config c {
                        test: print_string()
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(
        program, env, interpreter, TestFunctionReturnHelloWorld.func);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void funcCallReturnUserFuncWithoutReturnType() {
    String program =
        """
                        fn ret_string() {
                            return "Hello, World!";
                        }

                    quest_config c {
                        test: print(ret_string())
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(
        program, env, interpreter, TestFunctionReturnHelloWorld.func);

    assertFalse(outputStream.toString().contains("Hello, World!"));
  }

  /** Test, if Value. NULL does not get set, if non-existing property of datatype is assigned. */
  @Test
  public void testDontSetNullValue() {
    String program =
        """
                dungeon_config c {
                    this_value_does_not_exist_in_type: 42
                }
                    """;
    DSLInterpreter interpreter = new DSLInterpreter();
    interpreter.getQuestConfig(program);
    assertNotSame(42, Value.NONE.getInternalValue());
  }

  /**
   * Test, if a dot definition and object definition is correctly created.
   *
   * @throws URISyntaxException if the resource URL is not valid
   * @throws IOException if the resource file does not exist
   */
  @Test
  public void questConfigHighLevel() throws URISyntaxException, IOException {
    URL resource = getClass().getClassLoader().getResource("program.ds");
    assert resource != null;
    var ast = Helpers.getASTFromResourceFile(resource);

    var firstChild = ast.getChild(0);
    assertEquals(Node.Type.DotDefinition, firstChild.type);

    var secondChild = ast.getChild(1);
    assertEquals(Node.Type.ObjectDefinition, secondChild.type);
  }

  @DSLType
  private record TestComponent(@DSLTypeMember int member1, @DSLTypeMember String member2) {}

  @DSLType
  private record OtherComponent(
      @DSLTypeMember int member3, @DSLTypeMember TaskDependencyGraph member4) {}

  /** WTF? . */
  @Test
  public void aggregateTypeWithDefaults() {
    String program =
        """
                entity_type c {
                    test_component{
                        member1: 42,
                        member2: "Hello, World!"
                    },
                    other_component{
                        member3: 314
                    }
                }
                """;

    var env = new GameEnvironment();
    var interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(
        program, env, interpreter, TestComponent.class, OtherComponent.class);

    var rtEnv = interpreter.getRuntimeEnvironment();

    var typeWithDefaults = rtEnv.lookupPrototype("c");
    assertNotEquals(PrototypeValue.NONE, typeWithDefaults);

    var firstCompWithDefaults = typeWithDefaults.getDefaultValue("test_component");
    assertNotEquals(Value.NONE, firstCompWithDefaults);
    assertTrue(firstCompWithDefaults instanceof PrototypeValue);

    var secondCompWithDefaults = typeWithDefaults.getDefaultValue("other_component");
    assertNotEquals(Value.NONE, secondCompWithDefaults);
    assertTrue(secondCompWithDefaults instanceof PrototypeValue);

    // check members of components
    var member1Value = ((PrototypeValue) firstCompWithDefaults).getDefaultValue("member1");
    assertNotEquals(Value.NONE, member1Value);
    assertEquals(BuiltInType.intType, member1Value.getDataType());
    assertEquals(42, member1Value.getInternalValue());

    var member2Value = ((PrototypeValue) firstCompWithDefaults).getDefaultValue("member2");
    assertNotEquals(Value.NONE, member2Value);
    assertEquals(BuiltInType.stringType, member2Value.getDataType());
    assertEquals("Hello, World!", member2Value.getInternalValue());
  }

  /** WTF? . */
  @Test
  public void aggregateTypeInstancing() {
    String program =
        """
                entity_type my_obj {
                    test_component1 {
                        member1: 42,
                        member2: 12.34
                    },
                    test_component2 {
                        member1: "Hallo",
                        member2: 123
                    }
                }

                quest_config config {
                    entity: instantiate(my_obj)
                }
                """;

    var env = new TestEnvironment();
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

    var interpreter = new DSLInterpreter();
    var questConfig =
        Helpers.generateQuestConfigWithCustomTypes(
            program, env, interpreter, Entity.class, TestComponent1.class, TestComponent2.class);

    var entity = ((CustomQuestConfig) questConfig).entity();
    var rtEnv = interpreter.getRuntimeEnvironment();
    var globalMs = interpreter.getGlobalMemorySpace();

    // the config should contain the my_obj definition on the entity-value, which should
    // encapsulate the actual
    // test component instances
    var config = (AggregateValue) (globalMs.resolve("config"));
    var myObj = config.getMemorySpace().resolve("entity");
    assertNotEquals(Value.NONE, myObj);
    assertTrue(myObj instanceof AggregateValue);

    // test, that the referenced entities are correct
    var testComp1Value = ((AggregateValue) myObj).getMemorySpace().resolve("test_component1");
    assertNotEquals(Value.NONE, testComp1Value);
    var testComp1EncapsulatedObj =
        (EncapsulatedObject) ((AggregateValue) testComp1Value).getMemorySpace();
    var testComp1Internal = testComp1EncapsulatedObj.getInternalValue();
    assertTrue(testComp1Internal instanceof TestComponent1);

    TestComponent1 testComp1 = (TestComponent1) testComp1Internal;
    assertEquals(entity, testComp1.getEntity());

    // check member-values
    assertEquals(42, testComp1.getMember1());
    assertEquals(12.34, testComp1.getMember2(), 0.001f);
    assertEquals("DEFAULT VALUE", testComp1.getMember3());

    // test, that the referenced entities are correct
    var testComp2Value = ((AggregateValue) myObj).getMemorySpace().resolve("test_component2");
    assertNotEquals(Value.NONE, testComp2Value);
    var testComp2EncapsulatedObj =
        (EncapsulatedObject) ((AggregateValue) testComp2Value).getMemorySpace();
    var testComp2Internal = testComp2EncapsulatedObj.getInternalValue();
    assertTrue(testComp2Internal instanceof TestComponent2);

    TestComponent2 testComp2 = (TestComponent2) testComp2Internal;
    assertEquals(entity, testComp2.getEntity());

    // check member-values
    assertEquals("Hallo", testComp2.getMember1());
    assertEquals(123, testComp2.getMember2());
    assertEquals("DEFAULT VALUE", testComp2.getMember3());
  }

  /** WTF? . */
  @Test
  public void objectEncapsulation() {
    String program =
        """
                entity_type my_obj {
                    test_component1 {
                        member1: 42,
                        member2: 12.34
                    },
                    test_component2 {
                        member1: "Hallo",
                        member2: 123
                    }
                }

                quest_config config {
                    entity: instantiate(my_obj),
                    second_entity: instantiate(my_obj)
                }
                """;

    var env = new TestEnvironment();
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

    var interpreter = new DSLInterpreter();
    var questConfig =
        Helpers.generateQuestConfigWithCustomTypes(
            program, env, interpreter, Entity.class, TestComponent1.class, TestComponent2.class);

    var entity = ((CustomQuestConfig) questConfig).entity();
    var rtEnv = interpreter.getRuntimeEnvironment();
    var globalMs = interpreter.getGlobalMemorySpace();

    // the config should contain the my_obj definition on the entity-value, which should
    // encapsulate the actual
    // test component instances
    var config = (AggregateValue) (globalMs.resolve("config"));
    var firstEntity = (AggregateValue) config.getMemorySpace().resolve("entity");
    var secondEntity = (AggregateValue) config.getMemorySpace().resolve("second_entity");

    // set values in the testComponent1 of firstEntity and check, that the members in
    // second Entity stay the same
    var firstEntitysComp1 =
        (AggregateValue) firstEntity.getMemorySpace().resolve("test_component1");
    var firstEntitysComp1Member1 = firstEntitysComp1.getMemorySpace().resolve("member1");
    firstEntitysComp1Member1.setInternalValue(123);

    var secondEntitysComp1 =
        (AggregateValue) secondEntity.getMemorySpace().resolve("test_component1");
    var secondEntitysComp1Member1 = secondEntitysComp1.getMemorySpace().resolve("member1");
    var internalValue = secondEntitysComp1Member1.getInternalValue();
    Assert.assertEquals(42, internalValue);
  }

  /** WTF? . */
  @Test
  public void aggregateTypeInstancingNonSupportedExternalType() {
    String program =
        """
                entity_type my_obj {
                    component_with_external_type_member { }
                }

                quest_config config {
                    entity: instantiate(my_obj)
                }
                """;

    var env = new TestEnvironment();
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), ComponentWithExternalTypeMember.class);
    env.getTypeBuilder()
        .bindProperty(
            env.getGlobalScope(), Entity.ComponentWithExternalTypeMemberProperty.instance);

    DSLInterpreter interpreter = new DSLInterpreter();

    Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, Entity.class);

    var globalMs = interpreter.getGlobalMemorySpace();

    // check, if the component was instantiated and the
    // Point member is set to null, because the Point type is not supported
    // by the Typesystem
    var config = (AggregateValue) (globalMs.resolve("config"));
    var myObj = config.getMemorySpace().resolve("entity");
    var component =
        ((AggregateValue) myObj).getMemorySpace().resolve("component_with_external_type_member");

    var encapsulatedObject = (EncapsulatedObject) ((AggregateValue) component).getMemorySpace();
    var internalComponent = encapsulatedObject.getInternalValue();

    assertTrue(internalComponent instanceof ComponentWithExternalTypeMember);
    assertNull(((ComponentWithExternalTypeMember) internalComponent).point);
  }

  /** WTF? . */
  // TODO: should test resolving of member_external_type in the instantiated object
  @Test
  @Ignore
  public void adaptedInstancing() {
    String program =
        """
                entity_type my_obj {
                    test_component1 {
                        member1: 42,
                        member2: 12
                    },
                    test_component_with_external_type {
                        member_external_type: external_type { str: "Hello, World!" }
                    }
                }

                quest_config config {
                    entity: instantiate(my_obj)
                }
                """;

    // setup test type system
    var env = new TestEnvironment();
    env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, env.getGlobalScope());
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), ExternalType.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponentWithExternalType.class);
    env.getTypeBuilder()
        .bindProperty(env.getGlobalScope(), Entity.TestComponentWithExternalTypeProperty.instance);

    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(
        program, env, interpreter, Entity.class, TestComponent1.class);

    var globalMs = interpreter.getGlobalMemorySpace();
    AggregateValue config = (AggregateValue) (globalMs.resolve("config"));
    AggregateValue myObj = (AggregateValue) config.getMemorySpace().resolve("entity");
    AggregateValue component =
        (AggregateValue) myObj.getMemorySpace().resolve("test_component_with_external_type");

    Value externalTypeMemberValue = component.getMemorySpace().resolve("member_external_type");
    Assert.assertNotEquals(externalTypeMemberValue, Value.NONE);
    Assert.assertEquals(
        externalTypeMemberValue.getDataType().getTypeKind(), IType.Kind.AggregateAdapted);

    var internalObject = (TestComponentWithExternalType) component.getInternalValue();
    ExternalType externalTypeMember = internalObject.getMemberExternalType();
    Assert.assertEquals("Hello, World!", externalTypeMember.member3);
  }

  /** WTF? . */
  @Test
  public void adaptedInstancingMultiParam() {
    String program =
        """
                entity_type my_obj {
                    test_component1 {
                        member1: 42,
                        member2: 12
                    },
                    test_component_with_external_type {
                        member_external_type: external_type { string: "Hello, World!", number: 42 }
                    }
                }

                quest_config config {
                    entity: instantiate(my_obj)
                }
                """;

    // setup test type system
    var env = new TestEnvironment();
    env.getTypeBuilder()
        .registerTypeAdapter(ExternalTypeBuilderMultiParam.class, env.getGlobalScope());
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponentWithExternalType.class);
    env.getTypeBuilder()
        .bindProperty(env.getGlobalScope(), Entity.TestComponentWithExternalTypeProperty.instance);
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomTypes(
        program,
        env,
        interpreter,
        Entity.class,
        TestComponent1.class,
        // TestComponentWithExternalType.class,
        ExternalType.class);

    var globalMs = interpreter.getGlobalMemorySpace();
    AggregateValue config = (AggregateValue) (globalMs.resolve("config"));
    AggregateValue myObj = (AggregateValue) config.getMemorySpace().resolve("entity");
    AggregateValue component =
        (AggregateValue) myObj.getMemorySpace().resolve("test_component_with_external_type");

    Value externalTypeMemberValue = component.getMemorySpace().resolve("member_external_type");
    Assert.assertNotEquals(externalTypeMemberValue, Value.NONE);
    Assert.assertEquals(
        externalTypeMemberValue.getDataType().getTypeKind(), IType.Kind.AggregateAdapted);

    var internalObject = (TestComponentWithExternalType) component.getInternalValue();
    ExternalType externalTypeMember = internalObject.getMemberExternalType();
    Assert.assertEquals("Hello, World!", externalTypeMember.member3);
    Assert.assertEquals(42, externalTypeMember.member1);
  }

  /** WTF? . */
  @Test
  public void testIsBoolean() {
    Assert.assertFalse(DSLInterpreter.isBooleanTrue(Value.NONE));

    var boolFalse = new Value(BuiltInType.boolType, false);
    Assert.assertFalse(DSLInterpreter.isBooleanTrue(boolFalse));

    var boolTrue = new Value(BuiltInType.boolType, true);
    Assert.assertTrue(DSLInterpreter.isBooleanTrue(boolTrue));

    var zeroIntValue = new Value(BuiltInType.intType, 0);
    Assert.assertFalse(DSLInterpreter.isBooleanTrue(zeroIntValue));

    var nonZeroIntValue = new Value(BuiltInType.intType, 42);
    Assert.assertTrue(DSLInterpreter.isBooleanTrue(nonZeroIntValue));

    var zeroFloatValue = new Value(BuiltInType.floatType, 0.0f);
    Assert.assertFalse(DSLInterpreter.isBooleanTrue(zeroFloatValue));

    var nonZeroFloatValue = new Value(BuiltInType.floatType, 3.14f);
    Assert.assertTrue(DSLInterpreter.isBooleanTrue(nonZeroFloatValue));

    var stringValue = new Value(BuiltInType.stringType, "");
    Assert.assertTrue(DSLInterpreter.isBooleanTrue(stringValue));

    var graphValue =
        new Value(
            BuiltInType.graphType, new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>()));
    Assert.assertTrue(DSLInterpreter.isBooleanTrue(graphValue));
  }

  /** WTF? . */
  @Test
  public void testIfStmtFalse() {
    String program =
        """
                fn test_func() {
                    if 0 print("Hello, World!");
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertFalse(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void testIfStmtTrue() {
    String program =
        """
                fn test_func() {
                    if 1 print("Hello, World!");
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void testElseStmt() {
    String program =
        """
                fn test_func() {
                    if 0 print("Hello");
                    else print ("World");
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("World"));
  }

  /** WTF? . */
  @Test
  public void testIfElseStmt() {
    String program =
        """
                fn test_func() {
                    if 0 print("Hello");
                    else if 0 print ("World");
                    else print("!");
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("!"));
  }

  /** WTF? . */
  @Test
  public void testIfElseStmtSecondIf() {
    String program =
        """
                fn test_func() {
                    if false print("Hello");
                    else if true print ("World");
                    else print("!");
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("World"));
    assertFalse(outputStream.toString().contains("!"));
  }

  /** WTF? . */
  @Test
  public void testBranchingReturn() {
    String program =
        """
                fn test_func() {
                    if false {
                        print("branch1");
                    } else {
                        print("branch2 stmt1");
                        print("branch2 stmt2");
                        return;
                        print("after return stmt");
                    }
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertTrue(outputStream.toString().contains("branch2 stmt1"));
    assertTrue(outputStream.toString().contains("branch2 stmt2"));
    assertFalse(outputStream.toString().contains("after return stmt"));
    assertFalse(outputStream.toString().contains("branch1"));
  }

  /** WTF? . */
  @Test
  public void testBranchingReturnNested() {
    String program =
        """
                fn other_func() -> string {
                    print("other_func stmt1");
                    print("other_func stmt2");
                    return "hello" ;
                    print("other_func stmt3");
                }

                fn test_func() {
                    if false {
                        print("branch1");
                    } else {
                        print("branch2 stmt1");
                        print(other_func());
                        return;
                        print("after return stmt");
                    }
                }

                quest_config c {
                    test: test_func()
                }
                    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    Helpers.generateQuestConfigWithCustomFunctions(program, env, interpreter);

    assertEquals(
        "branch2 stmt1"
            + System.lineSeparator()
            + "other_func stmt1"
            + System.lineSeparator()
            + "other_func stmt2"
            + System.lineSeparator()
            + "hello"
            + System.lineSeparator(),
        outputStream.toString());
  }

  /** WTF? . */
  @Test
  public void testFuncRefValue() {
    String program =
        """
                    entity_type my_type {
                        test_component_with_callback {
                            on_interaction: other_func
                        }
                    }

                    fn other_func(entity my_entity) {
                        print("Hello, World!");
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        Helpers.generateQuestConfigWithCustomTypes(
            program, env, interpreter, Entity.class, TestComponentWithCallback.class);

    Assert.assertTrue(true);
  }

  /** WTF? . */
  @Test
  public void testFuncRefValueCall() {
    String program =
        """
                    entity_type my_type {
                        test_component_with_string_consumer_callback {
                            on_interaction: other_func
                        }
                    }

                    fn other_func(string text) {
                        print(text);
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponentWithStringConsumerCallback.class);

    var testComponentWithCallback =
        (TestComponentWithStringConsumerCallback) config.entity().components.get(0);
    testComponentWithCallback.executeCallbackWithText("Moin");
    testComponentWithCallback.executeCallbackWithText("Tach och");

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Moin"));
    Assert.assertTrue(output.contains("Tach och"));
  }

  /** WTF? . */
  @Test
  public void testFuncRefValueCallReturn() {
    String program =
        """
                    entity_type my_type {
                        test_component_with_string_function_callback {
                            on_interaction: other_func
                        }
                    }

                    fn other_func(string text) -> string {
                        return text;
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponentWithStringFunctionCallback.class);

    var testComponentWithCallback =
        (TestComponentWithStringFunctionCallback) config.entity().components.get(0);
    var returnValue = testComponentWithCallback.executeCallbackWithText("Moin");

    Assert.assertTrue(returnValue.contains("Moin"));
  }

  /** WTF? . */
  @Test
  public void registerListAndSetTypesInEnvironment() {
    String program =
        """
                    quest_config c {
                        int_list: [1,2,3],
                        string_list: ["Hello", "World", "!"]
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, Entity.class);

    var rtEnv = interpreter.getRuntimeEnvironment();
    IType intListType = (IType) rtEnv.getGlobalScope().resolve("int[]");
    Assert.assertNotEquals(null, intListType);
    Assert.assertEquals(IType.Kind.ListType, intListType.getTypeKind());
    Assert.assertEquals(BuiltInType.intType, ((ListType) intListType).getElementType());

    IType stringListType = (IType) rtEnv.getGlobalScope().resolve("string[]");
    Assert.assertNotEquals(null, stringListType);
    Assert.assertEquals(IType.Kind.ListType, stringListType.getTypeKind());
    Assert.assertEquals(BuiltInType.stringType, ((ListType) stringListType).getElementType());
  }

  /** WTF? . */
  @Test
  public void setListValues() {
    String program =
        """
                    quest_config c {
                        int_list: [1,2,3],
                        string_list: ["Hello", "World", "!"]
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, Entity.class);

    // cast to Integer to make the compiler happy
    Assert.assertEquals((Integer) 1, config.intList().get(0));
    Assert.assertEquals((Integer) 2, config.intList().get(1));
    Assert.assertEquals((Integer) 3, config.intList().get(2));

    Assert.assertEquals("Hello", config.stringList().get(0));
    Assert.assertEquals("World", config.stringList().get(1));
    Assert.assertEquals("!", config.stringList().get(2));
  }

  /** WTF? . */
  @Test
  public void setSetValues() {
    String program =
        """
                    quest_config c {
                        float_set: <1.2,2.3,3.0,3.0>,
                        string_set: <"Hello", "Hello", "World", "!">
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, Entity.class);

    Set<Float> floatSet = config.floatSet();
    Assert.assertEquals(3, floatSet.size());
    Assert.assertTrue(floatSet.contains(1.2f));
    Assert.assertTrue(floatSet.contains(2.3f));
    Assert.assertTrue(floatSet.contains(3.0f));

    Set<String> stringSet = config.stringSet();
    Assert.assertEquals(3, stringSet.size());
    Assert.assertTrue(stringSet.contains("Hello"));
    Assert.assertTrue(stringSet.contains("World"));
    Assert.assertTrue(stringSet.contains("!"));
  }

  /** WTF? . */
  @Test
  public void passListValueToFunc() {
    String program =
        """
                    fn test_func(entity[] my_entities) -> bool {
                        return true;
                    }

                    entity_type my_type{
                        test_component_list_callback{
                            on_interaction: test_func
                        }
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponentListCallback.class,
                TestComponentListOfListsCallback.class);

    var testComponentWithCallback = (TestComponentListCallback) config.entity().components.get(0);

    ArrayList<Entity> entities = new ArrayList<>();
    entities.add(new Entity());
    entities.add(new Entity());
    entities.add(new Entity());

    boolean returnValue = testComponentWithCallback.executeCallbackWithText(entities);
    Assert.assertTrue(returnValue);
  }

  /** WTF? . */
  @Test
  public void passListValueThroughFunc() {
    String program =
        """
                    fn test_func(entity[] my_entities) -> entity[] {
                        return my_entities;
                    }

                    entity_type my_type{
                        test_component_list_pass_through_callback{
                            on_interaction: test_func
                        }
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponentListCallback.class,
                TestComponentListPassThroughCallback.class);

    var testComponentWithCallback =
        (TestComponentListPassThroughCallback) config.entity().components.get(0);

    ArrayList<Entity> entities = new ArrayList<>();
    entities.add(new Entity());
    entities.add(new Entity());
    entities.add(new Entity());

    List<Entity> returnedEntities = testComponentWithCallback.executeCallback(entities);
    Assert.assertEquals(entities.get(0), returnedEntities.get(0));
    Assert.assertEquals(entities.get(1), returnedEntities.get(1));
    Assert.assertEquals(entities.get(2), returnedEntities.get(2));
  }

  /** WTF? . */
  @Test
  public void passSetValueThroughFunc() {
    String program =
        """
                    fn test_func(entity<> my_entities) -> entity<> {
                        return my_entities;
                    }

                    entity_type my_type{
                        test_component_set_pass_through_callback{
                            on_interaction: test_func
                        }
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program, env, interpreter, Entity.class, TestComponentSetPassThroughCallback.class);

    var testComponentWithCallback =
        (TestComponentSetPassThroughCallback) config.entity().components.get(0);

    HashSet<Entity> entities = new HashSet<>();
    entities.add(new Entity());
    entities.add(new Entity());
    entities.add(new Entity());

    Set<Entity> returnedEntities = testComponentWithCallback.executeCallback(entities);
    Assert.assertEquals(3, returnedEntities.size());
    for (var entity : entities) {
      Assert.assertTrue(returnedEntities.contains(entity));
    }
  }

  /** WTF? . */
  @Test
  // TODO: requires implementation of task dependency graph parsing (see:
  // https://github.com/Dungeon-CampusMinden/Dungeon/issues/520)
  public void taskDefinition() {
    String program =
        """
                    single_choice_task t1 {
                        description: "Hello",
                        answers: ["1", "2", "3"],
                        correct_answer_index: 1
                    }

                    multiple_choice_task t2 {
                        description: "Tschüss",
                        answers: ["4", "5", "6"],
                        correct_answer_indices: [0,1]
                    }

                    graph g {
                        t1 -> t2 [type=st_m]
                    }

                    dungeon_config c {
                        dependency_graph: g
                    }
                """;

    DSLInterpreter interpreter = new DSLInterpreter();
    var config = (DungeonConfig) interpreter.getQuestConfig(program);

    boolean b = true;
    /*
    Quiz singleChoiceTask = (Quiz) config.dependencyGraph().nodeIterator().next().task();
    Assert.assertTrue(singleChoiceTask instanceof SingleChoice);
    Assert.assertEquals("Hello", singleChoiceTask.taskText());
    Assert.assertTrue(singleChoiceTask.correctAnswerIndices().contains(1));
    var answers = singleChoiceTask.contentStream().toList();
    Assert.assertEquals("1", ((Quiz.Content) answers.get(0)).content());
    Assert.assertEquals("2", ((Quiz.Content) answers.get(1)).content());
    Assert.assertEquals("3", ((Quiz.Content) answers.get(2)).content());

    Quiz multipleChoiceTask = (Quiz) config.dependencyGraph().nodeIterator().next().task();
    Assert.assertTrue(multipleChoiceTask instanceof MultipleChoice);
    Assert.assertEquals("Tschüss", multipleChoiceTask.taskText());
    Assert.assertTrue(multipleChoiceTask.correctAnswerIndices().contains(1));
    var multipleChoiceAnswers = multipleChoiceTask.contentStream().toList();
    Assert.assertEquals("4", ((Quiz.Content) multipleChoiceAnswers.get(0)).content());
    Assert.assertEquals("5", ((Quiz.Content) multipleChoiceAnswers.get(1)).content());
    Assert.assertEquals("6", ((Quiz.Content) multipleChoiceAnswers.get(2)).content());
     */
  }

  /** WTF? . */
  @Test
  public void testMemberAccess() {
    String program =
        """
                    fn test_func(test_component2 component) {
                        print(component.member1);
                    }

                    entity_type my_type{
                        test_component_string_member_and_callback{
                            consumer: test_func
                        },
                        test_component2 {
                            member1: "Hello, World!"
                        }
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponent2.class,
                TestComponentStringMemberAndCallback.class);

    Entity entity = config.entity();
    var testComponentWithCallback = (TestComponentStringMemberAndCallback) entity.components.get(1);
    var testComponent2 = (TestComponent2) config.entity().components.get(0);
    testComponentWithCallback.getConsumer().accept(testComponent2);

    String outputStreamString = outputStream.toString();
    Assert.assertTrue(outputStreamString.contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void testMemberAccessFuncCall() {
    String program =
        """
                    fn return_component(test_component2 component) -> test_component2 {
                        return component;
                    }

                    fn use_function(test_component2 component) {
                        // setting of the component parameter does not work!
                        print(return_component(component).member1);
                    }

                    entity_type my_type{
                        test_component_string_member_and_callback{
                            consumer: use_function
                        },
                        test_component2 {
                            member1: "Hello, World!"
                        }
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    var config =
        (CustomQuestConfig)
            Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponent2.class,
                TestComponentStringMemberAndCallback.class);

    var testComponent2 = (TestComponent2) config.entity().components.get(0);
    var testComponentWithCallback =
        (TestComponentStringMemberAndCallback) config.entity().components.get(1);
    testComponentWithCallback.getConsumer().accept(testComponent2);

    String outputStreamString = outputStream.toString();
    Assert.assertTrue(outputStreamString.contains("Hello, World!"));
  }

  /** WTF? . */
  @Test
  public void testProperty() {
    String program =
        """
                entity_type my_type {
                    test_component2 {
                        member2: 42,
                        this_is_a_float: 3.14
                    },
                    test_component_with_callback {
                        consumer: get_property
                    }
                }

                fn get_property(test_component2 comp) {
                    print(comp.this_is_a_float);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentTestComponent2ConsumerCallback.class);
    env.getTypeBuilder()
        .bindProperty(env.getGlobalScope(), TestComponent2.TestComponentPseudoProperty.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();
    var componentWithConsumer =
        (TestComponentTestComponent2ConsumerCallback) entity.components.get(0);
    var testComponent2 = (TestComponent2) entity.components.get(1);
    componentWithConsumer.consumer.accept(testComponent2);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("3.14"));
  }

  /** WTF? . */
  @Test
  public void testPropertyOfComplexType() {
    String program =
        """
                entity_type my_type {
                    test_component2 {
                        member2: 42,
                        this_is_complex: complex_type { member1: 42 }
                    },
                    test_component_with_callback {
                        consumer: get_property
                    }
                }

                fn get_property(test_component2 comp) {
                    print(comp.this_is_complex.member1);
                    print(comp.this_is_complex.member3);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentTestComponent2ConsumerCallback.class);
    env.getTypeBuilder()
        .bindProperty(
            env.getGlobalScope(), TestComponent2.TestComponentPseudoPropertyComplexType.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();
    var componentWithConsumer =
        (TestComponentTestComponent2ConsumerCallback) entity.components.get(0);
    var testComponent2 = (TestComponent2) entity.components.get(1);
    componentWithConsumer.consumer.accept(testComponent2);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("42"));
  }

  /** WTF? . */
  @Test
  public void testComponentPropertyOfEntity() {
    String program =
        """
                entity_type my_type {
                    test_component2 {
                        member2: 42
                    },
                    test_component_with_callback {
                        consumer: get_property
                    }
                }

                fn get_property(entity ent) {
                    print(ent.test_component1.member1);
                    print(ent.test_component2.member2);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();
    var componentWithConsumer = (TestComponentEntityConsumerCallback) entity.components.get(0);
    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("42"));
  }

  /** WTF? . */
  @Test
  public void testComponentPropertyOfEntityUpdateValue() {
    String program =
        """
                entity_type my_type {
                    test_component2 {
                        member2: 42
                    },
                    test_component_with_callback {
                        consumer: get_property
                    }
                }

                fn get_property(entity ent) {
                    print(ent.test_component2.member2);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);
    // first call to dsl function get_property
    componentWithConsumer.consumer.accept(entity);

    TestComponent2 oldComponent =
        (TestComponent2)
            entity.components.stream().filter(c -> c instanceof TestComponent2).toList().get(0);
    entity.components.remove(oldComponent);

    TestComponent2 newComp = new TestComponent2(entity);
    newComp.setMember2(123);

    // second call to dsl function get_property
    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("42"));
    Assert.assertTrue(output.contains("123"));
  }

  /** WTF? . */
  @Test
  public void testAssignmentProperty() {
    String program =
        """
                    entity_type my_type {
                        test_component2 {
                            member1: "ja",
                            member3: "nein"
                        },
                        test_component_with_callback {
                            consumer: get_property
                        }
                    }

                    fn get_property(entity ent) {
                        ent.test_component2.member3 = ent.test_component2.member1 = "kuckuck";
                        print(ent.test_component2.member1);
                        print(ent.test_component2.member3);
                    }

                    quest_config c {
                        entity: instantiate(my_type)
                    }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);
    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    Assert.assertTrue(
        output.equals("kuckuck" + System.lineSeparator() + "kuckuck" + System.lineSeparator()));
  }

  /** WTF? . */
  @Test
  public void testAssignmentObjectMember() {
    String program =
        """
                entity_type my_type {
                    test_component2 {
                        member1: "ja",
                        member3: "nein"
                    },
                    test_component_with_callback {
                        consumer: set_property
                    }
                }

                fn set_property(entity ent) {
                    c.second_entity = ent;
                    print(c.second_entity.test_component2.member1);
                    ent.test_component2.member1 = "nein";
                    print(c.second_entity.test_component2.member1);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    Assert.assertTrue(
        output.equals("ja" + System.lineSeparator() + "nein" + System.lineSeparator()));
  }

  /** WTF? . */
  @Test
  public void testAssignmentFuncParam() {
    String program =
        """
                entity_type my_type {
                    test_component_with_string_consumer_callback {
                        on_interaction: set_param
                    }
                }

                fn set_param(string text) {
                    print(text);
                    text = "my text";
                    print(text);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentWithStringConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentWithStringConsumerCallback componentWithConsumer =
        (TestComponentWithStringConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentWithStringConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.executeCallbackWithText("hello");

    String output = outputStream.toString();
    Assert.assertTrue(
        output.equals("hello" + System.lineSeparator() + "my text" + System.lineSeparator()));
  }

  /** WTF? . */
  @Test
  public void testVariableCreation() {
    String program =
        """
                entity_type my_type {
                    test_component_with_string_consumer_callback {
                        on_interaction: get_property
                    }
                }

                fn get_property(string param) {
                    var test : string;
                    print(test);
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentWithStringConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentWithStringConsumerCallback componentWithConsumer =
        (TestComponentWithStringConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentWithStringConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.executeCallbackWithText("hello");

    // the output stream should only contain the default value for a string variable ("") and
    // the
    // line separator from the print-call
    String output = outputStream.toString();
    assertEquals(output, System.lineSeparator());
  }

  /** WTF? . */
  @Test
  public void testVariableCreationAndAssignment() {
    String program =
        """
            entity_type my_type {
                test_component_with_string_consumer_callback {
                    on_interaction: get_property
                }
            }

            fn get_property(string param) {
                var test : string;
                test = "Hello, World!";
                print(test);
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentWithStringConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentWithStringConsumerCallback componentWithConsumer =
        (TestComponentWithStringConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentWithStringConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.executeCallbackWithText("hello");

    // the output stream should only contain the default value for a string variable ("") and
    // the
    // line separator from the print-call
    String output = outputStream.toString();
    assertEquals(output, "Hello, World!" + System.lineSeparator());
  }

  /** WTF? . */
  @Test
  public void testVariableCreationAndAssignmentEntity() {
    String program =
        """
            entity_type my_type {
                test_component2 {
                    member1: "Hello, World!"
                },
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var test : entity;
                test = ent;
                print(test.test_component2.member1);
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the
    // line separator from the print-call
    String output = outputStream.toString();
    assertEquals(output, "Hello, World!" + System.lineSeparator());
  }

  /** WTF? . */
  @Test
  public void testVariableCreationList() {
    String program =
        """
        entity_type my_type {
            test_component2 {
                member1: "Hello, World!"
            },
            test_component_with_callback {
                consumer: func
            }
        }

        fn func(entity ent) {
            var test : entity[];
            if test {
                print("Hello, World!");
            }
        }

        quest_config c {
            entity: instantiate(my_type)
        }
        """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the
    // line separator from the print-call
    String output = outputStream.toString();
    assertEquals(output, "Hello, World!" + System.lineSeparator());
  }

  /** WTF? . */
  @Test
  public void testVariableCreationIfStmtBlock() {
    String program =
        """
        entity_type my_type {
            test_component_with_callback {
                consumer: func
            }
        }

        fn func(entity ent) {
            var test : string;
            if true {
                var test : int;
                test = 42;
            }
            print(test);
        }

        quest_config c {
            entity: instantiate(my_type)
        }
        """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the line separator from the print-call; if the variable definition in the if-stmt-body
    // "escapes" the output will contain "42\n"
    String output = outputStream.toString();
    assertEquals(System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testVariableCreationIfStmtSingleStmt() {
    String program =
        """
            entity_type my_type {
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var test : string;
                if true
                    var test : int;
                print(test);
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the line separator from the print-call; if the variable definition in the if-stmt-body
    // "escapes" the output will contain "0\n", as the new 'test'-variable will be initialized
    // with 0
    String output = outputStream.toString();
    assertEquals(System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testVariableCreationIfElseStmtSingleStmt() {
    String program =
        """
            entity_type my_type {
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var test : string;

                if true
                    var test : int;
                else
                    var test : int;
                print(test);

                if false
                    var test : int;
                else
                    var test : int;
                print(test);
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the line separator from the print-call; if the variable definitions in the
    // if-else=stmt-body "escapes" the output will contain '0', as the new 'test'-variable
    // will be initialized with 0
    String output = outputStream.toString();
    assertEquals(System.lineSeparator() + System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testNativeMethodCallList() {
    String program =
        """
        entity_type my_type {
            test_component_with_callback {
                consumer: func
            }
        }

        fn func(entity ent) {
            var test : string[];
            {
                test.add("hello");
                test.add("world");
            }
            print(test);
            print(test.size());
            print(test.get(1));
        }

        quest_config c {
            entity: instantiate(my_type)
        }
        """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    // the output stream should only contain the default value for a string variable ("") and
    // the line separator from the print-call; if the variable definitions in the
    // if-else=stmt-body "escapes" the output will contain '0', as the new 'test'-variable
    // will be initialized with 0
    String output = outputStream.toString();
    assertEquals(
        "[hello, world]"
            + System.lineSeparator()
            + "2"
            + System.lineSeparator()
            + "world"
            + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testNativeMethodCallSet() {
    String program =
        """
    entity_type my_type {
        test_component_with_callback {
            consumer: func
        }
    }

    fn func(entity ent) {
        var test : string<>;
        {
            test.add("hello");
            test.add("world");
        }
        print(test.size());
        print(test.contains("hello"));
        print(test.contains("!"));
    }

    quest_config c {
        entity: instantiate(my_type)
    }
    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals(
        "2"
            + System.lineSeparator()
            + "true"
            + System.lineSeparator()
            + "false"
            + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testExtensionMethodCall() {
    String program =
        """
        entity_type my_type {
            test_component1 {
                member1: 42,
                member2: 3.14,
                member3: "Hello, World!"
            },
            test_component2 {},
            test_component_with_callback {
                consumer: func
            }
        }

        fn func(entity ent) {
            // in test_component2.my_method, `member2` of the instance will be set to the first parameter
            ent.test_component2.my_method(ent.test_component1.member1, 42);
            print(ent.test_component2.member2);
        }

        quest_config c {
            entity: instantiate(my_type)
        }
        """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent1.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindMethod(env.getGlobalScope(), TestComponent2.MyMethod.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals("42" + System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testChainedExtensionMethodCall() {
    String program =
        """
    entity_type my_type {
        test_component1 {
            member1: 42,
            member2: 3.14,
            member3: "Hello, World!"
        },
        test_component2 {},
        test_component_with_callback {
            consumer: func
        }
    }

    fn func(entity ent) {
        // in test_component2.my_method, `member2` of the instance will be set to the first parameter
        ent.test_component2.my_method(ent.test_component1.member1, 42).my_method(42, 42);
        print(ent.test_component2.member2);
    }

    quest_config c {
        entity: instantiate(my_type)
    }
    """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent1.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent2.class);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
    env.getTypeBuilder().bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
    env.getTypeBuilder().bindMethod(env.getGlobalScope(), TestComponent2.MyMethod.instance);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals("42" + System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testInstantiateEntityDrawComponent() {
    String program =
        """
            entity_type wizard_type {
                draw_component {
                    path: "character/wizard"
                },
                hitbox_component {},
                position_component{}
            }

            dungeon_config c { }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    var config = (DungeonConfig) interpreter.getQuestConfig(program);

    // call the native `instantiate` function manually
    // resolve function in rtEnv
    var runtimeEnvironment = interpreter.getRuntimeEnvironment();
    NativeInstantiate instantiateFunc =
        (NativeInstantiate) runtimeEnvironment.getGlobalScope().resolve("instantiate");
    // create new IdNode for "wizard_type` to pass to native instantiate
    IdNode node = new IdNode("wizard_type", null);
    // call the function
    var value = (AggregateValue) instantiateFunc.call(interpreter, List.of(node));
    // extract the entity from the Value-instance
    core.Entity entity = (core.Entity) value.getInternalValue();

    Assert.assertTrue(entity.isPresent(DrawComponent.class));
    Assert.assertTrue(entity.isPresent(CollideComponent.class));
    Assert.assertTrue(entity.isPresent(PositionComponent.class));
  }

  /** WTF? . */
  @Test
  public void testInstantiateEntityDrawComponentAccessPath() {
    String program =
        """
        entity_type wizard_type {
            draw_component {
                path: "character/wizard"
            },
            hitbox_component {},
            position_component{}
        }

        fn test_func(entity ent) {
            print(ent.draw_component.path);
        }

        dungeon_config c { }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    var config = (DungeonConfig) interpreter.getQuestConfig(program);

    // call the native `instantiate` function manually
    // resolve function in rtEnv
    var runtimeEnvironment = interpreter.getRuntimeEnvironment();
    NativeInstantiate instantiateFunc =
        (NativeInstantiate) runtimeEnvironment.getGlobalScope().resolve("instantiate");
    // create new IdNode for "wizard_type` to pass to native instantiate
    IdNode node = new IdNode("wizard_type", null);
    // call the function
    var value = (AggregateValue) instantiateFunc.call(interpreter, List.of(node));
    // extract the entity from the Value-instance
    core.Entity entity = (core.Entity) value.getInternalValue();

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    FunctionSymbol fnSym =
        (FunctionSymbol) runtimeEnvironment.getGlobalScope().resolve("test_func");
    interpreter.executeUserDefinedFunctionRawParameters(
        fnSym, Arrays.stream(new Object[] {entity}).toList());

    // explanation: the `path`-property is just used as a parameter, which will be passed to the
    // adapter-method used for constructing the DrawComponent instance, after that, the
    // property will be null, because it is not stored in the DrawComponent instance
    // -> it is expected, that Value.NONE (of which the String representation is "[no value]")
    // is returned for `path` in that case
    String output = outputStream.toString();
    Assert.assertEquals("[no value]" + System.lineSeparator(), output);
  }

  /** WTF? . */
  @Test
  public void testTaskDependencyGraphNonConnected() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t2 {
                description: "Task2",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            graph tdg {
                t1;
                t2;
            }

            dungeon_config c {
                dependency_graph: tdg
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var graph = config.dependencyGraph();

    List<TaskNode> nodes = new ArrayList<>();
    var nodeIter = graph.nodeIterator();
    while (nodeIter.hasNext()) {
      nodes.add(nodeIter.next());
    }

    Assert.assertEquals(2, nodes.size());

    List<TaskEdge> edges = new ArrayList<>();
    var edgeIter = graph.edgeIterator();
    while (edgeIter.hasNext()) {
      edges.add(edgeIter.next());
    }

    Assert.assertEquals(0, edges.size());
  }

  /** WTF? . */
  @Test
  public void testTaskDependencyGraph() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t2 {
                description: "Task2",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t3 {
                description: "Task3",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t4 {
                description: "Task4",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t5 {
                description: "Task5",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t6 {
                description: "Task6",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            graph tdg {
                t1 -> t2 [type=st_m]
                t1 -> t3 [type=st_o]
                t1 -> t4 [type=seq]
                t1 -> t5 [type=c_f]
                t1 -> t6 [type=c_c]
            }

            dungeon_config c {
                dependency_graph: tdg
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var graph = config.dependencyGraph();

    List<TaskNode> nodes = new ArrayList<>();
    var nodeIter = graph.nodeIterator();
    while (nodeIter.hasNext()) {
      nodes.add(nodeIter.next());
    }

    Assert.assertEquals(6, nodes.size());

    List<TaskEdge> edges = new ArrayList<>();
    HashMap<TaskNode, List<TaskEdge>> startNodeToEdges = new HashMap<>();
    HashMap<TaskNode, List<TaskEdge>> endNodeToEdges = new HashMap<>();
    for (var node : nodes) {
      startNodeToEdges.put(node, new ArrayList<>());
      endNodeToEdges.put(node, new ArrayList<>());
    }

    var edgeIter = graph.edgeIterator();
    while (edgeIter.hasNext()) {
      var edge = edgeIter.next();
      var startNodesEdge = startNodeToEdges.get(edge.startNode());
      startNodesEdge.add(edge);
      var endNodeEdge = endNodeToEdges.get(edge.endNode());
      endNodeEdge.add(edge);
      edges.add(edge);
    }

    Assert.assertEquals(5, edges.size());

    var taskNode1 = nodes.stream().filter(t -> t.task().taskText().equals("Task1")).toList().get(0);
    var taskNode2 = nodes.stream().filter(t -> t.task().taskText().equals("Task2")).toList().get(0);
    var taskNode3 = nodes.stream().filter(t -> t.task().taskText().equals("Task3")).toList().get(0);
    var taskNode4 = nodes.stream().filter(t -> t.task().taskText().equals("Task4")).toList().get(0);
    var taskNode5 = nodes.stream().filter(t -> t.task().taskText().equals("Task5")).toList().get(0);
    var taskNode6 = nodes.stream().filter(t -> t.task().taskText().equals("Task6")).toList().get(0);

    var t1t2edge = endNodeToEdges.get(taskNode2).get(0);
    Assert.assertEquals(taskNode1, t1t2edge.startNode());
    Assert.assertEquals(TaskEdge.Type.subtask_mandatory, t1t2edge.edgeType());

    var t1t3edge = endNodeToEdges.get(taskNode3).get(0);
    Assert.assertEquals(taskNode1, t1t3edge.startNode());
    Assert.assertEquals(TaskEdge.Type.subtask_optional, t1t3edge.edgeType());

    var t1t4edge = endNodeToEdges.get(taskNode4).get(0);
    Assert.assertEquals(taskNode1, t1t4edge.startNode());
    Assert.assertEquals(TaskEdge.Type.sequence, t1t4edge.edgeType());

    var t1t5edge = endNodeToEdges.get(taskNode5).get(0);
    Assert.assertEquals(taskNode1, t1t5edge.startNode());
    Assert.assertEquals(TaskEdge.Type.conditional_false, t1t5edge.edgeType());

    var t1t6edge = endNodeToEdges.get(taskNode6).get(0);
    Assert.assertEquals(taskNode1, t1t6edge.startNode());
    Assert.assertEquals(TaskEdge.Type.conditional_correct, t1t6edge.edgeType());
  }

  /** WTF? . */
  @Test
  public void testTaskDependencyGraphGroupNotation() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t2 {
                description: "Task2",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t3 {
                description: "Task3",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t4 {
                description: "Task4",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t5 {
                description: "Task5",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            single_choice_task t6 {
                description: "Task6",
                answers: ["1", "2", "3"],
                correct_answer_index: 2
            }

            graph tdg {
                t1,t2 -> t3,t4 -> t5,t6 [type=seq_or]
            }

            dungeon_config c {
                dependency_graph: tdg
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var graph = config.dependencyGraph();

    List<TaskNode> nodes = new ArrayList<>();
    var nodeIter = graph.nodeIterator();
    while (nodeIter.hasNext()) {
      nodes.add(nodeIter.next());
    }

    Assert.assertEquals(6, nodes.size());

    List<TaskEdge> edges = new ArrayList<>();
    HashMap<TaskNode, List<TaskEdge>> startNodeToEdges = new HashMap<>();
    HashMap<TaskNode, List<TaskEdge>> endNodeToEdges = new HashMap<>();
    for (var node : nodes) {
      startNodeToEdges.put(node, new ArrayList<>());
      endNodeToEdges.put(node, new ArrayList<>());
    }

    var edgeIter = graph.edgeIterator();
    while (edgeIter.hasNext()) {
      var edge = edgeIter.next();
      var startNodesEdge = startNodeToEdges.get(edge.startNode());
      startNodesEdge.add(edge);
      var endNodeEdge = endNodeToEdges.get(edge.endNode());
      endNodeEdge.add(edge);
      edges.add(edge);
    }

    Assert.assertEquals(8, edges.size());

    var taskNode1 = nodes.stream().filter(t -> t.task().taskText().equals("Task1")).toList().get(0);
    var taskNode2 = nodes.stream().filter(t -> t.task().taskText().equals("Task2")).toList().get(0);
    var taskNode3 = nodes.stream().filter(t -> t.task().taskText().equals("Task3")).toList().get(0);
    var taskNode4 = nodes.stream().filter(t -> t.task().taskText().equals("Task4")).toList().get(0);
    var taskNode5 = nodes.stream().filter(t -> t.task().taskText().equals("Task5")).toList().get(0);
    var taskNode6 = nodes.stream().filter(t -> t.task().taskText().equals("Task6")).toList().get(0);

    var t1t3edge = startNodeToEdges.get(taskNode1).get(0);
    Assert.assertEquals(taskNode3, t1t3edge.endNode());

    var t1t4edge = startNodeToEdges.get(taskNode1).get(1);
    Assert.assertEquals(taskNode4, t1t4edge.endNode());

    var t2t3edge = startNodeToEdges.get(taskNode2).get(0);
    Assert.assertEquals(taskNode3, t2t3edge.endNode());

    var t2t4edge = startNodeToEdges.get(taskNode2).get(1);
    Assert.assertEquals(taskNode4, t2t4edge.endNode());

    var t3t5edge = startNodeToEdges.get(taskNode3).get(0);
    Assert.assertEquals(taskNode5, t3t5edge.endNode());

    var t3t6edge = startNodeToEdges.get(taskNode3).get(1);
    Assert.assertEquals(taskNode6, t3t6edge.endNode());

    var t4t5edge = startNodeToEdges.get(taskNode4).get(0);
    Assert.assertEquals(taskNode5, t4t5edge.endNode());

    var t4t6edge = startNodeToEdges.get(taskNode4).get(1);
    Assert.assertEquals(taskNode6, t4t6edge.endNode());
  }

  /** WTF? . */
  @Test
  public void testScenarioBuilderIntegration() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: ["1", "2", "3"],
            correct_answer_index: 2,
            scenario_builder: build_scenario1
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        entity_type wizard_type {
            draw_component {
                path: "character/wizard"
            },
            hitbox_component {},
            position_component{},
            task_component{}
        }

        entity_type knight_type {
            draw_component {
                path: "character/blue_knight"
            },
            hitbox_component {},
            position_component{}
        }

        fn build_scenario1(single_choice_task t) -> entity<><> {
            var ret_set : entity<><>;

            var first_room_set : entity<>;
            var second_room_set : entity<>;

            var wizard : entity;
            var knight : entity;

            wizard = instantiate(wizard_type);
            wizard.task_component.task = t;

            knight = instantiate(knight_type);

            first_room_set.add(wizard);
            second_room_set.add(knight);

            ret_set.add(first_room_set);
            ret_set.add(second_room_set);

            return ret_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();
    var roomIter = builtTask.iterator();

    var firstRoomSet = roomIter.next();
    var entityInFirstRoom = firstRoomSet.iterator().next();

    var secondRoomSet = roomIter.next();
    var entityInSecondRoom = secondRoomSet.iterator().next();

    DrawComponent drawComp1 = entityInFirstRoom.fetch(DrawComponent.class).get();
    var frameDrawComp1 = drawComp1.currentAnimation().animationFrames().get(0).pathString();

    DrawComponent drawComp2 = entityInSecondRoom.fetch(DrawComponent.class).get();
    var frameDrawComp2 = drawComp2.currentAnimation().animationFrames().get(0).pathString();

    int firstEntitiesId = entityInFirstRoom.id();
    int secondEntitiesId = entityInSecondRoom.id();
    // the entity with the smaller id should be the wizard
    if (firstEntitiesId < secondEntitiesId) {
      Assert.assertTrue(frameDrawComp1.contains("wizard"));
      Assert.assertTrue(frameDrawComp2.contains("knight"));
    } else {
      Assert.assertTrue(frameDrawComp1.contains("knight"));
      Assert.assertTrue(frameDrawComp2.contains("wizard"));
    }
  }

  /** WTF? . */
  @Test
  // native scenario builders will make this test case obsolete
  @Ignore
  public void testScenarioBuilderTypeCreation() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: ["1", "2", "3"],
            correct_answer_index: 2
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);

    var task = config.dependencyGraph().nodeIterator().next().task();

    // because the program does not declare any functions returning the `entity<><>` type
    // (e.g. no scenario builder function), the type for `entity<><>` won't be created before
    // scanning for scenario builders. It should be created on demand by the DSLInterpreter.
    // if this fails, this call will throw a RuntimeException, if not, it returns an
    Optional<Object> builtTask = interpreter.buildTask(task);
    Assert.assertTrue(builtTask.isEmpty());
  }

  /** WTF? . */
  @Test
  public void testEnumVariantInstantiation() {
    String program =
        """
                entity_type my_type {
                    test_component_with_function_callback {
                        get_enum: func
                    }
                }

                fn func(entity ent) -> my_enum {
                    return my_enum.A;
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentWithFunctionCallback.class);
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), MyEnum.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentWithFunctionCallback componentWithConsumer =
        (TestComponentWithFunctionCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentWithFunctionCallback)
                .toList()
                .get(0);

    MyEnum enumValue = componentWithConsumer.getGetEnum().apply(entity);
    Assert.assertEquals(MyEnum.A, enumValue);
  }

  /** WTF? . */
  @Test
  public void testEnumCallbackParameter() {
    String program =
        """
                entity_type my_type {
                    test_component_with_function_callback {
                        function_with_enum_param: func
                    }
                }

                fn func(my_enum value) -> bool {
                    if value {
                        return true;
                    } else {
                        return false;
                    }
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentWithFunctionCallback.class);
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), MyEnum.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentWithFunctionCallback componentWithConsumer =
        (TestComponentWithFunctionCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentWithFunctionCallback)
                .toList()
                .get(0);

    MyEnum parameter = MyEnum.A;
    boolean returnValue = componentWithConsumer.getFunctionWithEnumParam().apply(parameter);
    Assert.assertTrue(returnValue);
  }

  /** WTF? . */
  @Test
  public void testCollisionCallback() {
    String program =
        """
                single_choice_task t {
                    description: "hello",
                    answers: [1,2,3],
                    correct_answer_index: 1,
                    scenario_builder: build_scenario
                }

                graph g {
                    t;
                }

                dungeon_config c {
                    dependency_graph: g
                }

                entity_type my_type {
                    hitbox_component {
                        collide_enter: callback
                    }
                }

                fn callback(entity ent1, entity ent2, tile_direction dir) {
                    print(dir);
                }


                fn build_scenario(single_choice_task t) -> entity<><> {
                    var main_set : entity<><>;
                    var room_set : entity<>;

                    var wizard1 : entity;
                    var wizard2 : entity;
                    wizard1 = instantiate(my_type);
                    wizard2 = instantiate(my_type);

                    room_set.add(wizard1);
                    room_set.add(wizard2);
                    main_set.add(room_set);
                    return main_set;
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    var config = (DungeonConfig) interpreter.getQuestConfig(program);

    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();
    var roomIter = builtTask.iterator();

    var firstRoomSet = roomIter.next();
    var firstRoomIterator = firstRoomSet.iterator();
    core.Entity entityInFirstRoom1 = firstRoomIterator.next();
    core.Entity entityInFirstRoom2 = firstRoomIterator.next();

    CollideComponent component = entityInFirstRoom1.fetch(CollideComponent.class).get();
    component.onEnter(entityInFirstRoom1, entityInFirstRoom2, Tile.Direction.N);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("N"));
  }

  /** WTF? . */
  @Test
  public void testForLoop() {
    String program =
        """
            entity_type my_type {
                test_component1 {},
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var my_list : int[];
                my_list.add(1);
                my_list.add(2);
                my_list.add(3);
                for int entry in my_list {
                    print(entry);
                }
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent1.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals(
        "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testForLoopIterableExpression() {
    String program =
        """
            entity_type my_type {
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var my_list_of_lists : int[][];

                var my_list : int[];
                my_list.add(1);
                my_list.add(2);
                my_list.add(3);

                my_list_of_lists.add(my_list);
                for int entry in my_list_of_lists.get(0) {
                    print(entry);
                }
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals(
        "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testCountingForLoop() {
    String program =
        """
            entity_type my_type {
                test_component1 {},
                test_component_with_callback {
                    consumer: func
                }
            }

            fn func(entity ent) {
                var my_list : string[];
                my_list.add("Hello");
                my_list.add("World");
                my_list.add("!");
                for int entry in my_list count i {
                    print(i);
                    print(entry);
                }
            }

            quest_config c {
                entity: instantiate(my_type)
            }
            """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent1.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    assertEquals(
        "0"
            + System.lineSeparator()
            + "Hello"
            + System.lineSeparator()
            + "1"
            + System.lineSeparator()
            + "World"
            + System.lineSeparator()
            + "2"
            + System.lineSeparator()
            + "!"
            + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testWhileLoop() {
    String program =
        """
                entity_type my_type {
                    test_component1 {},
                    test_component_with_callback {
                        consumer: func
                    }
                }

                fn func(entity ent) {
                    var my_list : int[];
                    my_list.add(1);
                    my_list.add(2);
                    my_list.add(3);
                    my_list.add(0);

                    var list_entry : int;
                    list_entry = my_list.get(0);

                    // as arithmetic operations are not supported at the time this test is written,
                    // we need some way of updating the condition of the loop;
                    // the `my_list` is setup in a way, that the entries used as the index will
                    // return the next list-entry, until the entry with value `0` is returned,
                    // which will be interpreted as boolean false in the condition of the loop
                    while list_entry {
                        print(list_entry);
                        list_entry = my_list.get(list_entry);
                    }
                }

                quest_config c {
                    entity: instantiate(my_type)
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    TestEnvironment env = new TestEnvironment();
    DSLInterpreter interpreter = new DSLInterpreter();
    env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(
            env.getGlobalScope(), TestComponentEntityConsumerCallback.class);
    env.getTypeBuilder()
        .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), TestComponent1.class);

    var config =
        (CustomQuestConfig) Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

    var entity = config.entity();

    TestComponentEntityConsumerCallback componentWithConsumer =
        (TestComponentEntityConsumerCallback)
            entity.components.stream()
                .filter(c -> c instanceof TestComponentEntityConsumerCallback)
                .toList()
                .get(0);

    componentWithConsumer.consumer.accept(entity);

    String output = outputStream.toString();
    boolean b = true;
    assertEquals(
        "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator(),
        output);
  }

  /** WTF? . */
  @Test
  public void testItemTypeInstantiationSingleChoice() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
                answers: ["1", "2", "3"],
            correct_answer_index: 2,
            scenario_builder: build_scenario1
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        item_type item_type1 {
            display_name: "MyName",
            description: "Hello, this is a description",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_scenario1(single_choice_task t) -> entity<><> {
            var ret_set : entity<><>;

            var first_room_set : entity<>;

            var item : quest_item;
            var content : task_content[];
            content = t.get_content();

            item = build_quest_item(item_type1, content.get(1));
            place_quest_item(item, first_room_set);

            ret_set.add(first_room_set);
            return ret_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    var entityIterator = builtTask.iterator().next().iterator();
    // this will be the placed quest item
    var questItem = entityIterator.next();
    ItemComponent itemComponent = questItem.fetch(ItemComponent.class).get();
    QuestItem item = (QuestItem) itemComponent.item();
    // Assert.assertEquals("MyName", item.displayName());
    Quiz.Content content = (Quiz.Content) item.taskContentComponent().content();
    Assert.assertEquals("2", content.content());
  }

  /** WTF? . */
  @Test
  public void testItemTypeInstantiationMultipleChoice() {
    String program =
        """
            multiple_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_indices: [1,2],
                scenario_builder: build_scenario1
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }

            item_type item_type1 {
                display_name: "MyName",
                description: "Hello, this is a description",
                texture_path: "items/book/wisdom_scroll.png"
            }

            fn build_scenario1(multiple_choice_task t) -> entity<><> {
                var ret_set : entity<><>;

                var first_room_set : entity<>;

                var item : quest_item;
                var content : task_content[];
                content = t.get_content();

                item = build_quest_item(item_type1, content.get(1));
                place_quest_item(item, first_room_set);

                ret_set.add(first_room_set);
                return ret_set;
            }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    var entityIterator = builtTask.iterator().next().iterator();
    // this will be the placed quest item
    var questItem = entityIterator.next();
    ItemComponent itemComponent = questItem.fetch(ItemComponent.class).get();
    QuestItem item = (QuestItem) itemComponent.item();
    // Assert.assertEquals("MyName", item.displayName());
    Quiz.Content content = (Quiz.Content) item.taskContentComponent().content();
    Assert.assertEquals("2", content.content());
  }

  /** WTF? . */
  @Test
  public void testSetGradingFuncSingleChoice() {
    String program =
        """
            single_choice_task t1 {
                description: "Task1",
                answers: ["1", "2", "3"],
                correct_answer_index: 2,
                grading_function: grade_single_choice_task
            }

            graph g {
                t1
            }

            dungeon_config c {
                dependency_graph: g
            }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();

    TaskContent content = task.contentByIndex(2);
    HashSet<TaskContent> tcSet = new HashSet<>();
    tcSet.add(content);
    var score = (Float) task.scoringFunction().apply(task, tcSet);
    Assert.assertEquals((Float) 1.0f, score);
  }

  /** WTF? . */
  @Test
  public void testSetGradingFuncMultipleChoice() {
    String program =
        """
        multiple_choice_task t1 {
            description: "Task1",
            answers: ["1", "2", "3"],
            correct_answer_indices: [2,1],
            grading_function: grade_multiple_choice_task
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }
    """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();

    var tcSet = task.contentStream().collect(Collectors.toSet());
    var score = (Float) task.scoringFunction().apply(task, tcSet);
    Assert.assertEquals((Float) 0.5f, score);
  }

  /** WTF? . */
  @Test
  public void testSetGradingFunctionInScenarioBuilderSingleChoice() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: ["1", "2", "3"],
            correct_answer_index: 2,
            scenario_builder: build_scenario1
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        fn build_scenario1(single_choice_task t) -> entity<><> {
            var ret_set : entity<><>;
            var first_room_set : entity<>;

            t.set_grading_function(grade_single_choice_task);

            ret_set.add(first_room_set);
            return ret_set;
        }
        """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();

    // force scoring function to be un-assigned
    task.scoringFunction(null);

    // execute scenario builder, in which the grading function will be setk
    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    TaskContent content = task.contentByIndex(2);
    HashSet<TaskContent> tcSet = new HashSet<>();
    tcSet.add(content);
    var score = (Float) task.scoringFunction().apply(task, tcSet);
    Assert.assertEquals((Float) 1.0f, score);
  }

  /** WTF? . */
  @Test
  public void testNameSymbol() {
    String program =
        """
                single_choice_task t1 {
                    description: "Task1",
                    answers: ["1", "2", "3"],
                    correct_answer_index: 2
                }

                graph g {
                    t1
                }

                dungeon_config c {
                    dependency_graph: g
                }
                """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();

    Assert.assertEquals("t1", task.taskName());
  }

  /** WTF? . */
  @Test
  public void testDeclareAssignmentTask() {
    String program =
        """
                assign_task t1 {
                    description: "Task1",
                    solution: <["a", "b"], ["c", "d"], ["y", "x"], ["c", "hallo"], [_, "world"], ["derp", _]>
                }

                graph g {
                    t1
                }

                dungeon_config c {
                    dependency_graph: g
                }
            """;

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (AssignTask) config.dependencyGraph().nodeIterator().next().task();

    var solutionMap = task.solution();

    // asserts
    var emptyElementSet = solutionMap.get(AssignTask.EMPTY_ELEMENT);
    Assert.assertNotEquals(null, emptyElementSet);
    Assert.assertEquals(1, emptyElementSet.size());

    var worldElement = emptyElementSet.stream().toList().get(0);
    Assert.assertEquals("world", worldElement.content());

    var aElement =
        solutionMap.keySet().stream().filter(e -> e.content().equals("a")).findFirst().get();
    var aElementSet = solutionMap.get(aElement);
    Assert.assertEquals(1, aElementSet.size());

    var bElement = aElementSet.stream().toList().get(0);
    Assert.assertEquals("b", bElement.content());

    var cElement =
        solutionMap.keySet().stream().filter(e -> e.content().equals("c")).findFirst().get();
    var cElementSet = solutionMap.get(cElement);
    Assert.assertEquals(2, cElementSet.size());

    var dElement = cElementSet.stream().filter(e -> e.content().equals("d")).findFirst().get();
    Assert.assertNotNull(dElement);

    var helloElement =
        cElementSet.stream().filter(e -> e.content().equals("hallo")).findFirst().get();
    Assert.assertNotNull(helloElement);

    var yElement =
        solutionMap.keySet().stream().filter(e -> e.content().equals("y")).findFirst().get();
    var yElementSet = solutionMap.get(yElement);
    Assert.assertEquals(1, yElementSet.size());

    var xElement = yElementSet.stream().toList().get(0);
    Assert.assertEquals("x", xElement.content());

    var derpElement =
        solutionMap.keySet().stream().filter(e -> e.content().equals("derp")).findFirst().get();
    var derpElementSet = solutionMap.get(derpElement);
    Assert.assertEquals(1, derpElementSet.size());

    var derpElementSetEntry = derpElementSet.stream().toList().get(0);
    Assert.assertEquals(AssignTask.EMPTY_ELEMENT, derpElementSetEntry);
  }

  /** WTF? . */
  @Test
  @Ignore // red on first run, green on subsequent runs??? wtf?
  public void testAssignmentTaskScenarioBuilder() {
    String program =
        """
        assign_task t1 {
            description: "Task1",
            solution: <["a", "b"], ["c", "d"], ["y", "x"], ["c", "hallo"], [_, "world"], ["!", _]>
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        entity_type chest_type {
            inventory_component {},
            draw_component {
                path: "objects/treasurechest"
            },
            hitbox_component {},
            position_component{},
            interaction_component{},
            task_content_component{}
        }

        item_type scroll_type {
            display_name: "A scroll",
            description: "Please read me",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_task(assign_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var solution_map : [element -> element<>];
            solution_map = t.get_solution();

            // instantiate chests
            for element key in solution_map.get_keys() {
                if key.is_empty() {
                    // skip
                } else {
                    // if this variable is declared outside of the for-loop,
                    // it is not correctly placed in the set, because the internal
                    // Value will be still the same Object (with the same HashCode!!)
                    var chest : entity;
                    chest = instantiate(chest_type);
                    chest.task_content_component.content = key;
                    room_set.add(chest);
                }
            }

            var item : quest_item;
            // instantiate all answer elements as scrolls
            for element<> element_set in solution_map.get_elements() {
                for element element in element_set {
                    if element.is_empty() {
                        // skip
                    } else {
                        print(element);
                        item = build_quest_item(scroll_type, element);
                        place_quest_item(item, room_set);
                    }
                }
            }

            return_set.add(room_set);
            return return_set;
        }
        """;
    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = (AssignTask) config.dependencyGraph().nodeIterator().next().task();

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();

    // find all "scrolls"
    HashSet<String> elementContents = new HashSet<>();
    for (var roomSet : builtTask) {
      for (core.Entity entity : roomSet) {
        var optionalItemComp = entity.fetch(ItemComponent.class);
        if (optionalItemComp.isPresent()) {
          ItemComponent itemComp = optionalItemComp.get();
          QuestItem questItem = (QuestItem) itemComp.item();
          var element = (Element<String>) questItem.taskContentComponent().content();
          elementContents.add(element.content());
        }
      }
    }

    Assert.assertEquals(5, elementContents.size());
    Assert.assertTrue(elementContents.contains("b"));
    Assert.assertTrue(elementContents.contains("d"));
    Assert.assertTrue(elementContents.contains("x"));
    Assert.assertTrue(elementContents.contains("hallo"));
    Assert.assertTrue(elementContents.contains("world"));

    // find all chests
    HashSet<String> keyContents = new HashSet<>();
    for (var roomSet : builtTask) {
      for (core.Entity entity : roomSet) {
        var optionalInventoryComponent = entity.fetch(InventoryComponent.class);
        if (optionalInventoryComponent.isPresent()) {
          TaskContentComponent tcc = entity.fetch(TaskContentComponent.class).get();
          Element<String> element = (Element<String>) tcc.content();
          keyContents.add(element.content());
        }
      }
    }

    Assert.assertEquals(4, keyContents.size());
    Assert.assertTrue(keyContents.contains("a"));
    Assert.assertTrue(keyContents.contains("c"));
    Assert.assertTrue(keyContents.contains("y"));
    Assert.assertTrue(keyContents.contains("!"));
  }

  /** WTF? . */
  @Test
  public void testNamedInstantiate() {
    String program =
        """
        single_choice_task t1 {
            description: "Task1",
            answers: ["1", "HELLO", "3"],
            correct_answer_index: 2,
            scenario_builder: build_task
        }

        graph g {
            t1
        }

        dungeon_config c {
            dependency_graph: g
        }

        entity_type chest_type {
            inventory_component {},
            draw_component {
                path: "objects/treasurechest"
            },
            hitbox_component {},
            position_component{},
            interaction_component{},
            task_content_component{}
        }

        item_type scroll_type {
            display_name: "A scroll",
            description: "Please read me",
            texture_path: "items/book/wisdom_scroll.png"
        }

        fn build_task(single_choice_task t) -> entity<><> {
            var return_set : entity<><>;
            var room_set : entity<>;

            var ent : entity;
            var content : task_content;
            content = t.get_content().get(1);
            ent = instantiate_named(chest_type, content.text());
            room_set.add(ent);
            t.set_scenario_text("CUSTOM TEXT");

            return_set.add(room_set);
            return return_set;
        }
        """;

    // print currently just prints to system.out, so we need to
    // check the contents for the printed string
    var outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    DSLInterpreter interpreter = new DSLInterpreter();
    DungeonConfig config = (DungeonConfig) interpreter.getQuestConfig(program);
    var task = config.dependencyGraph().nodeIterator().next().task();

    var builtTask = (HashSet<HashSet<core.Entity>>) interpreter.buildTask(task).get();
    core.Entity entity = builtTask.stream().toList().get(0).stream().findFirst().get();
    Assert.assertTrue(entity.toString().contains("HELLO"));
    Assert.assertEquals("CUSTOM TEXT", task.scenarioText());
  }
}
