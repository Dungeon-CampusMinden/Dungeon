package ecs.tools;

import ecs.components.InteractionComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import java.util.Optional;
import level.tools.Coordinate;
import starter.Game;
import tools.Point;

public class InteractionTool {

    private record InteractionData(
            Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}

    public static void interactWithClosestInteractable(Entity entity) {
        PositionComponent heroPosition =
                (PositionComponent)
                        entity
                                .getComponent(PositionComponent.class)
                                .orElseThrow(() -> MissingPCFromEntity(Hero.class.getName()));
        Optional<InteractionData> data =
                Game.entities.stream()
                        .flatMap(
                                x ->
                                        x
                                                .getComponent(InteractionComponent.class)
                                                .map(InteractionComponent.class::cast)
                                                .stream())
                        .map(ic1 -> convertToData(ic1, heroPosition))
                        .filter(InteractionTool::checkReachable)
                        .min((x, y) -> Float.compare(x.dist, y.dist));
        data.ifPresent(x -> x.ic.triggerInteraction());
    }

    private static boolean checkReachable(InteractionData interactionData) {
        if ((interactionData.ic.getRadius() - interactionData.dist) > 0) {
            // check path .... yay
            Point dirvec = interactionData.unitDir;
            for (int i = 1; i < interactionData.dist; i++) {
                if (!Game.currentLevel
                        .getTileAt(
                                new Coordinate(
                                        (int) (dirvec.x * i + interactionData.pc.getPosition().x),
                                        (int) (dirvec.y * i + interactionData.pc.getPosition().x)))
                        .isAccessible()) {
                    return false;
                }
            }
            return true;
        }
        return false;
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
