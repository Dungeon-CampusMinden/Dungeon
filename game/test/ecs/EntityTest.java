package ecs;

import static org.junit.Assert.*;

import ecs.components.Component;
import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class EntityTest {

    private Entity entity;
    private final Component testComponent = Mockito.mock(Component.class);

    @Before
    public void setup() {
        Game.getEntities().clear();
        Game.getEntitiesToAdd().clear();
        Game.getEntitiesToRemove().clear();
        entity = new Entity();
        entity.addComponent(testComponent);
    }

    @Test
    public void cTor() {
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        assertTrue(Game.getEntities().contains(entity));
    }

    @Test
    public void addComponent() {
        assertEquals(testComponent, entity.getComponent(testComponent.getClass()).get());
    }

    @Test
    public void addAlreadyExistingComponent() {
        Component newComponent = Mockito.mock(Component.class);
        entity.addComponent(newComponent);
        assertEquals(newComponent, entity.getComponent(testComponent.getClass()).get());
    }

    @Test
    public void removeComponent() {
        entity.removeComponent(testComponent.getClass());
        assertTrue(entity.getComponent(testComponent.getClass()).isEmpty());
    }
}
