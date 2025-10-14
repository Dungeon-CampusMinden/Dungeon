package contrib.utils.components.collide;

import static org.junit.jupiter.api.Assertions.*;

import core.utils.Point;
import core.utils.Vector2;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link Hitbox} class. */
public class HitboxTest {

  @Test
  void testConstructorSetsValuesCorrectly() {
    Hitbox hitbox = new Hitbox(10, 5, 2, 3);
    assertEquals(10, hitbox.width());
    assertEquals(5, hitbox.height());
    assertEquals(2, hitbox.offset().x());
    assertEquals(3, hitbox.offset().y());
  }

  @Test
  void testLeftRightTopBottom() {
    Hitbox hitbox = new Hitbox(10, 5, 2, 3);

    // Check getters
    assertEquals(2, hitbox.left());
    assertEquals(12, hitbox.right());
    assertEquals(3, hitbox.bottom());
    assertEquals(8, hitbox.top());

    // Modify edges
    hitbox.left(4);
    assertEquals(4, hitbox.left());
    assertEquals(14, hitbox.right());

    hitbox.right(20);
    assertEquals(10, hitbox.left()); // width constant (20 - 10 = 10)
    assertEquals(20, hitbox.right());

    hitbox.bottom(10);
    assertEquals(10, hitbox.bottom());
    assertEquals(15, hitbox.top());

    hitbox.top(30);
    assertEquals(25, hitbox.bottom()); // top - height = bottom
    assertEquals(30, hitbox.top());
  }

  @Test
  void testIntersectsTrueWhenOverlapping() {
    Hitbox a = new Hitbox(10, 10, 0, 0);
    Hitbox b = new Hitbox(5, 5, 8, 8);
    assertTrue(a.intersects(b));
  }

  @Test
  void testIntersectsFalseWhenSeparate() {
    Hitbox a = new Hitbox(10, 10, 0, 0);
    Hitbox b = new Hitbox(5, 5, 15, 15);
    assertFalse(a.intersects(b));
  }

  @Test
  void testIntersectsWithRectangleSignature() {
    Hitbox a = new Hitbox(10, 10, 0, 0);
    assertTrue(a.intersects(8, 8, 5, 5)); // overlaps
    assertFalse(a.intersects(11, 11, 5, 5)); // just outside
  }

  @Test
  void testCenterOriginAndCenterAccessors() {
    Hitbox hb = new Hitbox(8, 4, 0, 0);
    hb.centerOrigin();

    // Offset should now be (-4, -2)
    assertEquals(Vector2.of(-4, -2), hb.offset());

    // Verify inherited center() and centerX/Y()
    hb.left(10);
    hb.bottom(20);
    assertEquals(14, hb.centerX(), 0.001f);
    assertEquals(22, hb.centerY(), 0.001f);
  }

  @Test
  void testAbsoluteCoordinatesUsePositionAndOffset() {
    Hitbox hb = new Hitbox(10, 5, 2, 3);
    hb.position(new Point(100, 200));

    // absoluteLeft = position.x + offset.x = 102
    assertEquals(102, hb.absoluteLeft(), 0.001f);
    // absoluteTop = position.y + offset.y + height = 200 + 3 + 5 = 208
    assertEquals(208, hb.absoluteTop(), 0.001f);
    // absoluteRight = position.x + offset.x + width = 100 + 2 + 10 = 112
    assertEquals(112, hb.absoluteRight(), 0.001f);
    // absoluteBottom = position.y + offset.y = 203
    assertEquals(203, hb.absoluteBottom(), 0.001f);
  }

  @Test
  void testSizeAndHalfSizeInherited() {
    Hitbox hb = new Hitbox(8, 6, 0, 0);
    assertEquals(Vector2.of(8, 6), hb.size());
    assertEquals(Vector2.of(4, 3), hb.halfSize());
  }

  @Test
  void testCornersReturnCorrectVectors() {
    Hitbox hb = new Hitbox(10, 10, 0, 0);
    var corners = hb.corners();

    assertEquals(Vector2.of(0, 0), corners.get(0)); // bottom-left
    assertEquals(Vector2.of(10, 0), corners.get(1)); // bottom-right
    assertEquals(Vector2.of(0, 10), corners.get(2)); // top-left
    assertEquals(Vector2.of(10, 10), corners.get(3)); // top-right
  }
}
