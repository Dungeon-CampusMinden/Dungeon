package ecs.systems;

import static org.mockito.Mockito.times;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AISystemTest {

    private AISystem system;
    private Entity entity;
    private final AIComponent aiComponent = Mockito.mock(AIComponent.class);

    @Before
    public void setup() {
        ECS.systems = Mockito.mock(SystemController.class);
        ECS.entities.clear();
        system = new AISystem();
        entity = new Entity();
        entity.addComponent(aiComponent);
    }

    @Test
    public void update() {
        system.update();
        Mockito.verify(aiComponent, times(1)).execute();
    }

    @Test
    public void updateWithoutAIComponent() {
        entity.removeComponent(AIComponent.class);
        system.update();
        Mockito.verify(aiComponent, times(0)).execute();
    }
}
