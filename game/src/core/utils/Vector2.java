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
public record Vector2(float x, float y) {

  /** A constant vector pointing upwards (0, 1). */
  public static final Vector2 UP = new Vector2(0, 1);

  /** A constant vector pointing downwards (0, -1). */
  public static final Vector2 DOWN = new Vector2(0, -1);

  /** A constant vector pointing to the left (-1, 0). */
  public static final Vector2 LEFT = new Vector2(-1, 0);

  /** A constant vector pointing to the right (1, 0). */
  public static final Vector2 RIGHT = new Vector2(1, 0);

  /** A constant vector representing no movement or zero magnitude (0, 0). */
  public static final Vector2 ZERO = new Vector2(0, 0);

  /**
   * Create a new vector from another vector.
   *
   * @param other The other vector.
   */
  public Vector2(Vector2 other) {
    this(other.x, other.y);
  }

  /**
   * Adds another vector to this one.
   *
   * @param other The other vector.
   * @return A new vector that is the sum of this and the other vector.
   */
  public Vector2 add(Vector2 other) {
    return new Vector2(this.x + other.x, this.y + other.y);
  }

  /**
   * Subtracts another vector from this one.
   *
   * @param other The other vector.
   * @return A new vector that is the difference of this and the other vector.
   */
  public Vector2 subtract(Vector2 other) {
    return new Vector2(this.x - other.x, this.y - other.y);
  }

  /**
   * Multiplies this vector by a scalar.
   *
   * @param scalar The scalar.
   * @return A new vector that is this vector multiplied by the scalar.
   */
  public Vector2 scale(float scalar) {
    return new Vector2(this.x * scalar, this.y * scalar);
  }

  /**
   * Multiplies this vector by two scalars.
   *
   * @param scalarX The scalar for the x component.
   * @param scalarY The scalar for the y component.
   * @return A new vector that is this vector multiplied by the scalars.
   */
  public Vector2 scale(float scalarX, float scalarY) {
    return new Vector2(this.x * scalarX, this.y * scalarY);
  }

  /**
   * @return The length of the vector.
   */
  public float length() {
    return (float) Math.sqrt(x * x + y * y);
  }

  /**
   * @return A new vector with the same direction but a length of 1.
   */
  public Vector2 normalize() {
    float len = length();
    if (len != 0) {
      return new Vector2(x / len, y / len);
    }
    return new Vector2(this);
  }

  /**
   * Calculates the dot product of this vector and another vector.
   *
   * @param other The other vector.
   * @return The dot product.
   */
  public float dot(Vector2 other) {
    return this.x * other.x + this.y * other.y;
  }

  /**
   * Creates a new vector that is the inverse of this vector.
   *
   * <p>The inverse of a vector is obtained by negating both its x and y components.
   *
   * @return A new vector with x and y components negated.
   */
  public Vector2 inverse() {
    return new Vector2(-this.x, -this.y);
  }

  /**
   * Rotates this vector by a specified angle in degrees.
   *
   * @param degrees The angle in degrees to rotate the vector.
   * @return A new vector that is the result of rotating this vector by the specified angle.
   */
  public Vector2 rotateDeg(float degrees) {
    float radians = (float) Math.toRadians(degrees);
    float cos = (float) Math.cos(radians);
    float sin = (float) Math.sin(radians);
    return new Vector2(this.x * cos - this.y * sin, this.x * sin + this.y * cos);
  }

  /**
   * Sets the length of the vector to a new value while maintaining its direction.
   *
   * @param newLength The desired length of the vector. (>=0)
   * @return A new vector with the specified length, maintaining the original direction.
   * @throws IllegalArgumentException if the new length is negative.
   */
  public Vector2 setLength(float newLength) {
    if (newLength < 0) {
      throw new IllegalArgumentException("New length must be non-negative.");
    }

    float currentLength = length();
    if (currentLength == 0) {
      return new Vector2(0, 0); // Avoid division by zero
    }
    return new Vector2(x * newLength / currentLength, y * newLength / currentLength);
  }
}
