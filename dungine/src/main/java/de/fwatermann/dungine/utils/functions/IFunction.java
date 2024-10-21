package de.fwatermann.dungine.utils.functions;

/**
 * The `IFunction` interface represents a functional interface with a single method `run` that
 * executes a function and returns a result.
 *
 * @param <R> the type of the result returned by the function
 */
@FunctionalInterface
public interface IFunction<R> {

  /**
   * The function
   *
   * @return the result of the function
   */
  R run();
}
