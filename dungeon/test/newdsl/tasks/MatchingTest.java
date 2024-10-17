package newdsl.tasks;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class MatchingTest {

    String path = "/test_resources/newdsl/tasks/matching.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Erdkunde";

    MatchingTask task = (MatchingTask) interpreter.env.get(taskName);

    @Test
    public void createsTask() {
        assert (task.getId().equals(taskName));
        assert (task.getType().equals(ASTNodes.TaskType.MATCHING));
    }
}
