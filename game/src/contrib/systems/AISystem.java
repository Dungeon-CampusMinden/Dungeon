package contrib.systems;

import contrib.components.AIComponent;

import core.Entity;
import core.System;

/** Controls the AI */
public class AISystem extends System {

    @Override
    protected boolean accept(Entity entity) {
        if (entity.getComponent(AIComponent.class).isPresent()) return true;
        else return false;
    }

    @Override
    public void systemUpdate() {
        getEntityStream()
                .forEach(
                        entity ->
                                ((AIComponent) entity.getComponent(AIComponent.class).get())
                                        .execute());
    }
}
