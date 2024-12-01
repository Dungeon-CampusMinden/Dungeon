package de.fwatermann.dungine.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the results of steps executed by a {@link LoadStepper}. Provides methods to retrieve
 * and set results based on step indices or identifiers.
 */
public class StepResults {

  private final LoadStepper stepper;

  /**
   * Constructs a new StepResults instance with the specified stepper.
   *
   * @param stepper the LoadStepper associated with these results
   */
  protected StepResults(LoadStepper stepper) {
    this.stepper = stepper;
  }

  private final Map<Integer, Object> results = new HashMap<>();

  /**
   * Retrieves the result of the specified step.
   *
   * @param <T> the type of the result
   * @param step the index of the step
   * @return the result of the step, or null if no result is found
   */
  public <T> T result(int step) {
    Object result = this.results.getOrDefault(step, null);
    if (result == null) return null;
    return (T) result;
  }

  /**
   * Retrieves the result of the step identified by the specified ID.
   *
   * @param <T> the type of the result
   * @param id the identifier of the step
   * @return the result of the step, or null if no result is found
   */
  public <T> T result(String id) {
    int index = this.stepper.stepMap.getOrDefault(id, -1);
    if (index == -1) return null;
    return this.result(index);
  }

  /**
   * Sets the result for the specified step.
   *
   * @param step the index of the step
   * @param result the result to set
   */
  protected void setResult(int step, Object result) {
    this.results.put(step, result);
  }
}
