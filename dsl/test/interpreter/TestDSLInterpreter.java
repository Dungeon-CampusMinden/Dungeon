package interpreter;

import static org.junit.Assert.*;

import graph.Graph;
import helpers.Helpers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
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

}
