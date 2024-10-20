package de.fwatermann.dungine.utils.pair;

/**
 * The `FloatPair` class represents a pair of float values. It provides methods to create a new pair
 * and to convert the pair to a generic `Pair` object.
 *
 * @param a the first float value
 * @param b the second float value
 */
public record FloatPair(float a, float b) {

  /**
   * Creates a new `FloatPair` with the specified values.
   *
   * @param a the first float value
   * @param b the second float value
   * @return a new `FloatPair` instance
   */
  public static FloatPair of(float a, float b) {
    return new FloatPair(a, b);
  }

  /**
   * Converts this `FloatPair` to a generic `Pair` object.
   *
   * @return a `Pair` object containing the float values
   */
  public Pair<Float, Float> getPair() {
    return new Pair<Float, Float>(this.a, this.b);
  }
}
