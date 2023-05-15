package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;
import contrib.utils.components.ai.ITransition;

import core.Entity;
import core.utils.components.MissingComponentException;

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
