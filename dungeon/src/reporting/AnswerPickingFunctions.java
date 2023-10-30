package reporting;

import contrib.components.InventoryComponent;
import contrib.item.Item;

import core.Entity;
import core.utils.components.MissingComponentException;

import task.Element;
import task.QuestItem;
import task.Task;
import task.TaskContent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Contains different Functions to create a Callback-Function for {@link
 * Task#answerPickingFunction(Function)}.
 *
 * <p>The Functions will find the given answers to a task in the game.
 *
 * <p>Note that it is important to choose the correct function. The correct function varies based on
 * the Task-Type and the game scenario.
 */
public class AnswerPickingFunctions {

    /**
     * This Callback will check the first container of the given {@link Task} and will return a
     * Collection that contains each TaskContent in the container.
     *
     * <p>This function assumes that the container is an Entity that has an {@link
     * InventoryComponent}
     *
     * <p>This function assumes that the given Answers are {@link QuestItem}s
     *
     * <p>This function ignores other Items in the container.
     *
     * <p>Use this function as a callback for {@link task.quizquestion.SingleChoice} and {@link
     * task.quizquestion.MultipleChoice} Tasks.
     *
     * @return Function that can be used as a callback for {@link
     *     Task#answerPickingFunction(Function)}
     */
    public static Function<Task, Set<TaskContent>> singleChestPicker() {
        return new Function<Task, Set<TaskContent>>() {
            @Override
            public Set<TaskContent> apply(Task task) {
                TaskContent containerContent =
                        (TaskContent) task.containerStream().findFirst().orElseThrow();
                Entity container = task.find(containerContent);
                InventoryComponent ic =
                        container
                                .fetch(InventoryComponent.class)
                                .orElseThrow(
                                        () ->
                                                MissingComponentException.build(
                                                        container, InventoryComponent.class));
                Item[] answerItems = ic.items(QuestItem.class);
                Set<TaskContent> res = new HashSet<>();
                for (Item i : answerItems) {
                    res.add(((QuestItem) i).taskContentComponent().content());
                }
                return res;
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
