package core.utils;

/**
 * Represents a 2D vector with mathematical operations. Provides immutable vector operations
 * returning new instances rather than modifying existing ones.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * IVec2 position = IVec2.of(10.0f, 5.0f);
 * IVec2 velocity = IVec2.of(2.0f, 3.0f);
 * IVec2 newPosition = position.add(velocity);
 *
 * // Normalize a vector for direction
 * IVec2 direction = IVec2.of(3.0f, 4.0f).normalize();
 *
 * // Scale and rotate vectors
 * IVec2 scaled = position.scale(2.0f);
 * IVec2 rotated = direction.rotateDeg(90.0f);
 * }</pre>
 */
public interface IVec2 {

  /** Zero vector representing no displacement or position at origin. */
  Vector2 ZERO = new Vector2(0, 0);

  /** Unit vector with both components set to 1. */
  Vector2 ONE = new Vector2(1, 1);

  // TODO: Remove later with Direction enum
  /** Unit vector pointing upward in 2D space. */
  Vector2 UP = new Vector2(0, 1);

  /** Unit vector pointing downward in 2D space. */
  Vector2 DOWN = new Vector2(0, -1);

  /** Unit vector pointing left in 2D space. */
  Vector2 LEFT = new Vector2(-1, 0);

  /** Unit vector pointing right in 2D space. */
  Vector2 RIGHT = new Vector2(1, 0);

  /**
   * Creates a new vector with the specified components.
   *
   * @param x The x component
   * @param y The y component
   * @return A new vector with the given components
   */
  static IVec2 of(float x, float y) {
    return new Vector2(x, y);
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
  default IVec2 add(IVec2 other) {
    return new Vector2(x() + other.x(), y() + other.y());
  }

  /**
   * Subtracts another vector from this one.
   *
   * @param other The other vector.
   * @return A new vector that is the difference of this and the other vector.
   */
  default IVec2 subtract(IVec2 other) {
    return new Vector2(x() - other.x(), y() - other.y());
  }

  /**
   * Multiplies this vector by a scalar.
   *
   * @param scalar The scalar.
   * @return A new vector that is this vector multiplied by the scalar.
   */
  default IVec2 scale(float scalar) {
    return new Vector2(x() * scalar, y() * scalar);
  }

  /**
   * Multiplies this vector by two scalars.
   *
   * @param scalarX The scalar for the x component.
   * @param scalarY The scalar for the y component.
   * @return A new vector that is this vector multiplied by the scalars.
   */
  default IVec2 scale(float scalarX, float scalarY) {
    return new Vector2(x() * scalarX, y() * scalarY);
  }

  /**
   * @return The length of the vector.
   */
  default float length() {
    return (float) Math.sqrt(x() * x() + y() * y());
  }

  /**
   * @return A new vector with the same direction but a length of 1.
   */
  default IVec2 normalize() {
    float len = length();
    if (len == 0) {
      return this; // Return the same vector if length is zero
    }
    return new Vector2(x() / len, y() / len);
  }

  /**
   * Computes the dot product of this vector and another vector. The dot product is useful for
   * determining the angle between vectors and projecting one vector onto another.
   *
   * @param other The other vector
   * @return The dot product as a scalar value
   */
  default float dot(IVec2 other) {
    return x() * other.x() + y() * other.y();
  }

  /**
   * Calculates the squared length of the vector. This is more efficient than length() when you only
   * need to compare distances or when the actual length value isn't needed.
   *
   * @return The squared length of the vector
   */
  default float lengthSquared() {
    return x() * x() + y() * y();
  }

  /**
   * Calculates the distance between this vector and another vector.
   *
   * @param other The other vector
   * @return The distance between the two vectors
   */
  default float distance(IVec2 other) {
    return subtract(other).length();
  }

  /**
   * Rotates this vector by a specified angle in degrees.
   *
   * @param degrees The angle in degrees to rotate the vector.
   * @return A new vector that is the result of rotating this vector by the specified angle.
   */
  default IVec2 rotateDeg(float degrees) {
    float radians = (float) Math.toRadians(degrees);
    float cos = (float) Math.cos(radians);
    float sin = (float) Math.sin(radians);
    return new Vector2(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Rotates this vector by a specified angle in radians.
   *
   * @param radians The angle in radians to rotate the vector
   * @return A new vector rotated by the specified angle
   */
  default IVec2 rotateRad(float radians) {
    float cos = (float) Math.cos(radians);
    float sin = (float) Math.sin(radians);
    return new Vector2(x() * cos - y() * sin, x() * sin + y() * cos);
  }

  /**
   * Sets the length of the vector to a new value while maintaining its direction.
   *
   * @param newLength The desired length of the vector. (>=0)
   * @return A new vector with the specified length, maintaining the original direction.
   * @throws IllegalArgumentException if the new length is negative.
   */
  default Vector2 setLength(float newLength) {
    if (newLength < 0) {
      throw new IllegalArgumentException("New length must be non-negative.");
    }

    float currentLength = length();
    if (currentLength == 0) {
      return new Vector2(0, 0); // Avoid division by zero
    }
    return new Vector2(x() * newLength / currentLength, y() * newLength / currentLength);
  }
}
