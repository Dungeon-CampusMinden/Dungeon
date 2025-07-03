package core.utils;

/**
 * A record representing a 2D vector with x and y Float components.
 *
 * <p>Provides utility methods for vector operations such as addition, subtraction, scaling,
 * normalization, and rotation.
 *
 * @param x The x component of the vector.
 * @param y The y component of the vector.
 * @see Point
 * @see core.level.utils.Coordinate Coordinate
 */
public record Vector2(float x, float y) implements IVec2 {

  /**
   * Create a new vector from another vector.
   *
   * @param other The other vector.
   */
  public Vector2(IVec2 other) {
    this(other.x(), other.y());
  }

  /**
   * Create a new vector from a point.
   *
   * @param point The point to create the vector from.
   */
  public Vector2(Point point) {
    this(point.x(), point.y());
  }

  /**
   * Create a new vector from a coordinate.
   *
   * @param coordinate The coordinate to create the vector from.
   */
  public Vector2(core.level.utils.Coordinate coordinate) {
    this(coordinate.x(), coordinate.y());
  }
}
