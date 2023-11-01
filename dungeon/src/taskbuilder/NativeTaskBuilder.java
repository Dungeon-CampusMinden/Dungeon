package taskbuilder;

import core.Entity;

import task.AssignTask;
import task.Task;
import task.quizquestion.MultipleChoice;
import task.quizquestion.SingleChoice;

import java.util.HashSet;
import java.util.Optional;

public class NativeTaskBuilder implements ITaskBuilder {
    @Override
    public Optional<Object> buildTask(Task task) {

        if (task instanceof SingleChoice)
            return Optional.ofNullable(singleChoiceA((SingleChoice) task));
        else if (task instanceof MultipleChoice)
            return Optional.ofNullable(multipleChoiceA((MultipleChoice) task));
        else if (task instanceof AssignTask)
            return Optional.ofNullable(assignTaskA((AssignTask) task));
        // HashSet<HashSet<core.Entity>>.
        return Optional.empty();
    }

    private HashSet<HashSet<Entity>> singleChoiceA(SingleChoice task) {
        return null;
    }

    private HashSet<HashSet<Entity>> multipleChoiceA(MultipleChoice task) {
        return null;
    }

    private HashSet<HashSet<Entity>> assignTaskA(AssignTask task) {
        return null;
    }
}
