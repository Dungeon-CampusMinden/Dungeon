package engine.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for {@link Rectangle}. */
class RectangleTest {

  @Test
  void twoCornersDefineTheSameRectangleInEitherOrder() {
    Point first = new Point(1, 5);
    Point second = new Point(4, 1);
    Rectangle expected = new Rectangle(3, 4, 1, 1);

    assertEquals(expected, new Rectangle(first, second));
    assertEquals(expected, new Rectangle(second, first));
  }

  @Test
  void containsPointsOnTheEdge() {
    Rectangle room = new Rectangle(4, 3);

    assertTrue(room.contains(new Point(2, 1)));
    assertTrue(room.contains(new Point(0, 0)));
    assertTrue(room.contains(new Point(4, 3)));
    assertFalse(room.contains(new Point(5, 1)));
  }

  @Test
  void intersectsRequiresAnOverlappingRectangle() {
    Rectangle room = new Rectangle(4, 3);

    assertTrue(room.intersects(new Rectangle(2, 2, 3, 1)));
    assertFalse(room.intersects(new Rectangle(2, 2, 4, 1)));
    assertFalse(room.intersects(new Rectangle(2, 2, 1, 3)));
    assertFalse(room.intersects(null));
  }

  @Test
  void expandsAllSidesAndKeepsTheSizeWhenMoved() {
    Rectangle room = new Rectangle(4, 3, 1, 2);

    assertEquals(new Rectangle(6, 5, 0, 1), room.expand(1));
    assertEquals(new Rectangle(4, 3, 3, 1), room.translate(Vector2.of(2, -1)));
  }
}
