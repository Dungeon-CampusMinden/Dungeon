package newdsl.tasks;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class SingleChoiceTest {

    String path = "/test_resources/newdsl/tasks/singlechoice.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "Task1";

    SingleChoiceTask task = (SingleChoiceTask) interpreter.env.get(taskName);

    @Test
    public void createsTask() {
        assert (task.getId().equals(taskName));
        assert (task.getType().equals(ASTNodes.TaskType.SINGLE_CHOICE));
    }

    @Test
    public void canBeAnswered() {
        Set<ChoiceAnswer> answers = new HashSet<>();
        answers.add(new ChoiceAnswer("Berlin"));

        float points = task.gradeTask(answers);

        assert (points > 0);
    }

}
