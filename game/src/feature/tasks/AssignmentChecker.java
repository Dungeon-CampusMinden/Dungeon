package feature.tasks;

import engine.Entity;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Decides whether the containers of an {@link AssignmentTask} hold a correct assignment.
 *
 * <p>The {@link AssignmentSystem} calls the checker of a registered assignment whenever the
 * placement of its items changes. A replacement checker can, for example, also require a lever to
 * be pulled.
 */
@FunctionalInterface
public interface AssignmentChecker {

  /** Checks the current container contents with {@link AssignmentTask#isCorrect}. */
  AssignmentChecker CONTENTS =
      assignment ->
          placement(assignment.task(), assignment.containers())
              .map(assignment.task()::isCorrect)
              .orElse(false);

  /**
   * Checks the assignment.
   *
   * @param assignment the registered task, its containers, and the last acting player
   * @return {@code true} if the assignment is solved
   */
  boolean check(AssignmentComponent assignment);

  /**
   * Collects which task container holds each task item.
   *
   * <p>Items that are not {@link AnswerItem}s of the task are ignored.
   *
   * @param task the assignment task
   * @param containers the task containers by container identifier
   * @return the container identifier of every task item in a container, or empty if an item
   *     identifier occurs more than once
   */
  static Optional<Map<String, String>> placement(
      final AssignmentTask task, final Map<String, Entity> containers) {
    Map<String, String> placement = new HashMap<>();
    for (Map.Entry<String, Entity> container : containers.entrySet()) {
      Item[] items =
          container
              .getValue()
              .fetch(InventoryComponent.class)
              .map(InventoryComponent::items)
              .orElse(new Item[0]);
      for (Item item : items) {
        if (item instanceof AnswerItem answer
            && task.itemIds().contains(answer.answerId())
            && placement.put(answer.answerId(), container.getKey()) != null) {
          return Optional.empty();
        }
      }
    }
    return Optional.of(Map.copyOf(placement));
  }
}
