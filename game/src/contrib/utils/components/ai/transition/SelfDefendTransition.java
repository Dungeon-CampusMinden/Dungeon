package contrib.utils.components.ai.transition;

import contrib.components.HealthComponent;

import core.Entity;
import core.utils.components.MissingComponentException;

import java.util.function.Function;

public class SelfDefendTransition implements Function<Entity, Boolean> {
    @Override
    public Boolean apply(Entity entity) {
        HealthComponent component =
                (HealthComponent)
                        entity.fetch(HealthComponent.class)
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
