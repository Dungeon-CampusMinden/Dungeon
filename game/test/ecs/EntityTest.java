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

    @Before
    public void setup() {
        ECS.entities.clear();
        entity = new Entity();
        entity.addComponent(testComponent.getClass(), testComponent);
    }

    @Test
    public void cTor() {
        assertTrue(ECS.entities.contains(entity));
    }

    @Test
    public void addComponent() {
        assertEquals(testComponent, entity.getComponent(testComponent.getClass()).get());
    }

    @Test
    public void addAlreadyExistingComponent() {
        Component newComponent = Mockito.mock(Component.class);
        entity.addComponent(testComponent.getClass(), newComponent);
        assertEquals(newComponent, entity.getComponent(testComponent.getClass()).get());
    }

    @Test
    public void removeComponent() {
        entity.removeComponent(testComponent.getClass());
        assertTrue(entity.getComponent(testComponent.getClass()).isEmpty());
    }
}
