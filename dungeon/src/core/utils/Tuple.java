package core.utils;

import java.io.Serial;
import java.io.Serializable;

/**
 * A simple generic record representing a tuple of two values.
 *
 * @param a foo
 * @param b foo
 * @param <A> foo
 * @param <B> foo
 */
public record Tuple<A, B>(A a, B b) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

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
