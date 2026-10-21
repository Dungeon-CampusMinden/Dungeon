package feature.tasks;

import engine.Component;
import engine.Entity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Registers an {@link AssignmentTask} and its containers with the {@link AssignmentSystem}.
 *
 * <p>Created by {@link TaskFactory#buildItemAssignment} or {@link TaskFactory#itemAssignment}.
 */
public final class AssignmentComponent implements Component {

  private final AssignmentTask task;
  private final Map<String, Entity> containers;
  private final AssignmentChecker checker;
  private final BiPredicate<Map<String, String>, Entity> submission;
  private boolean solved;
  private Optional<Map<String, String>> placement = Optional.empty();
  private Entity actor;

  /**
   * Creates the registration.
   *
   * @param task the assignment task
   * @param containers the task containers by container identifier
   * @param checker the check run after each placement change
   * @param submission receives each placement the checker accepts and the acting player; returns
   *     whether the assignment is solved
   */
  public AssignmentComponent(
      final AssignmentTask task,
      final Map<String, Entity> containers,
      final AssignmentChecker checker,
      final BiPredicate<Map<String, String>, Entity> submission) {
    this.task = Objects.requireNonNull(task, "task");
    this.containers = Map.copyOf(containers);
    this.checker = Objects.requireNonNull(checker, "checker");
    this.submission = Objects.requireNonNull(submission, "submission");
    if (!this.containers.keySet().equals(task.containerIds())) {
      throw new IllegalArgumentException("containers must match the task container identifiers");
    }
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
   * Returns whether a submitted placement solved the assignment.
   *
   * @return {@code true} once solved
   */
  public boolean solved() {
    return solved;
  }

  /**
   * Returns the player who last moved an item into or out of one of its containers.
   *
   * @return the last acting player, if known
   */
  public Optional<Entity> actor() {
    return Optional.ofNullable(actor);
  }

  boolean submit(final Map<String, String> placement, final Entity player) {
    solved = submission.test(placement, player);
    return solved;
  }

  Optional<Map<String, String>> placement() {
    return placement;
  }

  void placement(final Optional<Map<String, String>> placement) {
    this.placement = placement;
  }

  void actor(final Entity actor) {
    this.actor = actor;
  }
}
