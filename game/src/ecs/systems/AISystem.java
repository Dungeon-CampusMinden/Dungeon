package ecs.systems;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import mydungeon.ECS;

/** Controls the AI */
public class AISystem extends ECS_System {
    @Override
    public void update() {
        for (Entity entity : ECS.entities) {
            entity.getComponent(AIComponent.class)
                    .ifPresent(aiComponent -> ((AIComponent) aiComponent).execute());
        }
    }
}
