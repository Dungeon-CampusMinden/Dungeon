package ecs;

import static org.junit.Assert.*;

import ecs.components.Component;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EntityTest {

    private Entity entity;
    private final Component testComponent = Mockito.mock(Component.class);
    private final String componentName = "TestComponent";

    @Before
    public void setup() {
        ECS.entities.clear();
        entity = new Entity();
        entity.addComponent(componentName, testComponent);
    }

    @Test
    public void cTor() {
        assertTrue(ECS.entities.contains(entity));
    }

    @Test
    public void addComponent() {
        assertEquals(testComponent, entity.getComponent(componentName).get());
    }

    @Test
    public void addAlreadyExistingComponent() {
        Component newComponent = Mockito.mock(Component.class);
        entity.addComponent(componentName, newComponent);
        assertEquals(newComponent, entity.getComponent(componentName).get());
    }

    @Test
    public void removeComponent() {
        entity.removeComponent(componentName);
        assertTrue(entity.getComponent(componentName).isEmpty());
    }
}
