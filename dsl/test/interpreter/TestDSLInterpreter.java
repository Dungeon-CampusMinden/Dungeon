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
        assertNotSame(42, Value.NONE.getInternalObject());
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

        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);
        var otherCompType = tb.createTypeFromClass(new Scope(), OtherComponent.class);

        var env = new GameEnvironment();
        env.loadTypes(testCompType, otherCompType);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        var questConfig = interpreter.generateQuestConfig(ast);
        var rtEnv = interpreter.getRuntimeEnvironment();

        var typeWithDefaults = rtEnv.lookupPrototype("c");
        assertNotEquals(EntityType.NONE, typeWithDefaults);

        var firstCompWithDefaults = typeWithDefaults.getDefaultValue("test_component");
        assertNotEquals(Value.NONE, firstCompWithDefaults);
        assertTrue(firstCompWithDefaults instanceof EntityType);

        var secondCompWithDefaults = typeWithDefaults.getDefaultValue("other_component");
        assertNotEquals(Value.NONE, secondCompWithDefaults);
        assertTrue(secondCompWithDefaults instanceof EntityType);

        // check members of components
        var member1Value = ((EntityType) firstCompWithDefaults).getDefaultValue("member1");
        assertNotEquals(Value.NONE, member1Value);
        assertEquals(BuiltInType.intType, member1Value.getDataType());
        assertEquals(42, member1Value.getInternalObject());

        var member2Value = ((EntityType) firstCompWithDefaults).getDefaultValue("member2");
        assertNotEquals(Value.NONE, member2Value);
        assertEquals(BuiltInType.stringType, member2Value.getDataType());
        assertEquals("Hello, World!", member2Value.getInternalObject());
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
                    entity: my_obj
                }
                """;

        TypeBuilder tb = new TypeBuilder();
        var entityType = tb.createTypeFromClass(new Scope(), Entity.class);
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent1.class);
        var otherCompType = tb.createTypeFromClass(new Scope(), TestComponent2.class);

        var env = new TestEnvironment();
        env.loadTypes(entityType, testCompType, otherCompType);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        var entity = ((CustomQuestConfig) interpreter.generateQuestConfig(ast)).entity();
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
        var testComp1Internal = testComp1EncapsulatedObj.getInternalObject();
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
        var testComp2Internal = testComp2EncapsulatedObj.getInternalObject();
        assertTrue(testComp2Internal instanceof TestComponent2);

        TestComponent2 testComp2 = (TestComponent2) testComp2Internal;
        assertEquals(entity, testComp2.getEntity());

        // check member-values
        assertEquals("Hallo", testComp2.getMember1());
        assertEquals(123, testComp2.getMember2());
        assertEquals("DEFAULT VALUE", testComp2.getMember3());
    }

    @Test
    public void aggregateTypeInstancingNonSupportedExternalType() {
        String program =
                """
            entity_type my_obj {
                component_with_external_type_member { }
            }

            quest_config config {
                entity: my_obj
            }
            """;

        TypeBuilder tb = new TypeBuilder();
        var entityType = tb.createTypeFromClass(new Scope(), Entity.class);
        var compType = tb.createTypeFromClass(new Scope(), ComponentWithExternalTypeMember.class);

        var env = new TestEnvironment();
        env.loadTypes(entityType, compType);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        interpreter.generateQuestConfig(ast);
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
        var internalComponent = encapsulatedObject.getInternalObject();

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
                    member_external_type: "Hello, World!"
                }
            }

            quest_config config {
                entity: my_obj
            }
            """;

        // setup test type system
        var env = new TestEnvironment();
        var entityType = env.getTypeBuilder().createTypeFromClass(new Scope(), Entity.class);
        var testCompType =
                env.getTypeBuilder().createTypeFromClass(new Scope(), TestComponent1.class);

        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilder.class, Scope.NULL);
        var externalComponentType =
                env.getTypeBuilder()
                        .createTypeFromClass(Scope.NULL, TestComponentWithExternalType.class);
        var externalType = env.getTypeBuilder().createTypeFromClass(Scope.NULL, ExternalType.class);
        env.loadTypes(entityType, testCompType, externalComponentType, externalType);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        interpreter.generateQuestConfig(ast);

        var globalMs = interpreter.getGlobalMemorySpace();
        AggregateValue config = (AggregateValue) (globalMs.resolve("config"));
        AggregateValue myObj = (AggregateValue) config.getMemorySpace().resolve("entity");
        AggregateValue component =
                (AggregateValue)
                        myObj.getMemorySpace().resolve("test_component_with_external_type");
        var internalObject = (TestComponentWithExternalType) component.getInternalObject();
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
            entity: my_obj
        }
        """;

        // setup test type system
        var env = new TestEnvironment();
        var entityType = env.getTypeBuilder().createTypeFromClass(new Scope(), Entity.class);
        var testCompType =
                env.getTypeBuilder().createTypeFromClass(new Scope(), TestComponent1.class);

        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
        var externalComponentType =
                env.getTypeBuilder()
                        .createTypeFromClass(Scope.NULL, TestComponentWithExternalType.class);
        var adapterType = env.getTypeBuilder().createTypeFromClass(Scope.NULL, ExternalType.class);
        env.loadTypes(entityType, testCompType, externalComponentType, adapterType);

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        interpreter.generateQuestConfig(ast);

        var globalMs = interpreter.getGlobalMemorySpace();
        AggregateValue config = (AggregateValue) (globalMs.resolve("config"));
        AggregateValue myObj = (AggregateValue) config.getMemorySpace().resolve("entity");
        AggregateValue component =
                (AggregateValue)
                        myObj.getMemorySpace().resolve("test_component_with_external_type");
        var internalObject = (TestComponentWithExternalType) component.getInternalObject();
        ExternalType externalTypeMember = internalObject.getMemberExternalType();
        Assert.assertEquals("Hello, World!", externalTypeMember.member3);
        Assert.assertEquals(42, externalTypeMember.member1);
    }


    @Test
    public void instanceByFunction() {
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

        TypeBuilder tb = new TypeBuilder();
        var entityType = tb.createTypeFromClass(new Scope(), Entity.class);
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent1.class);
        var otherCompType = tb.createTypeFromClass(new Scope(), TestComponent2.class);

        var env = new TestEnvironment();
        var typesToLoad = new IType[] {entityType, testCompType, otherCompType};
        env.loadTypes(List.of(typesToLoad));

        SemanticAnalyzer symbolTableParser = new SemanticAnalyzer();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        var entity = ((CustomQuestConfig) interpreter.generateQuestConfig(ast)).entity();
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
        var testComp1Internal = testComp1EncapsulatedObj.getInternalObject();
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
        var testComp2Internal = testComp2EncapsulatedObj.getInternalObject();
        assertTrue(testComp2Internal instanceof TestComponent2);

        TestComponent2 testComp2 = (TestComponent2) testComp2Internal;
        assertEquals(entity, testComp2.getEntity());

        // check member-values
        assertEquals("Hallo", testComp2.getMember1());
        assertEquals(123, testComp2.getMember2());
        assertEquals("DEFAULT VALUE", testComp2.getMember3());
    }
}
