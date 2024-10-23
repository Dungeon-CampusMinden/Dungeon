package de.fwatermann.dungine.utils.functions;

/**
 * The `IFunction1P` interface represents a functional interface with a single method `run` that
 * executes a function with one parameter and returns a result.
 *
 * @param <R> the type of the result returned by the function
 * @param <P1> the type of the parameter accepted by the function
 */
@FunctionalInterface
public interface IFunction1P<R, P1> {

  /**
   * Executes the function with the given parameter and returns a result.
   *
   * @param p1 the parameter to be passed to the function
   * @return the result of the function execution
   */
  R run(P1 p1);
}
