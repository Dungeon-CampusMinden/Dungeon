package de.fwatermann.dungine.utils.functions;

/**
 * The `IVoidFunction4P` interface represents a functional interface with a single method `run` that
 * executes a function with four parameters and does not return a result.
 *
 * @param <P1> the type of the first parameter accepted by the function
 * @param <P2> the type of the second parameter accepted by the function
 * @param <P3> the type of the third parameter accepted by the function
 * @param <P4> the type of the fourth parameter accepted by the function
 */
@FunctionalInterface
public interface IVoidFunction4P<P1, P2, P3, P4> {

  /**
   * Executes the function with the given parameters.
   *
   * @param p1 the first parameter to be passed to the function
   * @param p2 the second parameter to be passed to the function
   * @param p3 the third parameter to be passed to the function
   * @param p4 the fourth parameter to be passed to the function
   */
  void run(P1 p1, P2 p2, P3 p3, P4 p4);
}
