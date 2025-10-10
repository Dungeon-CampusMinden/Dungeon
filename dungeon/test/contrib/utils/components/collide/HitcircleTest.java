package contrib.utils.components.collide;

import static org.junit.jupiter.api.Assertions.*;

import core.utils.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link Hitcircle} class. */
public class HitcircleTest {
  private Hitcircle circle;

  @BeforeEach
  void setup() {
    circle = new Hitcircle(5f, 10f, 10f);
    circle.position(new Point(0f, 0f)); // ensure absolutePosition() = offset initially
  }

  @Test
  void testConstructorAndAccessors() {
    assertEquals(5f, circle.radius());
    assertEquals(10f, circle.offset.x());
    assertEquals(10f, circle.offset.y());
  }

  @Test
  void testWidthAndHeightReflectRadius() {
    assertEquals(10f, circle.width());
    assertEquals(10f, circle.height());

    circle.width(20f);
    assertEquals(10f, circle.radius());

    circle.height(14f);
    assertEquals(7f, circle.radius());
  }

  @Test
  void testLeftRightTopBottom() {
    // initial geometry
    assertEquals(5f, circle.left());
    assertEquals(15f, circle.right());
    assertEquals(15f, circle.top());
    assertEquals(5f, circle.bottom());

    // move using setters
    circle.left(0f);
    assertEquals(5f, circle.offset.x(), 0.0001f);

    circle.right(20f);
    assertEquals(15f, circle.offset.x(), 0.0001f);

    circle.top(30f);
    assertEquals(25f, circle.offset.y(), 0.0001f);

    circle.bottom(10f);
    assertEquals(15f, circle.offset.y(), 0.0001f);
  }

  @Test
  void testCloneCreatesIndependentCopy() {
    Hitcircle clone = (Hitcircle) circle.clone();
    assertNotSame(circle, clone);
    assertEquals(circle.radius(), clone.radius());
    assertEquals(circle.offset, clone.offset);

    // modifying clone doesn’t affect original
    clone.radius = 20f;
    assertNotEquals(circle.radius(), clone.radius);
  }

  @Test
  void testCollideWithPoint_inside() {
    Point point = new Point(12f, 12f); // within radius distance 5
    assertTrue(circle.collide(point));
  }

  @Test
  void testCollideWithPoint_outside() {
    Point point = new Point(20f, 20f);
    assertFalse(circle.collide(point));
  }

  @Test
  void testCollideWithLine_intersects() {
    Point from = new Point(0f, 10f);
    Point to = new Point(20f, 10f);
    assertTrue(circle.collide(from, to)); // line passes through center
  }

  @Test
  void testCollideWithLine_miss() {
    Point from = new Point(0f, 0f);
    Point to = new Point(20f, 0f);
    assertFalse(circle.collide(from, to));
  }

  @Test
  void testCollideWithOtherCircle_intersecting() {
    Hitcircle other = new Hitcircle(5f, 15f, 10f);
    assertTrue(circle.collide(other)); // touching/overlapping
  }

  @Test
  void testCollideWithOtherCircle_nonIntersecting() {
    Hitcircle other = new Hitcircle(3f, 50f, 50f);
    assertFalse(circle.collide(other));
  }
}
