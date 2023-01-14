package ecs;

import static org.junit.Assert.*;

import ecs.components.Component;
import ecs.entities.Entity;
import java.util.HashSet;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;

public class EntityTest {

    private Entity entity;

    @Before
    public void setup() {
        ECS.entities = new HashSet<>();
        entity = new Entity();
    }

    @Test
    public void ctorTest() {
        assertTrue(ECS.entities.contains(entity));
    }

    @Test
    public void addComponent() {
        Component c = new TestComponent(entity);
        entity.addComponent(TestComponent.name, c);
        assertEquals(c, entity.getComponent(TestComponent.name));
    }

    @Test
    public void removeComponent() {
        Component c = new TestComponent(entity);
        entity.addComponent(TestComponent.name, c);
        assertEquals(c, entity.getComponent(TestComponent.name));
        entity.removeComponent(TestComponent.name);
        assertNull(entity.getComponent(TestComponent.name));
    }

    private class TestComponent extends Component {

        public static String name = "TestComponent";
        /**
         * @param entity associated entity
         */
        public TestComponent(Entity entity) {
            super(entity);
        }
    }
}
