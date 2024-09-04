package newdsl.tasks;

import newdsl.NewDSLHandler;
import newdsl.ast.ASTNodes;
import newdsl.interpreter.DSLInterpreter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class MultipleChoiceTest {

    String path = "/test_resources/newdsl/tasks/multiplechoice.task";
    DSLInterpreter interpreter = new NewDSLHandler(path).getInterpreter();
    String taskName = "GeographieDE";

    MultipleChoiceTask task = (MultipleChoiceTask) interpreter.env.get(taskName);

    @Test
    public void createsTask() {
        assert (task.getId().equals(taskName));
        assert (task.getType().equals(ASTNodes.TaskType.MULTIPLE_CHOICE));
    }

    @Test
    public void canBeAnswered() {
        Set<ChoiceAnswer> answers = new HashSet<>();
        answers.add(new ChoiceAnswer("Minden"));

        float points = task.gradeTask(answers);

        assert (points > 0);
    }

}
