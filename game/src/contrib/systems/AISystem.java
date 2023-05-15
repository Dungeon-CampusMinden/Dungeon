package contrib.systems;

import contrib.components.AIComponent;

import core.Entity;
import core.System;

/** Controls the AI */
public class AISystem extends System {

    @Override
    public void accept(Entity entity) {
        if (entity.getComponent(AIComponent.class).isPresent()) addEntity(entity);
        else removeEntity(entity);
    }

    @Override
    public void update() {
        getEntityStream()
                .forEach(
                        entity ->
                                ((AIComponent) entity.getComponent(AIComponent.class).get())
                                        .execute());
    }
}
