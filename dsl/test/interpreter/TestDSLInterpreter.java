package interpreter;

import static org.junit.Assert.*;

import contrib.components.CollideComponent;

import core.components.DrawComponent;
import core.components.PositionComponent;

import dslnativefunction.NativeInstantiate;

import dungeonFiles.DungeonConfig;

import helpers.Helpers;

import interpreter.mockecs.*;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import parser.ast.IdNode;
import parser.ast.Node;

import runtime.*;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.SemanticAnalyzer;
import semanticanalysis.types.*;

import task.Quiz;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;

import taskdependencygraph.TaskDependencyGraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class TestDSLInterpreter {
    /** Tests, if a native function call is evaluated by the DSLInterpreter */
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

    /** Test, if Value.NULL does not get set, if non-existing property of datatype is assigned */
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
     * Test, if a dot definition and object definition is correctly created
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

    /** Test, if the properties of the quest_config definition are correctly parsed */
    @Test
    @Ignore
    // TODO: adapt to new dungeonConfig and task dependency graph (see:
    // https://github.com/Programmiermethoden/Dungeon/issues/520)
    public void questConfigFull() {
        String program =
                """
                graph g {
                    A -- B
                }
                quest_config c {
                    level_graph: g,
                    quest_points: 42,
                    quest_desc: "Hello",
                    password: "TESTPW"
                }
                    """;
        DSLInterpreter interpreter = new DSLInterpreter();

        var questConfig = (DungeonConfig) interpreter.getQuestConfig(program);
        var taksDependencyGraph = questConfig.dependencyGraph();
    }

    @DSLType
    private record TestComponent(@DSLTypeMember int member1, @DSLTypeMember String member2) {}

    @DSLType
    private record OtherComponent(
            @DSLTypeMember int member3, @DSLTypeMember TaskDependencyGraph member4) {}

    @Test
    public void aggregateTypeWithDefaults() {
        String program =
                """
                graph g {
                    A -- B
                }

                entity_type c {
                    test_component{
                        member1: 42,
                        member2: "Hello, World!"
                    },
                    other_component{
                        member3: 314,
                        member4: g
                    }
                }
                """;

        var env = new GameEnvironment();
        var interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program, env, interpreter, TestComponent.class, OtherComponent.class);

        var rtEnv = interpreter.getRuntimeEnvironment();

        var typeWithDefaults = rtEnv.lookupPrototype("c");
        assertNotEquals(Prototype.NONE, typeWithDefaults);

        var firstCompWithDefaults = typeWithDefaults.getDefaultValue("test_component");
        assertNotEquals(Value.NONE, firstCompWithDefaults);
        assertTrue(firstCompWithDefaults instanceof Prototype);

        var secondCompWithDefaults = typeWithDefaults.getDefaultValue("other_component");
        assertNotEquals(Value.NONE, secondCompWithDefaults);
        assertTrue(secondCompWithDefaults instanceof Prototype);

        // check members of components
        var member1Value = ((Prototype) firstCompWithDefaults).getDefaultValue("member1");
        assertNotEquals(Value.NONE, member1Value);
        assertEquals(BuiltInType.intType, member1Value.getDataType());
        assertEquals(42, member1Value.getInternalValue());

        var member2Value = ((Prototype) firstCompWithDefaults).getDefaultValue("member2");
        assertNotEquals(Value.NONE, member2Value);
        assertEquals(BuiltInType.stringType, member2Value.getDataType());
        assertEquals("Hello, World!", member2Value.getInternalValue());
    }

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

        var interpreter = new DSLInterpreter();
        var questConfig =
                Helpers.generateQuestConfigWithCustomTypes(
                        program,
                        env,
                        interpreter,
                        Entity.class,
                        TestComponent1.class,
                        TestComponent2.class);

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

        var interpreter = new DSLInterpreter();
        var questConfig =
                Helpers.generateQuestConfigWithCustomTypes(
                        program,
                        env,
                        interpreter,
                        Entity.class,
                        TestComponent1.class,
                        TestComponent2.class);

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
                        env.getGlobalScope(),
                        Entity.ComponentWithExternalTypeMemberProperty.instance);

        DSLInterpreter interpreter = new DSLInterpreter();

        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter, Entity.class);

        var globalMs = interpreter.getGlobalMemorySpace();

        // check, if the component was instantiated and the
        // Point member is set to null, because the Point type is not supported
        // by the Typesystem
        var config = (AggregateValue) (globalMs.resolve("config"));
        var myObj = config.getMemorySpace().resolve("entity");
        var component =
                ((AggregateValue) myObj)
                        .getMemorySpace()
                        .resolve("component_with_external_type_member");

        var encapsulatedObject = (EncapsulatedObject) ((AggregateValue) component).getMemorySpace();
        var internalComponent = encapsulatedObject.getInternalValue();

        assertTrue(internalComponent instanceof ComponentWithExternalTypeMember);
        assertNull(((ComponentWithExternalTypeMember) internalComponent).point);
    }

    // TODO: should test resolving of member_external_type in the instantiated object
    @Test
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
        env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), ExternalType.class);
        env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(
                        env.getGlobalScope(), TestComponentWithExternalType.class);
        env.getTypeBuilder()
                .bindProperty(
                        env.getGlobalScope(),
                        Entity.TestComponentWithExternalTypeProperty.instance);

        DSLInterpreter interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program, env, interpreter, Entity.class, TestComponent1.class);

        var globalMs = interpreter.getGlobalMemorySpace();
        AggregateValue config = (AggregateValue) (globalMs.resolve("config"));
        AggregateValue myObj = (AggregateValue) config.getMemorySpace().resolve("entity");
        AggregateValue component =
                (AggregateValue)
                        myObj.getMemorySpace().resolve("test_component_with_external_type");

        Value externalTypeMemberValue = component.getMemorySpace().resolve("member_external_type");
        Assert.assertNotEquals(externalTypeMemberValue, Value.NONE);
        Assert.assertEquals(
                externalTypeMemberValue.getDataType().getTypeKind(), IType.Kind.AggregateAdapted);

        var internalObject = (TestComponentWithExternalType) component.getInternalValue();
        ExternalType externalTypeMember = internalObject.getMemberExternalType();
        Assert.assertEquals("Hello, World!", externalTypeMember.member3);
    }

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
                .createDSLTypeForJavaTypeInScope(
                        env.getGlobalScope(), TestComponentWithExternalType.class);
        env.getTypeBuilder()
                .bindProperty(
                        env.getGlobalScope(),
                        Entity.TestComponentWithExternalTypeProperty.instance);
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
                (AggregateValue)
                        myObj.getMemorySpace().resolve("test_component_with_external_type");

        Value externalTypeMemberValue = component.getMemorySpace().resolve("member_external_type");
        Assert.assertNotEquals(externalTypeMemberValue, Value.NONE);
        Assert.assertEquals(
                externalTypeMemberValue.getDataType().getTypeKind(), IType.Kind.AggregateAdapted);

        var internalObject = (TestComponentWithExternalType) component.getInternalValue();
        ExternalType externalTypeMember = internalObject.getMemberExternalType();
        Assert.assertEquals("Hello, World!", externalTypeMember.member3);
        Assert.assertEquals(42, externalTypeMember.member1);
    }

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
                        BuiltInType.graphType,
                        new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>()));
        Assert.assertTrue(DSLInterpreter.isBooleanTrue(graphValue));
    }

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
                        Helpers.generateQuestConfigWithCustomTypes(
                                program, env, interpreter, Entity.class);

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
                        Helpers.generateQuestConfigWithCustomTypes(
                                program, env, interpreter, Entity.class);

        // cast to Integer to make the compiler happy
        Assert.assertEquals((Integer) 1, config.intList().get(0));
        Assert.assertEquals((Integer) 2, config.intList().get(1));
        Assert.assertEquals((Integer) 3, config.intList().get(2));

        Assert.assertEquals("Hello", config.stringList().get(0));
        Assert.assertEquals("World", config.stringList().get(1));
        Assert.assertEquals("!", config.stringList().get(2));
    }

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
                        Helpers.generateQuestConfigWithCustomTypes(
                                program, env, interpreter, Entity.class);

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

        var testComponentWithCallback =
                (TestComponentListCallback) config.entity().components.get(0);

        ArrayList<Entity> entities = new ArrayList<>();
        entities.add(new Entity());
        entities.add(new Entity());
        entities.add(new Entity());

        boolean returnValue = testComponentWithCallback.executeCallbackWithText(entities);
        Assert.assertTrue(returnValue);
    }

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
                                program,
                                env,
                                interpreter,
                                Entity.class,
                                TestComponentSetPassThroughCallback.class);

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

    @Test
    @Ignore
    // TODO: requires implementation of task dependency graph parsing (see:
    // https://github.com/Programmiermethoden/Dungeon/issues/520)
    public void taskDefinition() {
        String program =
                """
                    single_choice_task my_single_choice_task {
                        description: "Hello",
                        answers: ["1", "2", "3"],
                        correct_answer_index: 1
                    }

                    multiple_choice_task my_multiple_choice_task {
                        description: "Tschüss",
                        answers: ["4", "5", "6"],
                        correct_answer_index: [0,1]
                    }

                    dungeon_config c {
                        tasks: [my_single_choice_task, my_multiple_choice_task]
                    }
                """;

        DSLInterpreter interpreter = new DSLInterpreter();
        var config = (DungeonConfig) interpreter.getQuestConfig(program);

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
    }

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
        var testComponentWithCallback =
                (TestComponentStringMemberAndCallback) entity.components.get(1);
        var testComponent2 = (TestComponent2) config.entity().components.get(0);
        testComponentWithCallback.getConsumer().accept(testComponent2);

        String outputStreamString = outputStream.toString();
        Assert.assertTrue(outputStreamString.contains("Hello, World!"));
    }

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
                .bindProperty(
                        env.getGlobalScope(), TestComponent2.TestComponentPseudoProperty.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

        var entity = config.entity();
        var componentWithConsumer =
                (TestComponentTestComponent2ConsumerCallback) entity.components.get(0);
        var testComponent2 = (TestComponent2) entity.components.get(1);
        componentWithConsumer.consumer.accept(testComponent2);

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("3.14"));
    }

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
                        env.getGlobalScope(),
                        TestComponent2.TestComponentPseudoPropertyComplexType.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

        var entity = config.entity();
        var componentWithConsumer =
                (TestComponentTestComponent2ConsumerCallback) entity.components.get(0);
        var testComponent2 = (TestComponent2) entity.components.get(1);
        componentWithConsumer.consumer.accept(testComponent2);

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("42"));
    }

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

        var entity = config.entity();
        var componentWithConsumer = (TestComponentEntityConsumerCallback) entity.components.get(0);
        componentWithConsumer.consumer.accept(entity);

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("42"));
    }

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                        entity.components.stream()
                                .filter(c -> c instanceof TestComponent2)
                                .toList()
                                .get(0);
        entity.components.remove(oldComponent);

        TestComponent2 newComp = new TestComponent2(entity);
        newComp.setMember2(123);

        // second call to dsl function get_property
        componentWithConsumer.consumer.accept(entity);

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("42"));
        Assert.assertTrue(output.contains("123"));
    }

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);
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
                output.equals(
                        "kuckuck" + System.lineSeparator() + "kuckuck" + System.lineSeparator()));
    }

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                output.equals(
                        "hello" + System.lineSeparator() + "my text" + System.lineSeparator()));
    }

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder().bindMethod(env.getGlobalScope(), TestComponent2.MyMethod.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent1Property.instance);
        env.getTypeBuilder()
                .bindProperty(env.getGlobalScope(), Entity.TestComponent2Property.instance);
        env.getTypeBuilder().bindMethod(env.getGlobalScope(), TestComponent2.MyMethod.instance);

        var config =
                (CustomQuestConfig)
                        Helpers.generateQuestConfigWithCustomTypes(program, env, interpreter);

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
}
