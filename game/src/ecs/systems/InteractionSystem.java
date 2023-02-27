package ecs.systems;

import ecs.components.InteractionComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import java.util.Optional;
import mydungeon.ECS;
import tools.Point;

public class InteractionSystem {
    private record ISData(Entity e, PositionComponent pc, InteractionComponent ic, float dist) {}

    public static void interactWithClosestInteractable() {
        PositionComponent heroPosition =
                (PositionComponent)
                        ECS.hero
                                .getComponent(PositionComponent.class)
                                .orElseThrow(() -> MissingPCFromEntity(Hero.class.getName()));
        Optional<ISData> data =
                ECS.entities.stream()
                        .flatMap(
                                x ->
                                        x
                                                .getComponent(InteractionComponent.class)
                                                .map(InteractionComponent.class::cast)
                                                .stream())
                        .map(ic1 -> convertToData(ic1, heroPosition))
                        .filter(x -> x.ic.getRadius() - x.dist > 0)
                        .min(
                                (x, y) ->
                                        Float.compare(
                                                x.ic.getRadius() - x.dist,
                                                y.ic.getRadius() - y.dist));
        data.ifPresent(x -> x.ic.triggerInteraction());
    }

    private static InteractionSystem.ISData convertToData(
            InteractionComponent ic, PositionComponent heroPosition) {
        Entity entity = ic.getEntity();

        PositionComponent pc =
                ((PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> MissingPCFromEntity(entity.getClass().getName())));
        return new ISData(
                entity,
                pc,
                ic,
                Point.calculateDistance(heroPosition.getPosition(), pc.getPosition()));
    }

    private static MissingComponentException MissingPCFromEntity(String entity) {
        return new MissingComponentException(
                "Missing "
                        + PositionComponent.class.getName()
                        + " from "
                        + entity
                        + " in "
                        + InteractionSystem.class.getName());
    }
}
