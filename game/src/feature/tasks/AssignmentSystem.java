package feature.tasks;

import engine.Entity;
import engine.System;
import feature.components.InventoryComponent;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks registered {@link AssignmentComponent}s on the server whenever the placement of their
 * items changes.
 *
 * <p>A changed placement runs the assignment's {@link AssignmentChecker}; a correct placement runs
 * it again on every tick until it reports a solution, e.g. once a required lever is pulled. When it
 * reports a solution, the placement is submitted to the task component, which invokes its correct
 * callback with the player who last moved an item into or out of a changed container. Wrong
 * placements are ignored because players may still be sorting.
 */
public final class AssignmentSystem extends System {

  /** Creates the system. */
  public AssignmentSystem() {
    super(AssignmentComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(AssignmentSystem::check);
  }

  private static void check(final Entity entity) {
    AssignmentComponent assignment = entity.fetch(AssignmentComponent.class).orElseThrow();
    TaskComponent<Map<String, String>> taskComponent = assignment.taskComponent();
    if (taskComponent.isSolved()) {
      return;
    }
    Optional<Map<String, String>> placement =
        AssignmentChecker.placement(assignment.task(), assignment.containers());
    boolean changed = !placement.equals(assignment.placement());
    if (changed) {
      mover(changedContainers(assignment, placement)).ifPresent(assignment::actor);
      assignment.placement(placement);
    }
    boolean correct = placement.map(assignment.task()::isCorrect).orElse(false);
    if ((changed || correct) && assignment.checker().check(assignment)) {
      taskComponent.submit(placement.orElse(Map.of()), assignment.actor().orElse(entity));
    }
  }

  /**
   * Returns the containers whose task items differ between the last and the new placement.
   *
   * @param assignment the assignment holding the last placement
   * @param placement the new placement
   * @return the changed containers, or all containers when a placement is unknown
   */
  private static Set<Entity> changedContainers(
      final AssignmentComponent assignment, final Optional<Map<String, String>> placement) {
    Optional<Map<String, String>> previous = assignment.placement();
    if (previous.isEmpty() || placement.isEmpty()) {
      return Set.copyOf(assignment.containers().values());
    }
    Map<String, String> before = previous.orElseThrow();
    Map<String, String> after = placement.orElseThrow();
    Set<String> containerIds = new HashSet<>();
    assignment.task().itemIds().stream()
        .filter(item -> !Objects.equals(before.get(item), after.get(item)))
        .forEach(
            item -> {
              Optional.ofNullable(before.get(item)).ifPresent(containerIds::add);
              Optional.ofNullable(after.get(item)).ifPresent(containerIds::add);
            });
    return containerIds.stream().map(assignment.containers()::get).collect(Collectors.toSet());
  }

  private static Optional<Entity> mover(final Set<Entity> containers) {
    return containers.stream()
        .flatMap(container -> container.fetch(InventoryComponent.class).stream())
        .flatMap(inventory -> inventory.lastMover().stream())
        .findFirst();
  }
}
