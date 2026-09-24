package feature.tasks;

/**
 * Represents an abstract task that evaluates whether a given answer is correct.
 *
 * @param <T> the type of answer accepted by the task
 */
public abstract class Task<T> {

  /** The description or instructions associated with this task. */
  protected String taskDescription;

  /**
   * Determines whether the given answer is correct for this task. Has to be implemented in an
   * actual task.
   *
   * @param answer the answer to evaluate
   * @return {@code true} if the answer is correct; {@code false} otherwise
   */
  public abstract boolean isCorrect(T answer);
}
