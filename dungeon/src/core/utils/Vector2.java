package core.utils;

import core.level.utils.Coordinate;

/**
 * Represents a 2D vector with mathematical operations. Provides immutable vector operations
 * returning new instances rather than modifying existing ones.
 *
 * <p>This interface returns float values for x and y components, which can cause a loss of
 * precision when converting from double to float.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Vector2 position = Vector2.of(10.0f, 5.0f);
 * Vector2 velocity = Vector2.of(2.0f, 3.0f);
 * Vector2 newPosition = position.add(velocity);
 *
 * // Normalize a vector for direction
 * Vector2 direction = Vector2.of(3.0f, 4.0f).normalize();
 *
 * // Scale and rotate vectors
 * Vector2 scaled = position.scale(2.0f);
 * Vector2 rotated = direction.rotateDeg(90.0f);
 * }</pre>
 *
 * <p>For unit vectors, use {@link Direction} which provides predefined directions and convenience
 * methods for working with cardinal directions.
 *
 * @see Direction
 */
public interface Vector2 {

  /** Zero vector representing no displacement or position at origin. */
  Vector2 ZERO = Vector2.of(0, 0);

  /** Unit vector with both components set to 1. */
  Vector2 ONE = Vector2.of(1, 1);

  /** Vector with maximum float values for both components. */
  Vector2 MAX = Vector2.of(Float.MAX_VALUE, Float.MAX_VALUE);

  /** A small tolerance for floating-point comparisons. */
  double EPSILON = 1e-9;

  /**
   * Creates a new vector with the specified components.
   *
   * @param x The x component
   * @param y The y component
   * @return A new vector with the given components
   */
  static Vector2 of(double x, double y) {
    return new BasicVector2(x, y);
  }

  /**
   * Copies the components of another vector into a new vector.
   *
   * @param other The vector to copy from
   * @return A new vector with the same components as the other vector
   */
  static Vector2 of(Vector2 other) {
    return of(other.x(), other.y());
  }

  /**
   * Create a new vector from a point.
   *
   * @param point The point to create the vector from.
   * @return A new vector with the same x and y coordinates as the point.
   */
  static Vector2 of(Point point) {
    return of(point.x(), point.y());
  }

