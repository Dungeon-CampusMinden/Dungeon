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
    this(size.x(), size.y(), offset.x(), offset.y());
  }

  /**
   * Creates a Rectangle from the specified points.
   *
   * @param from first point
   * @param to second point
   */
  public Rectangle(Point from, Point to) {
    this(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y()), Math.min(from.x(), to.x()), Math.min(from.y(), to.y()));
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

  /**
   * Expands the rectangle by the specified amount in all directions.
   *
   * @param amount the amount to expand the rectangle
   * @return a new expanded Rectangle
   */
  public Rectangle expand(float amount) {
    return new Rectangle(width + amount * 2, height + amount * 2, x - amount, y - amount);
  }

  /**
   * Checks if this rectangle intersects with another rectangle.
   *
   * @param worldBounds the other rectangle to check intersection with
   * @return true if the rectangles intersect, false otherwise
   */
  public boolean intersects(Rectangle worldBounds) {
    if (worldBounds == null) return false;
    return x < worldBounds.x + worldBounds.width
        && x + width > worldBounds.x
        && y < worldBounds.y + worldBounds.height
        && y + height > worldBounds.y;
  }

  /**
   * Checks if the rectangle contains the given point.
   *
   * @param point the point to check
   * @return true if the rectangle contains the point, false otherwise
   */
  public boolean contains(Point point) {
    return point.x() >= x && point.x() <= x + width && point.y() >= y && point.y() <= y + height;
  }

  /**
   * Translates the rectangle by the given vector.
   *
   * @param translation the vector to translate by
   * @return a new translated Rectangle
   */
  public Rectangle translate(Vector2 translation) {
    return new Rectangle(width, height, x + translation.x(), y + translation.y());
  }
}
