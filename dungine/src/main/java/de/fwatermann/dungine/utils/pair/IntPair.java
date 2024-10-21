package de.fwatermann.dungine.utils.pair;

/**
 * The `IntPair` record represents a pair of integers. It provides utility methods to create an
 * instance of the pair and to retrieve the pair as a generic `Pair` object.
 *
 * @param a the first integer
 * @param b the second integer
 */
public record IntPair(int a, int b) {

  /**
   * Creates a new `IntPair` with the specified integers.
   *
   * @param a the first integer
   * @param b the second integer
   * @return a new `IntPair` instance
   */
  public static IntPair of(int a, int b) {
    return new IntPair(a, b);
  }

  /**
   * Returns the pair as a generic `Pair` object.
   *
   * @return a `Pair` object containing the two integers
   */
  public Pair<Integer, Integer> getPair() {
    return new Pair<>(this.a, this.b);
  }
}
