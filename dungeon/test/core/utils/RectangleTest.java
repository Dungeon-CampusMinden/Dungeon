package core.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for the {@link Rectangle} class. */
public class RectangleTest {

  private static final double DELTA = 1e-6;

  /** Tests the creation of a rectangle from x/y bounds. */
  @Test
  public void testOfBounds() {
    Rectangle rectangle = Rectangle.ofBounds(1.0f, 2.0f, 3.0f, 4.0f);

    assertEquals(3.0f, rectangle.width(), DELTA);
    assertEquals(4.0f, rectangle.height(), DELTA);
    assertEquals(1.0f, rectangle.x(), DELTA);
    assertEquals(2.0f, rectangle.y(), DELTA);
  }

  /** Tests copying a rectangle preserves the canonical value order. */
  @Test
  public void testCopyOf() {
    Rectangle original = new Rectangle(3.0f, 4.0f, 1.0f, 2.0f);
    Rectangle copy = Rectangle.copyOf(original);

    assertEquals(original.width(), copy.width(), DELTA);
    assertEquals(original.height(), copy.height(), DELTA);
    assertEquals(original.x(), copy.x(), DELTA);
    assertEquals(original.y(), copy.y(), DELTA);
    assertNotSame(original, copy);
  }
}
