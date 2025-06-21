package core.utils;

/**
 * A 2D vector with float components.
 *
 * <p>This record is immutable and provides common vector operations.
 */
public record Vector2(float x, float y) {

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
  public Vector2 multiply(float scalar) {
    return new Vector2(this.x * scalar, this.y * scalar);
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
    if (len == 0) {
      return new Vector2(0, 0);
    }
    return new Vector2(x / len, y / len);
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
}

