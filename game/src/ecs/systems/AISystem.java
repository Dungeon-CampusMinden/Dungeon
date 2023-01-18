package ecs.systems;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import mydungeon.ECS;

public class AISystem extends ECS_System {
    @Override
    public void update() {
        for (Entity entity : ECS.entities) {
            AIComponent aiComponent = (AIComponent) entity.getComponent(AIComponent.name);
            if (aiComponent != null) {
                aiComponent.execute();
            }
        }
    }
}
