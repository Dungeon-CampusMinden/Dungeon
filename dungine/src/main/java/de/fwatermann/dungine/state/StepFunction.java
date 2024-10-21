package de.fwatermann.dungine.state;

/** Contains functional interfaces for step functions with and without results. */
public class StepFunction {

  /** Create a new instance of StepFunction. */
  public StepFunction() {}

  /**
   * Functional interface for a step function that takes {@link StepResults} as input and returns a
   * result.
   *
   * @param <T> the type of the result
   */
  @FunctionalInterface
  public interface IWithResults<T> {
    /**
     * Runs the step function with the given {@link StepResults}.
     *
     * @param results the step results
     * @return the result of the step function
     */
    T run(StepResults results);
  }

  /**
   * Functional interface for a step function that does not take any input and returns a result.
   *
   * @param <T> the type of the result
   */
  @FunctionalInterface
  public interface IWithoutResults<T> {
    /**
     * Runs the step function.
     *
     * @return the result of the step function
     */
    T run();
  }

  /**
   * Functional interface for a step function that takes {@link StepResults} as input and does not
   * return a result.
   */
  @FunctionalInterface
  public interface IVoidWithResults {
    /**
     * Runs the step function with the given {@link StepResults}.
     *
     * @param results the step results
     */
    void run(StepResults results);
  }

  /**
   * Functional interface for a step function that does not take any input and does not return a
   * result.
   */
  @FunctionalInterface
  public interface IVoidWithoutResults {
    /** Runs the step function. */
    void run();
  }
}
