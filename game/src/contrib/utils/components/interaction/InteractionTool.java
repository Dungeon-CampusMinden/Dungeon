package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.Optional;
import java.util.function.Function;

public class InteractionTool {

    public static final Function<InteractionData, Boolean> SIMPLE_REACHABLE =
            (interactionData -> (interactionData.ic().getRadius() - interactionData.dist()) > 0);

    public static final Function<InteractionData, Boolean> CONTROLL_POINTS_REACHABLE =
            new ControlPointReachable();

    public static void interactWithClosestInteractable(Entity entity) {
        interactWithClosestInteractable(entity, SIMPLE_REACHABLE);
    }

    public static void interactWithClosestInteractable(
            Entity entity, Function<InteractionData, Boolean> iReachable) {
        PositionComponent heroPosition =
                (PositionComponent)
                        entity.fetch(PositionComponent.class)
                                .orElseThrow(() -> MissingPCFromEntity(Entity.class.getName()));
        Optional<InteractionData> data =
                Game.entityStream()
                        .flatMap(
                                x ->
                                        x
                                                .fetch(InteractionComponent.class)
                                                .map(InteractionComponent.class::cast)
                                                .stream())
                        .map(ic1 -> convertToData(ic1, heroPosition))
                        .filter(iReachable::apply)
                        .min((x, y) -> Float.compare(x.dist(), y.dist()));
        data.ifPresent(x -> x.ic().triggerInteraction());
    }

    private static InteractionData convertToData(
            InteractionComponent ic, PositionComponent heroPosition) {
        Entity entity = ic.getEntity();

        PositionComponent pc =
                ((PositionComponent)
                        entity.fetch(PositionComponent.class)
                                .orElseThrow(
                                        () -> MissingPCFromEntity(entity.getClass().getName())));
        return new InteractionData(
                entity,
                pc,
                ic,
                Point.calculateDistance(heroPosition.position(), pc.position()),
                Point.getUnitDirectionalVector(heroPosition.position(), pc.position()));
    }

    private static MissingComponentException MissingPCFromEntity(String entity) {
        return new MissingComponentException(
                "Missing "
                        + PositionComponent.class.getName()
                        + " from "
                        + entity
                        + " in "
                        + InteractionTool.class.getName());
    }
}
