package content.components.ai.transition;

import content.utils.ai.ITransition;
import api.components.HealthComponent;
import api.components.MissingComponentException;
import api.Entity;

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
