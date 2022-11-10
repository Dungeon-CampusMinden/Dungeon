package interpreter;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestDSLInterpreter {
    @Test
    public void testGetQuestConfigSimpleGraph() {
        String program = "graph g {\n" + "A -- B \n" + "B -- C -- D -> E \n" + "}";
        DSLInterpreter interpreter = new DSLInterpreter();
        var questConfig = interpreter.getQuestConfig(program);
        assertNotNull(questConfig);
        assertNotNull(questConfig.levelGenGraph());
    }
}
