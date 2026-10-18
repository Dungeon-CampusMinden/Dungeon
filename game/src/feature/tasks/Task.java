package feature.tasks;

import tools.jackson.databind.json.JsonMapper;

/**
 * Represents an abstract task that evaluates whether a given answer is correct.
 *
 * <p>Each task type also defines how its answers appear in tracking, so every room that uses the
 * type records them the same way; see {@link TaskTracking}.
 *
 * @param <T> the type of answer accepted by the task
 */
public abstract class Task<T> {

  private static final JsonMapper JSON = JsonMapper.builder().build();

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

  /**
   * Returns the tracking name of this task's answer shape, e.g. {@code "choice"}.
   *
   * @return the answer kind recorded with each tracked answer
   */
  public abstract String answerKind();

  /**
   * Encodes an answer for tracking.
   *
   * @param answer the answer to encode
   * @return the complete answer as recorded by tracking
   */
  public abstract String trackedAnswer(T answer);

  /**
   * Encodes a structured answer as compact JSON for {@link #trackedAnswer}.
   *
   * @param answer the answer, with a stable iteration order
   * @return compact JSON
   */
  protected static String json(final Object answer) {
    return JSON.writeValueAsString(answer);
  }
}
