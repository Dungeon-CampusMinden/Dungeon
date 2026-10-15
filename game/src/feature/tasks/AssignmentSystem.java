package feature.tasks;

import engine.Entity;
import engine.Game;
import engine.System;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContextKeys;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Checks registered {@link AssignmentComponent}s on the server whenever the placement of their
 * items changes.
 *
 * <p>A changed placement runs the assignment's {@link AssignmentChecker}. When it reports a
 * solution, the placement is submitted to the task component, which invokes its correct callback
 * with the player who last changed a container. Wrong placements are ignored because players may
 * still be sorting.
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
    if (!assignment.placementChanged(placement)) {
      return;
    }
    viewer(assignment.containers().values()).ifPresent(assignment::actor);
    if (assignment.checker().check(assignment.task(), assignment.containers())) {
      taskComponent.submit(placement.orElse(Map.of()), assignment.actor().orElse(entity));
    }
  }

  private static Optional<Entity> viewer(final Collection<Entity> containers) {
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
