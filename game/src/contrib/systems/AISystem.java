package contrib.systems;

import contrib.components.AIComponent;

import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

import java.util.function.Consumer;

/** Controls the AI. */
public final class AISystem extends System {

    private static final Consumer<Entity> executeAI =
            entity ->
                    entity.fetch(AIComponent.class)
                            .orElseThrow(
                                    () ->
                                            MissingComponentException.build(
                                                    entity, AIComponent.class))
                            .execute(entity);

    public AISystem() {
        super(AIComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(executeAI);
    }
}
