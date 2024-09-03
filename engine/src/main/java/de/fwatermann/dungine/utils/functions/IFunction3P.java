package de.fwatermann.dungine.utils.functions;

/**
 * The `IFunction3P` interface represents a functional interface with a single method `run` that executes a function with three parameters and returns a result.
 *
 * @param <R> the type of the result returned by the function
 * @param <P1> the type of the first parameter accepted by the function
 * @param <P2> the type of the second parameter accepted by the function
 * @param <P3> the type of the third parameter accepted by the function
 */
@FunctionalInterface
public interface IFunction3P<R, P1, P2, P3> {

  /**
   * Executes the function with the given parameters and returns a result.
   *
   * @param p1 the first parameter to be passed to the function
   * @param p2 the second parameter to be passed to the function
   * @param p3 the third parameter to be passed to the function
   * @return the result of the function execution
   */
  R run(P1 p1, P2 p2, P3 p3);

}
