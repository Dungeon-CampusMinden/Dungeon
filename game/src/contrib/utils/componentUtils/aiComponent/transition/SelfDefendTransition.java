package contrib.utils.componentUtils.aiComponent.transition;

import core.Entity;
import core.utils.componentUtils.MissingComponentException;
import contrib.component.HealthComponent;
import contrib.utils.componentUtils.aiComponent.ITransition;

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
