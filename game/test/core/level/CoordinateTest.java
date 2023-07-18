package core.level;

import core.utils.position.Position;

import org.junit.Before;
import org.junit.Test;

public class CoordinateTest {

    private Coordinate coordinate;
    private int x = 3, y = -3;

    @Before
    public void setup() {
        coordinate = new Coordinate(x, y);
    }

    @Test
    public void test_equals() {
        assertEquals(coordinate, new Coordinate(x, y));
        assertNotEquals(coordinate, new Coordinate(y, x));
    }

    @Test
    public void test_toPoint() {
        Position position = coordinate.toPoint();
        assertEquals((float) coordinate.x, position.x, 0.0f);
        assertEquals((float) coordinate.y, position.y, 0.0f);
    }
}
