package ecs;

import static org.junit.Assert.*;

import ecs.components.Component;
import ecs.entities.Entity;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;

public class EntityTest {

    private Entity entity;

    @Before
    public void setup() {
        ECS.entities.clear();
        entity = new Entity();
    }

    @Test
    public void ctorTest() {
        assertTrue(ECS.entities.contains(entity));
    }

    @Test
    public void addComponent() {
        Component c = new TestComponent(entity);
        assertEquals(c, entity.getComponent(TestComponent.name).get());
    }

    @Test
    public void removeComponent() {
        Component c = new TestComponent(entity);
        assertEquals(c, entity.getComponent(TestComponent.name).get());
        entity.removeComponent(TestComponent.name);
        assertTrue(entity.getComponent(TestComponent.name).isEmpty());
    }

    private class TestComponent extends Component {

        public static String name = "TestComponent";
        /**
         * @param entity associated entity
         */
        public TestComponent(Entity entity) {
            super(entity, name);
        }
    }
}
