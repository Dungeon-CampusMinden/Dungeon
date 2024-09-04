package newdsl.dynamiccontent;

import newdsl.NewDSLHandler;
import newdsl.interpreter.DSLInterpreter;
import newdsl.tasks.MatchingTask;
import org.junit.jupiter.api.Test;

public class VariantTest {

    String path = "/test_resources/newdsl/dynamiccontent/variant.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Atlantik";

    MatchingTask task = (MatchingTask) interpreter.env.get(taskName);

    @Test
    public void selectsAnswers() {
        assert (task.getAnswers().size() == 2);
    }
}
