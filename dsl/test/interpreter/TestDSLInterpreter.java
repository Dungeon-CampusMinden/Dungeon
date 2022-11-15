package interpreter;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
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
}
