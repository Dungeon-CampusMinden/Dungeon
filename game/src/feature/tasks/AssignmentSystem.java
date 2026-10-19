package feature.tasks;

import engine.Entity;
import engine.System;
import feature.components.InventoryComponent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Checks registered {@link AssignmentComponent}s on the server whenever the placement of their
 * items changes.
 *
 * <p>A changed placement runs the assignment's {@link AssignmentChecker}. When it reports a
 * solution, the placement goes to the assignment's submission together with the player who last
 * moved an item into or out of one of its containers. A correct placement is checked and submitted
 * again on every tick until the submission accepts it, e.g. once a required lever is pulled or the
 * riddle becomes active. Wrong placements are ignored because players may still be sorting.
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
    if (assignment.solved()) {
      return;
    }
    Optional<Map<String, String>> placement =
        AssignmentChecker.placement(assignment.task(), assignment.containers());
    boolean changed = !placement.equals(assignment.placement());
    if (changed) {
      mover(assignment.containers().values()).ifPresent(assignment::actor);
      assignment.placement(placement);
    }
    boolean correct = placement.map(assignment.task()::isCorrect).orElse(false);
    if ((changed || correct) && assignment.checker().check(assignment)) {
      assignment.submit(placement.orElse(Map.of()), assignment.actor().orElse(entity));
    }
  }

  private static Optional<Entity> mover(final Collection<Entity> containers) {
    return containers.stream()
        .flatMap(container -> container.fetch(InventoryComponent.class).stream())
        .flatMap(inventory -> inventory.lastMove().stream())
        .max(Comparator.comparingLong(InventoryComponent.Move::nanoTime))
        .map(InventoryComponent.Move::player);
  }
}
