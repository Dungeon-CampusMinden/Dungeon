package interpreter;

import static org.junit.Assert.*;

import helpers.Helpers;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

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
    public void testQuestConfigParsing() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("program.ds");
        assert resource != null;
        var ast = Helpers.getASTFromResourceFile(resource);
        Assert.assertNotNull(ast);

        Assert.assertNotNull(null);
    }
}
