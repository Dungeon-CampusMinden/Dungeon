package core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EntityTest {

    private Entity entity;
    private final Component testComponent = Mockito.mock(Component.class);

    @Before
    public void setup() {
        // Cleanup
        Game.removeEntity(entity);
        entity = new Entity();
        entity.addComponent(testComponent);
    }

    @Test
    public void cTor() {
        assertTrue(Game.getEntitiesStream().anyMatch(e -> e == entity));
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
