package newdsl.tasks;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class CalculationTest {

    String path = "/test_resources/newdsl/tasks/calculation.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Task1";

    CalculationTask task = (CalculationTask) interpreter.env.get(taskName);

    @Test
    public void createsTask() {
        assert (task.getId().equals(taskName));
        assert (task.getType().equals(ASTNodes.TaskType.CALCULATION));
    }

    @Test
    public void canBeAnswered() {
        Set<ParameterAnswer> answers = new HashSet<>();
        answers.add(new ParameterAnswer(null, "3"));

        float points = task.gradeTask(answers);

        assert (points > 0);
    }

}
