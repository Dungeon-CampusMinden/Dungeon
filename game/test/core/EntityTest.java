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
        Game.getEntities().forEach(e -> Game.removeEntity(e));
        Game.getDelayedEntitySet().update();
        entity = new Entity();
        entity.addComponent(testComponent);
    }

    @Test
    public void cTor() {
        Game.getDelayedEntitySet().update();
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
