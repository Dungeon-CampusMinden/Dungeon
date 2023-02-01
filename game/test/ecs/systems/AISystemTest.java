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

public class AISystemTest {

    private AISystem system;
    private AIComponent aiComponent = Mockito.mock(AIComponent.class);

    @Before
    public void setup() {
        ECS.systems = Mockito.mock(SystemController.class);
        ECS.entities.clear();
        system = new AISystem();
        Entity entity = new Entity();
        entity.addComponent(AIComponent.name, aiComponent);
    }

    @Test
    public void update() {
        system.update();
        Mockito.verify(aiComponent, times(1)).execute();
    }
}
