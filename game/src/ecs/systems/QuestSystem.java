package ecs.systems;

import ecs.components.quests.QuestComponent;
import starter.Game;

public class QuestSystem extends ECS_System {

    @Override
    public void update() {
        Game.getEntities().stream()
                // Considers only entities that have QuestComponents
                .flatMap(e -> e.getComponent(QuestComponent.class).stream())
                // Convert from Component to QuestComponent
                .map(e -> (QuestComponent) e)
                // Update every QuestComponent
                .forEach(q -> q.update());
    }

}
