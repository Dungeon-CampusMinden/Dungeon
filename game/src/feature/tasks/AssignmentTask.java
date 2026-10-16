package feature.tasks;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A task that sorts items (set A) into containers (set B).
 *
 * <p>Each item belongs into at most one container; a container can take any number of items. An
 * answer maps the identifier of every task item that lies in one of the task containers to that
 * container's identifier. It is correct when every item with a target lies in its target and every
 * item without a target lies in none of the containers.
 *
 * <p>The task only describes the assignment. {@link TaskFactory#buildItemAssignment} connects it to
 * containers in the dungeon, whose items carry their identifiers as {@link AnswerItem}s.
 */
public class AssignmentTask extends Task<Map<String, String>> {

  private final Set<String> containerIds;
  private final Set<String> itemIds;
  private final Map<String, String> targets;

  /**
   * Creates an assignment task.
   *
   * @param taskDescription the instructions shown to players
   * @param containerIds identifiers of all containers
   * @param itemIds identifiers of all items, including those that belong nowhere
   * @param targets the target container identifier for every item that belongs into one
   * @throws IllegalArgumentException if a target references an unknown item or container
   */
  public AssignmentTask(
      String taskDescription,
      Set<String> containerIds,
      Set<String> itemIds,
      Map<String, String> targets) {
    this.taskDescription = Objects.requireNonNull(taskDescription, "taskDescription");
    this.containerIds = Set.copyOf(containerIds);
    this.itemIds = Set.copyOf(itemIds);
    this.targets = Map.copyOf(targets);
    if (!this.itemIds.containsAll(this.targets.keySet())
        || !this.containerIds.containsAll(this.targets.values())) {
      throw new IllegalArgumentException("targets must map task items to task containers");
    }
  }

  /**
   * Checks a placement of task items in task containers.
   *
   * @param answer the container identifier of every task item that lies in a task container
   * @return {@code true} if the placement equals the targets
   */
  @Override
  public boolean isCorrect(Map<String, String> answer) {
    return targets.equals(answer);
  }

  /**
   * Returns the identifiers of all containers.
   *
   * @return the container identifiers
   */
  public Set<String> containerIds() {
    return containerIds;
  }

  /**
   * Returns the identifiers of all items, including those that belong nowhere.
   *
   * @return the item identifiers
   */
  public Set<String> itemIds() {
    return itemIds;
  }
}
