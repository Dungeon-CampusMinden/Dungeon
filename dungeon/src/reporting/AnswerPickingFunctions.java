package reporting;

import task.Element;
import task.Task;
import task.TaskContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AnswerPickingFunctions {

    public static Function<Task, Set<TaskContent>> singleChestPicker() {
        // todo
        return new Function<Task, Set<TaskContent>>() {
            @Override
            public Set<TaskContent> apply(Task task) {
                return null;
            }
        };
    }

    public static Function<Task, Set<TaskContent>> multipleChestPicker() {
        return new Function<Task, Set<TaskContent>>() {
            @Override
            public Set<TaskContent> apply(Task task) {
                Map<Element, Set<Element>> givenSolution = new HashMap<>();
                // todo
                return Set.of(new Element<>(task, givenSolution));
            }
        };
    }
}
