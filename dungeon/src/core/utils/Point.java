package core.utils;

import core.level.utils.Coordinate;
import java.io.Serial;
import java.io.Serializable;

/**
 * A record representing a 2D point with x and y Float coordinates.
 *
 * <p>Provides utility methods for point operations such as checking distance, calculating distance,
 * converting to a coordinate, and adding a vector.
 *
 * @param x The x coordinate of the point.
 * @param y The y coordinate of the point.
 * @see Vector2
 * @see Coordinate
 */
public record Point(float x, float y) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Create a new point from another point.
   *
   * @param other The other point.
   */
  public Point(Point other) {
    this(other.x, other.y);
  }

  /**
   * Check if two points are positioned in a specified range from each other.
   *
   * @param p1 The first point which is considered.
   * @param p2 The second point which is considered.
   * @param range The range in which the two points are positioned from each other.
   * @return True if the distance between the two points is within the radius, else false.
   */
  public static boolean inRange(final Point p1, final Point p2, final float range) {
    return calculateDistance(p1, p2) <= range;
  }

  /**
   * calculates the distance between two points.
   *
   * @param p1 Point A
   * @param p2 Point B
   * @return the Distance between the two points
   */
  public static float calculateDistance(final Point p1, final Point p2) {
    float xDiff = p1.x() - p2.x();
    float yDiff = p1.y() - p2.y();
    return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
  }

  /**
   * Create new Point centered in the tile.
   *
   * @return centered Point
   */
  public Point toCenteredPoint() {
    return new Point((int) x() + 0.5f, (int) y() + 0.5f);
  }

  /**
   * Convert Point to Coordinate by parsing float to int.
   *
   * @return the converted point
   */
  public Coordinate toCoordinate() {
    return new Coordinate((int) x(), (int) y());
  }

  /**
   * Moves this point by a vector.
   *
   * @param vector The vector to move the point by.
   * @return A new point that is the sum of this point and the given vector.
   */
  public Point translate(final Vector2 vector) {
    return new Point(this.x() + vector.x(), this.y() + vector.y());
  }

  /**
   * Calculates the vector from this point to another point.
   *
   * @param other The other point.
   * @return The vector from this point to the other point.
   */
  public Vector2 vectorTo(final Point other) {
    return Vector2.of(other.x() - this.x(), other.y() - this.y());
  }

  /**
   * Calculates the Euclidean distance between this point and the given point.
   *
   * @param otherPos The point to which the distance is calculated.
   * @return The Euclidean distance between this point and the given point.
   */
  public double distance(Point otherPos) {
    return Math.sqrt(Math.pow(otherPos.x() - x(), 2) + Math.pow(otherPos.y() - y(), 2));
  }

  /**
   * Create new Point flooring the values.
   *
   * @return floored Point
   */
  public Point floor() {
    return new Point((int) x(), (int) y());
  }

  @Override
  public String toString() {
    return x + "," + y;
  }
}
