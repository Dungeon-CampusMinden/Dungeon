package ecs.components.ai.transition;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;

public class SelfDefendTransition implements ITransition {
    @Override
    public boolean isInFightMode(Entity entity) {
        HealthComponent component =
                (HealthComponent)
                        entity.getComponent(HealthComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "Missing "
                                                                + HealthComponent.class.getName()
                                                                + " which is required for the "
                                                                + SelfDefendTransition.class
                                                                        .getName()));
        return component.getCurrentHealthpoints() < component.getMaximalHealthpoints();
    }
}
