package feature.tasks;

import engine.Entity;
import engine.Game;
import engine.System;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContextKeys;
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
 * <p>A changed placement, or a requested {@link AssignmentComponent#recheck()}, runs the
 * assignment's {@link AssignmentChecker}. When it reports a solution, the placement is submitted to
 * the task component, which invokes its correct callback with the player who last changed a
 * container through its inventory dialog. Wrong placements are ignored because players may still be
 * sorting.
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
      viewer(changedContainers(assignment, placement)).ifPresent(assignment::actor);
      assignment.placement(placement);
    }
    if ((assignment.consumeRecheck() || changed) && assignment.checker().check(assignment)) {
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

  private static Optional<Entity> viewer(final Set<Entity> containers) {
    return Game.allPlayers()
        .filter(
            player ->
                player
                    .fetch(UIComponent.class)
                    .flatMap(
                        ui ->
                            ui.dialogContext()
                                .find(DialogContextKeys.SECONDARY_ENTITY, Integer.class))
                    .filter(id -> containers.stream().anyMatch(container -> container.id() == id))
                    .isPresent())
        .findFirst();
  }
}
