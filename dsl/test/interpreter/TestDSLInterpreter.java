package interpreter;

import static org.junit.Assert.*;

import graph.Graph;
import helpers.Helpers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;
import parser.AST.Node;
import runtime.AggregateTypeWithDefaults;
import runtime.GameEnvironment;
import runtime.Value;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.SymbolTableParser;
import semanticAnalysis.types.BuiltInType;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;
import semanticAnalysis.types.TypeBuilder;

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

        var questConfig = interpreter.getQuestConfig(program);
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

        var questConfig = interpreter.getQuestConfig(program);
        assertEquals(42, questConfig.questPoints());
        assertEquals("Hello", questConfig.questDesc());
        assertEquals("TESTPW", questConfig.password());
        var graph = questConfig.levelGraph();

        var edgeIter = graph.getEdgeIterator();
        int edgeCount = 0;
        while (edgeIter.hasNext()) {
            edgeIter.next();
            edgeCount++;
        }
        assertEquals(1, edgeCount);

        var nodeIter = graph.getNodeIterator();
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

                game_object c {
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
        env.loadTypes(new Symbol[] {testCompType, otherCompType});

        SymbolTableParser symbolTableParser = new SymbolTableParser();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        var questConfig = interpreter.generateQuestConfig(ast);
        var rtEnv = interpreter.getRuntimeEnvironment();

        var typeWithDefaults = rtEnv.lookupTypeWithDefaults("c");
        assertNotEquals(AggregateTypeWithDefaults.NONE, typeWithDefaults);

        var firstCompWithDefaults = typeWithDefaults.getDefaultValue("test_component");
        assertNotEquals(Value.NONE, firstCompWithDefaults);
        assertTrue(firstCompWithDefaults instanceof AggregateTypeWithDefaults);

        var secondCompWithDefaults = typeWithDefaults.getDefaultValue("other_component");
        assertNotEquals(Value.NONE, secondCompWithDefaults);
        assertTrue(secondCompWithDefaults instanceof AggregateTypeWithDefaults);

        // check members of components
        var member1Value =
                ((AggregateTypeWithDefaults) firstCompWithDefaults).getDefaultValue("member1");
        assertNotEquals(Value.NONE, member1Value);
        assertEquals(BuiltInType.intType, member1Value.getDataType());
        assertEquals(42, member1Value.getInternalValue());

        var member2Value =
                ((AggregateTypeWithDefaults) firstCompWithDefaults).getDefaultValue("member2");
        assertNotEquals(Value.NONE, member2Value);
        assertEquals(BuiltInType.stringType, member2Value.getDataType());
        assertEquals("Hello, World!", member2Value.getInternalValue());
    }

    @DSLType(name = "quest_config")
    public record CustomQuestConfig(@DSLTypeMember Object gameObject) {}

    class TestEnvironment extends GameEnvironment {
        public TestEnvironment() {
            super();
        }

        @Override
        protected void bindBuiltIns() {
            for (Symbol type : builtInTypes) {
                // load custom QuestConfig
                if (!type.getName().equals("quest_config")) {
                    globalScope.bind(type);
                }
            }

            TypeBuilder tp = new TypeBuilder();
            var questConfigType = tp.createTypeFromClass(Scope.NULL, CustomQuestConfig.class);
            loadTypes(new semanticAnalysis.types.AggregateType[] {questConfigType});

            for (Symbol func : nativeFunctions) {
                globalScope.bind(func);
            }
        }
    }

    @Test
    @Ignore
    public void aggregateTypeInstancing() {
        String program =
                """
                graph g {
                    A -- B
                }

                quest_config config {
                    game_object: c
                }
                """;

        TypeBuilder tb = new TypeBuilder();
        var testCompType = tb.createTypeFromClass(new Scope(), TestComponent.class);
        var otherCompType = tb.createTypeFromClass(new Scope(), OtherComponent.class);

        var env = new TestEnvironment();
        env.loadTypes(new Symbol[] {testCompType, otherCompType});

        SymbolTableParser symbolTableParser = new SymbolTableParser();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        // var questConfig = interpreter.generateQuestConfig(ast);
        // var rtEnv = interpreter.getRuntimeEnvironment();
    }

    @Test
    public void testDontOverwriteCtorDefaults() {
        String program =
            """
                game_object my_obj {
                    component_with_default_ctor {
                        member1:  "Hello, World!",
                        member2: 42
                    }
                }
            """;

        TypeBuilder tb = new TypeBuilder();
        var compWithDefaultsType = tb.createTypeFromClass(new Scope(), ComponentWithDefaultCtor.class);

        var env = new GameEnvironment();
        env.loadTypes(new Symbol[] {compWithDefaultsType});

        SymbolTableParser symbolTableParser = new SymbolTableParser();
        symbolTableParser.setup(env);
        var ast = Helpers.getASTFromString(program);
        symbolTableParser.walk(ast);

        DSLInterpreter interpreter = new DSLInterpreter();
        interpreter.initializeRuntime(env);

        interpreter.generateQuestConfig(ast);
        // extract memory space corresponding to the game object
        var memSpace = interpreter.getGlobalMemorySpace();

        // TODO: this does not work, because the gmae object definition
        //  is currently not instantiated
        var obj = memSpace.resolve("my_obj");
        assertNotEquals(Value.NONE, obj);
    }
}
