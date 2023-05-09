package content.utils.interaction;

import content.utils.position.Point;
import api.components.InteractionComponent;
import api.components.MissingComponentException;
import api.components.PositionComponent;
import content.components.interaction.ControlPointReachable;
import api.Entity;
import java.util.Optional;

import starter.Game;

public class InteractionTool {

    public static final IReachable SIMPLE_REACHABLE =
            (interactionData -> (interactionData.ic().getRadius() - interactionData.dist()) > 0);

    public static final IReachable CONTROLL_POINTS_REACHABLE = new ControlPointReachable();

    public static void interactWithClosestInteractable(Entity entity) {
        interactWithClosestInteractable(entity, SIMPLE_REACHABLE);
    }

    public static void interactWithClosestInteractable(Entity entity, IReachable iReachable) {
        PositionComponent heroPosition =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(() -> MissingPCFromEntity(Entity.class.getName()));
        Optional<InteractionData> data =
                Game.getEntities().stream()
                        .flatMap(
                                x ->
                                        x
                                                .getComponent(InteractionComponent.class)
                                                .map(InteractionComponent.class::cast)
                                                .stream())
                        .map(ic1 -> convertToData(ic1, heroPosition))
                        .filter(iReachable::checkReachable)
                        .min((x, y) -> Float.compare(x.dist(), y.dist()));
        data.ifPresent(x -> x.ic().triggerInteraction());
    }

    private static InteractionData convertToData(
            InteractionComponent ic, PositionComponent heroPosition) {
        Entity entity = ic.getEntity();

        PositionComponent pc =
                ((PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> MissingPCFromEntity(entity.getClass().getName())));
        return new InteractionData(
                entity,
                pc,
                ic,
                Point.calculateDistance(heroPosition.getPosition(), pc.getPosition()),
                Point.getUnitDirectionalVector(heroPosition.getPosition(), pc.getPosition()));
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
