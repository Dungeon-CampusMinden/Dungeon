package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ecs.entitys.Entity;
import java.util.HashMap;
import mydungeon.ECS;
import org.junit.Before;
import org.junit.Test;
import tools.Point;

public class PositionComponentTest {

    private Point position;
    private Entity entity;

    @Before
    public void setup() {
        ECS.positionComponentMap = new HashMap<>();
        entity = new Entity();
        position = new Point(3, 3);
    }

    @Test
    public void testConstructor() {
        PositionComponent component = new PositionComponent(entity, position);
        assertNotNull(component);
        assertNotNull(ECS.positionComponentMap.get(entity));
    }

    @Test
    public void testSetPosition() {
        PositionComponent component = new PositionComponent(entity, position);
        assertEquals(position, component.getPosition());
        Point newPoint = new Point(3, 4);
        component.setPosition(newPoint);
        assertEquals(newPoint, component.getPosition());
    }
}
