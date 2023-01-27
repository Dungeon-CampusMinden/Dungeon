package ecs.components;

import static org.mockito.Mockito.times;

import ecs.components.ai.AIComponent;
import ecs.entities.Entity;
import ecs.systems.AISystem;
import ecs.systems.SystemController;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AIComponentTest {

    private AISystem system;
    private AIComponent component = Mockito.mock(AIComponent.class);
    Entity entity;

    @Before
    public void setup() {
        ECS.systems = Mockito.mock(SystemController.class);
        system = new AISystem();
        entity = new Entity();
        entity.addComponent(AIComponent.name, component);
    }

    @Test
    public void update() {
        system.update();
        Mockito.verify(component, times(1)).execute();
    }
}
