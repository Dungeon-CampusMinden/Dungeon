package engine.utils;

/**
 * A functional interface representing an operation that accepts four input arguments and returns no
 * result.
 *
 * @param <T> the first argument type
 * @param <U> the second argument type
 * @param <V> the third argument type
 * @param <W> the fourth argument type
 */
@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {
  /**
   * Performs this operation with the given arguments.
   *
   * @param t the first argument
   * @param u the second argument
   * @param v the third argument
   * @param w the fourth argument
   */
  void accept(final T t, final U u, final V v, final W w);
}
