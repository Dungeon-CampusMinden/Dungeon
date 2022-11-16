package interpreter;

import static org.junit.Assert.*;

import helpers.Helpers;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import parser.AST.Node;

public class TestDSLInterpreter {
    @Test
    public void testGetQuestConfigSimpleGraph() {
        String program = "graph g {\n" + "A -- B \n" + "B -- C -- D -> E \n" + "}";
        DSLInterpreter interpreter = new DSLInterpreter();
        var questConfig = interpreter.getQuestConfig(program);
        assertNotNull(questConfig);
        assertNotNull(questConfig.levelGenGraph());

        var edgeIter = questConfig.levelGenGraph().getEdgeIterator();
        AtomicInteger edgeCount = new AtomicInteger(0);
        edgeIter.forEachRemaining(elem -> edgeCount.addAndGet(1));
        assertEquals(4, edgeCount.get());
    }

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
}
