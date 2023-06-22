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
}
