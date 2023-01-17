package ecs.components;

import static org.junit.Assert.assertEquals;

import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import tools.Point;

public class PositionComponentTest {

    private Point position;
    private Entity entity;

    @Before
    public void setup() {
        entity = new Entity();
        position = new Point(3, 3);
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
