package ecs.components;

import static org.junit.Assert.assertEquals;

import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import tools.Point;

public class PositionComponentTest {

    private final Point position = new Point(3, 3);
    private PositionComponent positionComponent;

    @Before
    public void setup() {
        positionComponent = new PositionComponent(Mockito.mock(Entity.class), position);
    }

    @Test
    public void setPosition() {
        assertEquals(position, positionComponent.getPosition());
        Point newPoint = new Point(3, 4);
        positionComponent.setPosition(newPoint);
        assertEquals(newPoint, positionComponent.getPosition());
    }
}
