package core.utils;

/**
 * A record representing a rectangle with width, height, and position offset.
 *
 * @param width width of the rectangle
 * @param height height of the rectangle
 * @param x x position of the rectangle
 * @param y y position of the rectangle
 */
public record Rectangle(float width, float height, float x, float y) {

  /**
   * Creates a Rectangle with specified size and offset.
   *
   * @param size size of the rectangle
   * @param offset offset position of the rectangle
   */
  public Rectangle(Vector2 size, Vector2 offset) {
    this(offset.x(), offset.y(), size.x(), size.y());
  }

  /**
   * Creates a Rectangle with specified size and zero offset.
   *
   * @param width width of the rectangle
   * @param height height of the rectangle
   */
  public Rectangle(float width, float height) {
    this(width, height, 0, 0);
  }

  /**
   * Creates a Rectangle with specified size and zero offset.
   *
   * @param size size of the rectangle
   */
  public Rectangle(Vector2 size) {
    this(size.x(), size.y());
  }

  /**
   * Returns the size of the rectangle as a Vector2.
   *
   * @return the size of the rectangle
   */
  public Vector2 size() {
    return Vector2.of(width, height);
  }

  /**
   * Returns the offset of the rectangle as a Vector2.
   *
   * @return the offset of the rectangle
   */
  public Vector2 offset() {
    return Vector2.of(x, y);
  }
}
