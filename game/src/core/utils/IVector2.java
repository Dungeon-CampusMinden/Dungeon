package core.utils;

import core.level.utils.Coordinate;

/**
 * Represents a 2D vector with mathematical operations. Provides immutable vector operations
 * returning new instances rather than modifying existing ones.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * IVector2 position = IVector2.of(10.0f, 5.0f);
 * IVector2 velocity = IVector2.of(2.0f, 3.0f);
 * IVector2 newPosition = position.add(velocity);
 *
 * // Normalize a vector for direction
 * IVector2 direction = IVector2.of(3.0f, 4.0f).normalize();
 *
 * // Scale and rotate vectors
 * IVector2 scaled = position.scale(2.0f);
 * IVector2 rotated = direction.rotateDeg(90.0f);
 * }</pre>
 */
public interface IVector2 {

  /** Zero vector representing no displacement or position at origin. */
  IVector2 ZERO = IVector2.of(0, 0);

  /** Unit vector with both components set to 1. */
  IVector2 ONE = IVector2.of(1, 1);

  // TODO: Remove later with Direction enum
  /** Unit vector pointing upward in 2D space. */
  IVector2 UP = IVector2.of(0, 1);

  /** Unit vector pointing downward in 2D space. */
  IVector2 DOWN = IVector2.of(0, -1);

  /** Unit vector pointing left in 2D space. */
  IVector2 LEFT = IVector2.of(-1, 0);

  /** Unit vector pointing right in 2D space. */
  IVector2 RIGHT = IVector2.of(1, 0);

  /** A small tolerance for floating-point comparisons. */
  double EPSILON = 1e-9;

  /**
   * Creates a new vector with the specified components.
   *
   * <p>This method will cast the double values to float, which may result in a loss of precision.
   *
   * @param x The x component
   * @param y The y component
   * @return A new vector with the given components
   */
  static IVector2 of(double x, double y) {
    return new Vector2((float) x, (float) y);
  }

  /**
   * Copies the components of another vector into a new vector.
   *
   * @param other The vector to copy from
   * @return A new vector with the same components as the other vector
   */
  static IVector2 of(IVector2 other) {
    return of(other.x(), other.y());
  }

  /**
   * Create a new vector from a point.
   *
   * @param point The point to create the vector from.
   * @return A new vector with the same x and y coordinates as the point.
   */
  static IVector2 of(Point point) {
    return of(point.x(), point.y());
  }

  /**
   * Create a new vector from a coordinate.
   *
   * @param coordinate The coordinate to create the vector from.
   * @return A new vector with the same x and y coordinates as the coordinate.
   */
  static IVector2 of(Coordinate coordinate) {
    return of(coordinate.x(), coordinate.y());
  }

  /**
   * Gets the x component of the vector.
   *
   * @return The x component.
   */
  float x();

  /**
   * Gets the y component of the vector.
   *
   * @return The y component.
   */
  float y();

  /**
   * Adds another vector to this one.
   *
   * @param other The other vector.
   * @return A new vector that is the sum of this and the other vector.
   */
  default IVector2 add(IVector2 other) {
    return IVector2.of(x() + other.x(), y() + other.y());
  }

  /**
   * Subtracts another vector from this one.
   *
   * @param other The other vector.
   * @return A new vector that is the difference of this and the other vector.
   */
  default IVector2 subtract(IVector2 other) {
    return IVector2.of(x() - other.x(), y() - other.y());
  }

  /**
   * Multiplies this vector by a scalar.
   *
   * @param scalar The scalar.
   * @return A new vector that is this vector multiplied by the scalar.
   */
  default IVector2 scale(double scalar) {
    return IVector2.of(x() * scalar, y() * scalar);
  }

  /**
   * Multiplies this vector by two scalars.
   *
   * @param scalarX The scalar for the x component.
   * @param scalarY The scalar for the y component.
   * @return A new vector that is this vector multiplied by the scalars.
   */
  default IVector2 scale(double scalarX, double scalarY) {
    return IVector2.of(x() * scalarX, y() * scalarY);
  }

  /**
   * @return The length of the vector.
   */
  default double length() {
    return Math.sqrt(x() * x() + y() * y());
  }

  /**
   * Normalizes the vector to a unit vector (length of 1).
   *
   * <p>Normalization is useful for ensuring that the vector maintains its direction but has a
   * length of 1.
   *
   * <p>If the vector is zero (length 0), it returns the zero vector.
   *
   * @return A new vector that is the normalized version of this vector or the zero vector if the
   *     length is 0.
   */
  default IVector2 normalize() {
    double len = length();
    if (len < EPSILON) {
      return IVector2.ZERO;
    }
    return IVector2.of(x() / len, y() / len);
  }

  /**
   * Computes the dot product of this vector and another vector. The dot product is useful for
   * determining the angle between vectors and projecting one vector onto another.
   *
   * @param other The other vector
   * @return The dot product as a scalar value
   */
  default double dot(IVector2 other) {
    return x() * other.x() + y() * other.y();
  }

  /**
   * Calculates the squared length of the vector. This is more efficient than length() when you only
   * need to compare distances or when the actual length value isn't needed.
   *
   * @return The squared length of the vector
   */
  default double lengthSquared() {
    return x() * x() + y() * y();
  }

  /**
   * Calculates the distance between this vector and another vector.
   *
   * @param other The other vector
   * @return The distance between the two vectors
   */
  default double distance(IVector2 other) {
    return subtract(other).length();
  }

  /**
   * Rotates this vector by a specified angle in degrees.
   *
   * @param degrees The angle in degrees to rotate the vector.
   * @return A new vector that is the result of rotating this vector by the specified angle.
   */
  default IVector2 rotateDeg(double degrees) {
    double radians = Math.toRadians(degrees);
    double cos = Math.cos(radians);
    double sin = Math.sin(radians);
    return IVector2.of(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Rotates this vector by a specified angle in radians.
   *
   * @param radians The angle in radians to rotate the vector
   * @return A new vector rotated by the specified angle
   */
  default IVector2 rotateRad(double radians) {
    double cos = Math.cos(radians);
    double sin = Math.sin(radians);
    return IVector2.of(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Sets the length of the vector to a new value while maintaining its direction.
   *
   * @param newLength The desired length of the vector. (>=0)
   * @return A new vector with the specified length, maintaining the original direction.
   * @throws IllegalArgumentException if the new length is negative.
   */
  default IVector2 setLength(double newLength) {
    if (newLength < 0) {
      throw new IllegalArgumentException("New length must be non-negative.");
    }

    double currentLength = length();
    if (currentLength < EPSILON) {
      return IVector2.of(0, 0); // Avoid division by zero
    }
    return IVector2.of(x() * newLength / currentLength, y() * newLength / currentLength);
  }

  /**
   * A record representing a 2D vector with x and y Float components.
   *
   * <p>Provides utility methods for vector operations such as addition, subtraction, scaling,
   * normalization, and rotation.
   *
   * @param x The x component of the vector.
   * @param y The y component of the vector.
   * @see Point
   * @see Coordinate
   */
  record Vector2(float x, float y) implements IVector2 {}
}
