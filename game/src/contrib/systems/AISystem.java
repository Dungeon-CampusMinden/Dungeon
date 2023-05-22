package contrib.systems;

import contrib.components.AIComponent;

import core.Entity;
import core.System;

import java.util.function.Consumer;

/** Controls the AI */
public class AISystem extends System {

    private static final Consumer<Entity> executeAI =
            entity -> ((AIComponent) entity.getComponent(AIComponent.class).get()).execute();

    public AISystem() {
        super(AIComponent.class);
    }

    @Override
    public void execute() {
        getEntityStream().forEach(executeAI);
    }
}
