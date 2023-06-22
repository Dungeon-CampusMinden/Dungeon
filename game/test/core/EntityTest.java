package core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EntityTest {

    private Entity entity;
    private final Component testComponent = Mockito.mock(Component.class);

    @Before
    public void setup() {
        entity = new Entity();
        entity.addComponent(testComponent);
    }

    @Test
    public void addComponent() {
        assertEquals(testComponent, entity.fetch(testComponent.getClass()).get());
    }

    @Test
    public void addAlreadyExistingComponent() {
        Component newComponent = Mockito.mock(Component.class);
        entity.addComponent(newComponent);
        assertEquals(newComponent, entity.fetch(testComponent.getClass()).get());
    }

    @Test
    public void removeComponent() {
        entity.removeComponent(testComponent.getClass());
        assertTrue(entity.fetch(testComponent.getClass()).isEmpty());
    }

    @Test
    public void compareTo() {
        Entity entity1 = new Entity();
        Entity entity2 = new Entity();

        assertEquals(entity1.id(), entity1.id());
        assertEquals(0, entity1.compareTo(entity1));
        assertTrue(entity1.compareTo(entity2) < 0);
        assertTrue(entity2.compareTo(entity1) > 0);
    }

    @After
    public void tearDown() {
        Game.removeAllEntities();

    }
}