  /**
   * Create a new vector from a coordinate.
   *
   * @param coordinate The coordinate to create the vector from.
   * @return A new vector with the same x and y coordinates as the coordinate.
   */
  static Vector2 of(Coordinate coordinate) {
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
  default Vector2 add(Vector2 other) {
    return Vector2.of(x() + other.x(), y() + other.y());
  }

  /**
   * Subtracts another vector from this one.
   *
   * @param other The other vector.
   * @return A new vector that is the difference of this and the other vector.
   */
  default Vector2 subtract(Vector2 other) {
    return Vector2.of(x() - other.x(), y() - other.y());
  }

  /**
   * Multiplies this vector by a scalar.
   *
   * @param scalar The scalar.
   * @return A new vector that is this vector multiplied by the scalar.
   */
  default Vector2 scale(double scalar) {
    return Vector2.of(x() * scalar, y() * scalar);
  }

  /**
   * Multiplies this vector by two scalars, using a vector.
   *
   * @param scalar The vector containing the scalars for x and y components.
   * @return A new vector that is this vector multiplied by the scalars in the vector.
   */
  default Vector2 scale(Vector2 scalar) {
    return Vector2.of(x() * scalar.x(), y() * scalar.y());
  }

  /**
   * Returns the Euclidean length (magnitude) of this vector.
   *
   * @return The length of the vector.
   */
  default double length() {
    return Math.sqrt(x() * x() + y() * y());
  }

  /**
   * Checks if the vector is a zero vector (within {@link #EPSILON} tolerance).
   *
   * <p>Uses squared length for better performance and to avoid an unnecessary sqrt.
   *
   * @return true if the vector is a zero vector, false otherwise.
   */
  default boolean isZero() {
    return lengthSquared() < EPSILON * EPSILON;
  }

  /**
   * Normalizes the vector to a unit vector (length of 1).
   *
   * <p>Normalization is useful for ensuring that the vector maintains its direction but has a
   * length of 1.
   *
   * <p>If the vector is effectively zero (length &lt; {@link #EPSILON}), it returns the zero
   * vector.
   *
   * @return A new vector that is the normalized version of this vector or the zero vector if the
   *     length is (near) zero.
   */
  default Vector2 normalize() {
    double len = length();
    if (len < EPSILON) {
      return Vector2.ZERO;
    }
    return Vector2.of(x() / len, y() / len);
  }

  /**
   * Computes the dot product of this vector and another vector. The dot product is useful for
   * determining the angle between vectors and projecting one vector onto another.
   *
   * @param other The other vector
   * @return The dot product as a scalar value
   */
  default double dot(Vector2 other) {
    return x() * other.x() + y() * other.y();
  }

  /**
   * Calculates the squared length of the vector. This is more efficient than {@link #length()} when
   * you only need to compare distances or when the actual length value isn't needed.
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
  default double distance(Vector2 other) {
    return subtract(other).length();
  }

  /**
   * Calculates the angle of this vector relative to the positive X axis (0° = +X) in degrees.
   *
   * <p>This is equivalent to {@code Math.toDegrees(Math.atan2(y(), x()))}.
   *
   * @return the angle in degrees between this vector (as a displacement from origin) and the
   *     positive X axis.
   */
  default double angleDeg() {
    return Math.toDegrees(Math.atan2(y(), x()));
  }

  /**
   * Calculates the angle from this vector to the {@code target} vector in degrees.
   *
   * <p>Semantics: returns {@code Math.toDegrees(Math.atan2(target.y() - this.y(), target.x() -
   * this.x()))}. Use this when you want the angle pointing from {@code this} to {@code target}
   * (i.e. {@code this -> target}).
   *
   * @param target the destination vector
   * @return the angle in degrees from this vector to {@code target}
   */
  default double angleToDeg(Vector2 target) {
    return Math.toDegrees(Math.atan2(target.y() - y(), target.x() - x()));
  }

  /**
   * Rotates this vector by a specified angle in degrees.
   *
   * <p>Positive angles rotate counter-clockwise in a standard Cartesian coordinate system (Y up).
   * If your rendering coordinate system has Y down (screen coordinates), you may need to negate the
   * angle when applying it to sprites.
   *
   * @param degrees The angle in degrees to rotate the vector.
   * @return A new vector that is the result of rotating this vector by the specified angle.
   */
  default Vector2 rotateDeg(double degrees) {
    double radians = Math.toRadians(degrees);
    double cos = Math.cos(radians);
    double sin = Math.sin(radians);
    return Vector2.of(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Rotates this vector by a specified angle in radians.
   *
   * <p>Positive angles rotate counter-clockwise in a standard Cartesian coordinate system (Y up).
   *
   * @param radians The angle in radians to rotate the vector
   * @return A new vector rotated by the specified angle
   */
  default Vector2 rotateRad(double radians) {
    double cos = Math.cos(radians);
    double sin = Math.sin(radians);
    return Vector2.of(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Returns a new vector pointing in the opposite direction.
   *
   * <p>Effectively, this scales both components of the vector by -1.
   *
   * @return A new vector which is the inverse of this vector
   */
  default Vector2 inverse() {
    return this.scale(-1);
  }

  /**
   * Determines the cardinal {@link Direction} based on the vector components.
   *
   * <p>Selection rule: - If |x| >= |y| the vector is considered horizontal: RIGHT if x > 0, LEFT
   * otherwise. - Otherwise the vector is considered vertical: UP if y > 0, DOWN otherwise.
   *
   * <p>This ties in favor of horizontal direction when |x| == |y|.
   *
   * @return the {@link Direction} corresponding to the vector's dominant axis
   */
  default Direction direction() {
    double ax = Math.abs(x());
    double ay = Math.abs(y());
    if (ax >= ay) {
      return x() >= 0 ? Direction.RIGHT : Direction.LEFT;
    } else {
      return y() >= 0 ? Direction.UP : Direction.DOWN;
    }
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
  record BasicVector2(float x, float y) implements Vector2 {

    /**
     * Creates a new Vector2 instance with the specified x and y components.
     *
     * <p>This constructor will convert double values to float, which may result in a loss of
     * precision.
     *
     * @param x the x component
     * @param y the y component
     */
    public BasicVector2(double x, double y) {
      this((float) x, (float) y);
    }

    @Override
    public String toString() {
      return x + "," + y;
    }
  }
}
