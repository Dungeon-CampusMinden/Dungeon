package newdsl.interpreter;

import newdsl.NewDSLHandler;
import newdsl.TaskHandler;
import newdsl.graph.TaskDependencyGraph;
import newdsl.tasks.ChoiceAnswer;
import newdsl.tasks.TaskState;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class TaskHandlerTest {

    String path = "/test_resources/newdsl/interpreter/reihenfolge.task";

    String configName = "MeineAufgaben";

    TaskDependencyGraph taskSetup = new NewDSLHandler(path).getInterpreter().getTaskDependencyGraph(configName);


    @Test
    public void correctPath() {
        TaskHandler assignment = new TaskHandler(taskSetup);

        Set<ChoiceAnswer> answers = new HashSet<>();

        answers.add(new ChoiceAnswer("George Washington"));
        assignment.giveAnswers(answers);
        assert(assignment.getCurrent().getState() == TaskState.FINISHED_CORRECT);

        assignment.visitNextTask();
        assert(assignment.getCurrent().getId().equals("Literatur"));
        answers.clear();
        answers.add(new ChoiceAnswer("Virginia Woolf"));
        assignment.enterSolution(answers);
        assert(assignment.getCurrent().getState() == TaskState.FINISHED_WRONG);

        assignment.visitNextTask();
        assert(assignment.getCurrent().getId().equals("Technologie"));
        assignment.visitNextTask(); // skipping optional tasks is allowed
        assert(assignment.getCurrent().getId().equals("Biologie"));

    }

    @Test
    public void incorrectPath() {
        TaskHandler assignment = new TaskHandler(taskSetup);

        Set<ChoiceAnswer> answers = new HashSet<>();

        answers.add(new ChoiceAnswer("Thomas Jefferson"));
        assignment.giveAnswers(answers);
        assert(assignment.getCurrent().getState() == TaskState.FINISHED_WRONG);

        assignment.visitNextTask();
        assert(assignment.getCurrent().getId().equals("Musik"));
        answers.clear();
        answers.add(new ChoiceAnswer("Mozart"));
        answers.add(new ChoiceAnswer("Beethoven"));
        assignment.enterSolution(answers);
        assert(assignment.getCurrent().getState() == TaskState.FINISHED_CORRECT);
    }

    @Test
    public void goingBack() {
        TaskHandler assignment = new TaskHandler(taskSetup);

        Set<ChoiceAnswer> answers = new HashSet<>();

        answers.add(new ChoiceAnswer("Thomas Jefferson"));
        assignment.giveAnswers(answers);
        assert(assignment.getCurrent().getState() == TaskState.FINISHED_WRONG);

        assignment.visitNextTask();
        assert(assignment.getCurrent().getId().equals("Musik"));
        assignment.visitPreviousTask();
        assert(assignment.getCurrent().getId().equals("Geschichte"));
    }

}
