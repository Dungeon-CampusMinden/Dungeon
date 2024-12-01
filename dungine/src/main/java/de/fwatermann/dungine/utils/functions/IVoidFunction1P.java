package de.fwatermann.dungine.utils.functions;

/**
 * The `IVoidFunction1P` interface represents a functional interface with a single method `run` that
 * executes a function with one parameter and does not return a result.
 *
 * @param <P1> the type of the parameter accepted by the function
 */
@FunctionalInterface
public interface IVoidFunction1P<P1> {

  /**
   * Executes the function with the given parameter.
   *
   * @param p1 the parameter to be passed to the function
   */
  void run(P1 p1);
}
