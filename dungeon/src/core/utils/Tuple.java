package core.utils;

/**
 * A simple generic record representing a tuple of two values.
 *
 * @param a foo
 * @param b foo
 * @param <A> foo
 * @param <B> foo
 */
public record Tuple<A, B>(A a, B b) {

  /**
   * Create a new tuple.
   *
   * @param first The first value.
   * @param second The second value.
   * @return The new tuple.
   */
  public static <A, B> Tuple<A, B> of(A first, B second) {
    return new Tuple<>(first, second);
  }
}
