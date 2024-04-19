package core.utils;

/**
 * A functional interface representing an operation that accepts three input arguments and returns
 * no result.
 *
 * @param <T> foo
 * @param <U> foo
 * @param <R> foo
 */
@FunctionalInterface
public interface TriConsumer<T, U, R> {
  /**
   * Performs this operation with the given argument.
   *
   * @param t Argument 1
   * @param u Argument 2
   * @param r Argument 3
   */
  void accept(final T t, final U u, final R r);
}
