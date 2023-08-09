package core.components;

import static org.junit.Assert.assertEquals;

import core.utils.Point;

import org.junit.Before;
import org.junit.Test;

public class PositionComponentTest {

    private final Point position = new Point(3, 3);
    private PositionComponent positionComponent;

    @Before
    public void setup() {
        positionComponent = new PositionComponent(position);
    }

    @Test
    public void setPosition() {
        assertEquals(position, positionComponent.position());
        Point newPoint = new Point(3, 4);
        positionComponent.position(newPoint);
        assertEquals(newPoint, positionComponent.position());
    }
}
