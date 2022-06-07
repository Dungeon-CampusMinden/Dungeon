package level;

import static org.junit.Assert.*;

import level.tools.Coordinate;
import org.junit.Before;
import org.junit.Test;
import tools.Point;

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
        Point point = coordinate.toPoint();
        assertEquals((float) coordinate.x, point.x, 0.0f);
        assertEquals((float) coordinate.y, point.y, 0.0f);
    }
}
