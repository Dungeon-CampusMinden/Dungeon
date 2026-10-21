package feature.tasks;

import java.util.Objects;
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

  private final String taskDescription;

  /**
   * Creates a task.
   *
   * @param taskDescription the description or instructions shown to players, possibly empty
   */
  protected Task(final String taskDescription) {
    this.taskDescription = Objects.requireNonNull(taskDescription, "taskDescription");
  }

  /**
   * Returns the description or instructions shown to players, e.g. a question.
   *
   * @return the task description, possibly empty
   */
  public String taskDescription() {
    return taskDescription;
  }

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
