package interpreter;

import static org.junit.Assert.*;

import dslToGame.QuestConfig;
import dslToGame.graph.Graph;

import helpers.Helpers;

import interpreter.mockecs.*;

import org.junit.Assert;
import org.junit.Test;

import parser.ast.Node;

import runtime.*;

import semanticanalysis.Scope;
import semanticanalysis.SemanticAnalyzer;
import semanticanalysis.types.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDSLInterpreter {
    /** Tests, if a native function call is evaluated by the DSLInterpreter */
    @Test
    public void funcCall() {
        String program =
                """
                quest_config c {
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
                quest_config c {
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

    @Test
    public void questConfigPartial() {
        // the quest_config type has also a quest_points and a password
        // parameter, these should be set to default values
        String program =
                """
                quest_config c {
                    quest_desc: "Hello"
                }
                    """;
        DSLInterpreter interpreter = new DSLInterpreter();

        var questConfig = (QuestConfig) interpreter.getQuestConfig(program);
        assertEquals(0, questConfig.questPoints());
        assertEquals("Hello", questConfig.questDesc());
        assertEquals("", questConfig.password());
    }

    /** Test, if the properties of the quest_config definition are correctly parsed */
    @Test
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

        var questConfig = (QuestConfig) interpreter.getQuestConfig(program);
        assertEquals(42, questConfig.questPoints());
        assertEquals("Hello", questConfig.questDesc());
        assertEquals("TESTPW", questConfig.password());
        var graph = questConfig.levelGraph();

        var edgeIter = graph.edgeIterator();
        int edgeCount = 0;
        while (edgeIter.hasNext()) {
            edgeIter.next();
            edgeCount++;
        }
        assertEquals(1, edgeCount);

        var nodeIter = graph.nodeIterator();
        int nodeCount = 0;
        while (nodeIter.hasNext()) {
            nodeIter.next();
            nodeCount++;
        }
        assertEquals(2, nodeCount);
    }

    @DSLType
    private record TestComponent(@DSLTypeMember int member1, @DSLTypeMember String member2) {}

    @DSLType
    private record OtherComponent(
            @DSLTypeMember int member3, @DSLTypeMember Graph<String> member4) {}

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
        DSLInterpreter interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program, env, interpreter, Entity.class, ComponentWithExternalTypeMember.class);

        var globalMs = interpreter.getGlobalMemorySpace();

        // check, if the component was instantiated and the
        // Position member is set to null, because the Position type is not supported
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
        assertNull(((ComponentWithExternalTypeMember) internalComponent).position);
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
                        member_external_type: "Hello, World!"
                    }
                }

                quest_config config {
                    entity: instantiate(my_obj)
                }
                """;

        // setup test type system
        var env = new TestEnvironment();
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, Scope.NULL);
        DSLInterpreter interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponent1.class,
                TestComponentWithExternalType.class,
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
                externalTypeMemberValue.getDataType().getTypeKind(), IType.Kind.PODAdapted);

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
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
        DSLInterpreter interpreter = new DSLInterpreter();
        Helpers.generateQuestConfigWithCustomTypes(
                program,
                env,
                interpreter,
                Entity.class,
                TestComponent1.class,
                TestComponentWithExternalType.class,
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
                        new Graph<String>(new ArrayList<>(), new ArrayList<>()));
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
}
