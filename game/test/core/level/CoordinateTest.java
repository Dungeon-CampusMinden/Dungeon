package core.level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import core.level.utils.Coordinate;
import core.utils.Point;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link Coordinate} class. */
public class CoordinateTest {

  private Coordinate coordinate;
  private final int x = 3;
  private final int y = -3;

  /** WTF? . */
  @Before
  public void setup() {
    coordinate = new Coordinate(x, y);
  }

  /** WTF? . */
  @Test
  public void test_equals() {
    assertEquals(coordinate, new Coordinate(x, y));
    assertNotEquals(coordinate, new Coordinate(y, x));
  }

  /** WTF? . */
  @Test
  public void test_toPoint() {
    Point point = coordinate.toPoint();
    assertEquals((float) coordinate.x, point.x, 0.0f);
    assertEquals((float) coordinate.y, point.y, 0.0f);
  }
}
