package reporting;

import task.Element;
import task.Task;
import task.TaskContent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnswerPickingFunctions {

    public static Set<TaskContent> singleChestPicker(Task task) {
        // todo
        return new HashSet<>();
    }

    public static Set<TaskContent> multipleChestPicker(Task task) {
        // todo
        Map<Element, Set<Element>> givenSolution = new HashMap<>();
        return Set.of(new Element<>(task, givenSolution));
    }
}
