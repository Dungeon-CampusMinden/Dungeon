package newdsl.dynamiccontent;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import newdsl.tasks.MatchingTask;
import org.junit.jupiter.api.Test;

public class SelectionTest {

    String path = "/test_resources/newdsl/dynamiccontent/select.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Erdkunde";

    MatchingTask task = (MatchingTask) interpreter.env.get(taskName);

    @Test
    public void selectsAnswers() {
        assert (task.getAnswers().size() == 4);
    }
}
