package task.reporting;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import task.Task;
import task.TaskContent;
import task.game.content.QuestItem;
import task.tasktype.AssignTask;
import task.tasktype.Element;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

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
   * <p>This function assumes that the container is an Entity that has an {@link InventoryComponent}
   *
   * <p>This function assumes that the given answers are {@link QuestItem}s
   *
   * <p>This function ignores other items in the container. Will also ignore QuestItems from other
   * tasks.
   *
   * <p>Use this function as a callback for {@link SingleChoice} and {@link MultipleChoice} Tasks.
   *
   * @return Function that can be used as a callback for {@link
   *     Task#answerPickingFunction(Function)}
   */
  public static Function<Task, Set<TaskContent>> singleChestPicker() {
    return task -> {
      TaskContent containerContent = (TaskContent) task.containerStream().findFirst().orElseThrow();

      Entity container =
          task.find((TaskContent) containerContent)
              .orElseThrow(
                  () -> new NullPointerException("The container does not exist in the game."));
      InventoryComponent ic =
          container
              .fetch(InventoryComponent.class)
              .orElseThrow(
                  () -> MissingComponentException.build(container, InventoryComponent.class));
      Set<Item> answerItems = ic.items(QuestItem.class);
      Set<TaskContent> res = new HashSet<>();
      for (Item i : answerItems) {
        TaskContent content = ((QuestItem) i).taskContentComponent().content();
        if (content.task() == task) res.add(content);
      }
      return res;
    };
  }

  /**
   * This Callback will check all containers of the given {@link Task} and will return a single
   * wrapper {@link Element} that stores a {@link Map TaskContent, Set TaskContent}.
   *
   * <p>Use the wrapper Element's content to get the answer map.
   *
   * <p>This function assumes that the containers are Entities with {@link InventoryComponent}s.
   *
   * <p>This function assumes that the given answers are {@link QuestItem}s
   *
   * <p>This function ignores other items in the container. Will also ignore QuestItems from other
   * tasks.
   *
   * <p>Use this function as a callback for {@link AssignTask}s.
   *
   * @return Function that can be used as a callback for {@link
   *     Task#answerPickingFunction(Function)}
   */
  public static Function<Task, Set<TaskContent>> multipleChestPicker() {
    return task -> {
      Map<TaskContent, Set<TaskContent>> answerMap = new HashMap<>();
      Element wrapperElement = new Element(task, answerMap);

      task.containerStream()
          .forEach(
              containerContent -> {
                Entity container =
                    task.find((TaskContent) containerContent)
                        .orElseThrow(
                            () ->
                                new NullPointerException(
                                    "The container does not exist in the game."));
                InventoryComponent ic =
                    container
                        .fetch(InventoryComponent.class)
                        .orElseThrow(
                            () ->
                                MissingComponentException.build(
                                    container, InventoryComponent.class));
                Set<Item> answerItems = ic.items(QuestItem.class);
                Set<TaskContent> res = new HashSet<>();
                for (Item i : answerItems) {
                  TaskContent content = ((QuestItem) i).taskContentComponent().content();
                  if (content.task() == task) res.add(content);
                }
                answerMap.put((TaskContent) containerContent, res);
              });
      return Set.of(wrapperElement);
    };
  }

  /**
   * This Callback will check the heroes inventory and will return a Collection that contains each
   * TaskContent if the given task in the container.
   *
   * <p>This function assumes that the given answers are {@link QuestItem}s
   *
   * <p>This function ignores other items in the container.
   *
   * <p>Use this function as a callback for {@link SingleChoice} and {@link MultipleChoice} Tasks.
   *
   * @return Function that can be used as a callback for {@link
   *     Task#answerPickingFunction(Function)}
   */
  public static Function<Task, Set<TaskContent>> heroInventoryPicker() {
    return task -> {
      Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
      InventoryComponent ic =
          hero.fetch(InventoryComponent.class)
              .orElseThrow(() -> MissingComponentException.build(hero, InventoryComponent.class));
      Set<Item> answerItems = ic.items(QuestItem.class);
      Set<TaskContent> res = new HashSet<>();
      for (Item i : answerItems) {
        TaskContent content = ((QuestItem) i).taskContentComponent().content();
        if (content.task() == task) res.add(content);
      }
      return res;
    };
  }
}
