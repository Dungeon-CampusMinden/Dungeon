package ecs.systems;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import starter.Game;

/** Controls the AI */
public class AISystem extends ECS_System {

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
