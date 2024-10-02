package newdsl.tasks;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import org.junit.jupiter.api.Test;

public class FillInTheBlankTest {

    String path = "/test_resources/newdsl/tasks/fitb.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Nerdwissen";

    FillInTheBlankTask task = (FillInTheBlankTask) interpreter.env.get(taskName);

    @Test
    public void createsTask() {
        assert (task.getId().equals(taskName));
        assert (task.getType().equals(ASTNodes.TaskType.FILL_IN_THE_BLANK));
    }

}
