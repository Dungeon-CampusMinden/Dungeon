package de.fwatermann.dungine.utils.functions;

/**
 * The `IVoidFunction` interface represents a functional interface with a single method `run` that
 * executes a function without any parameters and does not return a result.
 */
@FunctionalInterface
public interface IVoidFunction {

  /** Executes the function. */
  void run();
}
