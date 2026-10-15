package feature.tasks;

import engine.Component;
import engine.Entity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registers an {@link AssignmentTask} and its containers with the {@link AssignmentSystem}.
 *
 * <p>Created by {@link TaskFactory#buildItemAssignment}.
 */
public final class AssignmentComponent implements Component {

  private final TaskComponent<Map<String, String>> taskComponent;
  private final AssignmentTask task;
  private final Map<String, Entity> containers;
  private final AssignmentChecker checker;
  private Optional<Map<String, String>> checkedPlacement;
  private Entity actor;

  /**
   * Creates the registration.
   *
   * @param taskComponent the component that owns the task and receives the solved placement
   * @param task the assignment task
   * @param containers the task containers by container identifier
   * @param checker the check run after each placement change
   */
  public AssignmentComponent(
      final TaskComponent<Map<String, String>> taskComponent,
      final AssignmentTask task,
      final Map<String, Entity> containers,
      final AssignmentChecker checker) {
    this.taskComponent = Objects.requireNonNull(taskComponent, "taskComponent");
    this.task = Objects.requireNonNull(task, "task");
    this.containers = Map.copyOf(containers);
    this.checker = Objects.requireNonNull(checker, "checker");
    if (!this.containers.keySet().equals(task.containerIds())) {
      throw new IllegalArgumentException("containers must match the task container identifiers");
    }
  }

  /**
   * Returns the component that owns the task.
   *
   * @return the task component
   */
  public TaskComponent<Map<String, String>> taskComponent() {
    return taskComponent;
  }

  /**
   * Returns the assignment task.
   *
   * @return the task
   */
  public AssignmentTask task() {
    return task;
  }

  /**
   * Returns the task containers by container identifier.
   *
   * @return the containers
   */
  public Map<String, Entity> containers() {
    return containers;
  }

  /**
   * Returns the check run after each placement change.
   *
   * @return the checker
   */
  public AssignmentChecker checker() {
    return checker;
  }

  /**
   * Returns the player who last changed the placement through an open container.
   *
   * @return the last acting player, if known
   */
  public Optional<Entity> actor() {
    return Optional.ofNullable(actor);
  }

  /** Runs the check again on the next system tick even if the placement did not change. */
  public void recheck() {
    checkedPlacement = null;
  }

  boolean placementChanged(final Optional<Map<String, String>> placement) {
    if (placement.equals(checkedPlacement)) {
      return false;
    }
    checkedPlacement = placement;
    return true;
  }

  void actor(final Entity actor) {
    this.actor = actor;
  }
}
