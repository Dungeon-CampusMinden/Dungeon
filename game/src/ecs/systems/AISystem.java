package ecs.systems;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import starter.Game;

/** Controls the AI */
public class AISystem extends ECS_System {
    @Override
    public void update() {
        for (Entity entity : Game.entities) {
            entity.getComponent(AIComponent.class)
                    .ifPresent(aiComponent -> ((AIComponent) aiComponent).execute());
        }
    }
}
