package api.systems;

import api.System;
import api.components.AIComponent;
import api.Entity;
import starter.Game;

/** Controls the AI */
public class AISystem extends System {

    private record AISData(Entity e, AIComponent aic) {}

    @Override
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(AIComponent.class).stream())
                .map(aic -> buildDataObject((AIComponent) aic))
                .forEach(aic -> aic.aic.execute());
    }

    private AISystem.AISData buildDataObject(AIComponent aic) {
        Entity e = aic.getEntity();

        return new AISData(e, aic);
    }
}
