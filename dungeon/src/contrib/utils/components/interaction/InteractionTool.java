package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.Optional;
import java.util.function.Function;

/** This class provides utility methods for interacting with interactable entities in the game. */
public final class InteractionTool {

  private static final Function<InteractionData, Boolean> SIMPLE_REACHABLE =
      (interactionData -> (interactionData.ic().radius() - interactionData.dist()) > 0);

  /**
   * Interacts with the closest interactable entity.
   *
   * @param entity Entity The entity that is interacting.
   */
  public static void interactWithClosestInteractable(final Entity entity) {
    interactWithClosestInteractable(entity, SIMPLE_REACHABLE);
  }

  /**
   * Interacts with the closest interactable entity.
   *
   * @param who The entity that is interacting.
   * @param iReachable The function that determines if the entity is reachable.
   */
  public static void interactWithClosestInteractable(
      final Entity who, final Function<InteractionData, Boolean> iReachable) {
    PositionComponent heroPosition =
        who.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(who, PositionComponent.class));
    Optional<InteractionData> data =
        Game.entityStream()
            .filter(x -> x.isPresent(InteractionComponent.class))
            .map(x -> convertToData(x, heroPosition))
            .filter(iReachable::apply)
            .min((x, y) -> Float.compare(x.dist(), y.dist()));
    data.ifPresent(x -> x.ic().triggerInteraction(x.e(), who));
  }

  private static InteractionData convertToData(
      final Entity entity, final PositionComponent heroPosition) {

    InteractionComponent ic =
        entity
            .fetch(InteractionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InteractionComponent.class));
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return new InteractionData(
        entity,
        pc,
        ic,
        Point.calculateDistance(heroPosition.position(), pc.position()),
        Point.unitDirectionalVector(heroPosition.position(), pc.position()));
  }

  private record InteractionData(
      Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
}
