package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ecs.entitys.Entity;
import java.util.HashMap;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;

public class VelocityComponentTest {

    private Entity entity;

    @Before
    public void setup() {
        entity = new Entity();
        ECS.velocityComponentMap = new HashMap<>();
    }

    @Test
    public void testConstructor() {
        VelocityComponent component = new VelocityComponent(entity, 1.0f, 2.0f, 3.0f, 4.0f);
        assertNotNull(component);
        assertNotNull(ECS.velocityComponentMap.get(entity));
    }

    @Test
    public void testSetX() {
        VelocityComponent component = new VelocityComponent(entity, 1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(1.0f, component.getX(), 0.001);
        component.setX(5.0f);
        assertEquals(5.0f, component.getX(), 0.001);
    }

    @Test
    public void testSetY() {
        VelocityComponent component = new VelocityComponent(entity, 1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(2.0f, component.getY(), 0.001);
        component.setY(6.0f);
        assertEquals(6.0f, component.getY(), 0.001);
    }

    @Test
    public void testSetXSpeed() {
        VelocityComponent component = new VelocityComponent(entity, 1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(3.0f, component.getxSpeed(), 0.001);
        component.setxSpeed(6.0f);
        assertEquals(6.0f, component.getxSpeed(), 0.001);
    }

    @Test
    public void testSetYSpeed() {
        VelocityComponent component = new VelocityComponent(entity, 1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(4.0f, component.getySpeed(), 0.001);
        component.setySpeed(6.0f);
        assertEquals(6.0f, component.getySpeed(), 0.001);
    }
}
