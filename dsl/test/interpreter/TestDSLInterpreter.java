package interpreter;

import static org.junit.Assert.*;

import helpers.Helpers;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import parser.AST.Node;

public class TestDSLInterpreter {
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
    public void questConfigFull() {
        String program =
                """
            graph g {
                A -- B
            }
            quest_config c {
                level_graph: g,
                quest_points: 42,
                quest_desc: "Hello"
            }
            """;
        DSLInterpreter interpreter = new DSLInterpreter();
        var questConfig = interpreter.getQuestConfig(program);
        assertEquals(42, questConfig.points());
        assertEquals("Hello", questConfig.taskDescription());
        var graph = questConfig.levelGenGraph();

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
