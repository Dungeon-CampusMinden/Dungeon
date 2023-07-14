package core.components;

import static org.junit.Assert.assertEquals;

import core.Entity;
import core.utils.position.Position;

import org.junit.Before;
import org.junit.Test;

public class PositionComponentTest {

    private final Position position = new Position(3, 3);
    private PositionComponent positionComponent;

    @Before
    public void setup() {
        positionComponent = new PositionComponent(new Entity(), position);
    }

    @Test
    public void setPosition() {
        assertEquals(position, positionComponent.position());
        Position newPosition = new Position(3, 4);
        positionComponent.position(newPosition);
        assertEquals(newPosition, positionComponent.position());
    }
}
