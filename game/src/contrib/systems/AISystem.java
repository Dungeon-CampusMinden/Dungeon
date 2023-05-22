package contrib.systems;

import contrib.components.AIComponent;

import core.Entity;
import core.System;

/** Controls the AI */
public class AISystem extends System {

    @Override
    protected boolean accept(Entity entity) {
        return entity.getComponent(AIComponent.class).isPresent();
    }

    @Override
    public void execute() {
        getEntityStream()
                .forEach(
                        entity ->
                                ((AIComponent) entity.getComponent(AIComponent.class).get())
                                        .execute());
    }
}
