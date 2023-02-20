package ecs.systems;

import ecs.components.Component;
import ecs.components.InteractionComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import java.util.Optional;
import mydungeon.ECS;

public class InteractionSystem {
    public static void interactWithClosestInteractable() {
        InteractionComponent closest = null;
        float smallestDist = Float.MAX_VALUE;
        PositionComponent heroPosition =
                (PositionComponent)
                        ECS.hero
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("Missing component"));

        for (Entity entity : ECS.entities) {
            Optional<Component> optionalComponent = entity.getComponent(InteractionComponent.class);
            if (optionalComponent.isPresent()) {
                InteractionComponent ic = (InteractionComponent) optionalComponent.get();
                float dist =
                        AITools.calculateDistance(
                                heroPosition.getPosition(),
                                ((PositionComponent)
                                                entity.getComponent(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        new MissingComponentException(
                                                                                "Missing component")))
                                        .getPosition());

                if (dist < ic.getRadius()) {
                    if (dist < smallestDist) {
                        closest = ic;
                        smallestDist = dist;
                    }
                }
            }
        }

        if (closest != null) {
            closest.triggerInteraction();
        }
    }
}
