package core.level.utils;

import core.utils.IVector2;
import core.utils.Point;

/**
 * A record representing a 2D coordinate with x and y Integer components.
 *
 * <p>Provides utility methods for coordinate operations such as converting to a point, adding a
 * vector, calculating the vector to another coordinate, and computing the distance between two
 * coordinates.
 *
 * @param x The x component of the coordinate.
 * @param y The y component of the coordinate.
 * @see Point
 * @see IVector2
 */
public record Coordinate(int x, int y) {

  /**
   * Create a new coordinate from another coordinate.
   *
   * @param other The other coordinate.
   */
  public Coordinate(Coordinate other) {
    this(other.x, other.y);
  }

  /**
   * Convert Coordinate to Point.
   *
   * @return Coordinate converted to a point;
   */
  public Point toPoint() {
    return new Point(x, y);
  }

  /**
   * Convert Coordinate to Point centered in the tile.
   *
   * @return Coordinate converted to a point;
   */
  public Point toCenteredPoint() {
    return new Point(x + 0.5f, y + 0.5f);
  }

  /**
   * Moves this coordinate by a vector.
   *
   * @param vector The vector to move the coordinate by.
   * @return A new coordinate that is the sum of this coordinate and the given vector.
   */
  public Coordinate translate(final IVector2 vector) {
    return new Coordinate(this.x + (int) vector.x(), this.y + (int) vector.y());
  }

  /**
   * Calculates the vector from this coordinate to another coordinate.
   *
   * @param other The other coordinate.
   * @return The vector from this coordinate to the other coordinate.
   */
  public IVector2 vectorTo(final Coordinate other) {
    return IVector2.of(other.x - this.x, other.y - this.y);
  }

  /**
   * Calculates the distance between this Coordinate and another Coordinate.
   *
   * @param other The other Coordinate to calculate the distance to.
   * @return The distance between this Coordinate and the other Coordinate.
   */
  public int distance(Coordinate other) {
    return Math.abs(x - other.x) + Math.abs(y - other.y);
  }
}
