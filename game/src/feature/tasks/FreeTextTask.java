package feature.tasks;

import java.util.List;

/**
 * A task that evaluates whether a users free-text answer matches one of the accepted answers.
 *
 * <p>Answers can be compared either case-sensitively or case-insensitively. When case sensitivity
 * is disabled, the accepted answers are normalized to lowercase during construction, and the user's
 * answer is normalized before comparison.
 */
public class FreeTextTask extends Task<String> {

  private final List<String> acceptedAnswer;
  private boolean caseSensitive;

  /**
   * Creates a free-text task with the specified description, accepted answers, and case-sensitivity
   * setting.
   *
   * @param taskDescription the description of the task
   * @param acceptedAnswer the list of answers that are considered correct
   * @param caseSensitive whether answer comparison should be case-sensitive
   */
  public FreeTextTask(String taskDescription, List<String> acceptedAnswer, boolean caseSensitive) {
    this.taskDescription = taskDescription;
    this.caseSensitive = caseSensitive;
    if (this.caseSensitive) {
      this.acceptedAnswer = acceptedAnswer;
    } else {
      this.acceptedAnswer = acceptedAnswer.stream().map(String::toLowerCase).toList();
    }
  }

  /**
   * Creates a case-insensitive free-text task.
   *
   * @param taskDescription the description of the task
   * @param acceptedAnswer the list of answers that are considered correct
   */
  public FreeTextTask(String taskDescription, List<String> acceptedAnswer) {
    this(taskDescription, acceptedAnswer, false);
  }

  /**
   * Creates a case-insensitive free-text task without a description.
   *
   * @param acceptedAnswer the list of answers that are considered correct
   */
  public FreeTextTask(List<String> acceptedAnswer) {
    this("", acceptedAnswer);
  }

  /**
   * Determines whether the given answer matches one of the accepted answers.
   *
   * <p>If case sensitivity is disabled, the answer is converted to lowercase before comparison.
   *
   * @param answer the answer to evaluate
   * @return {@code true} if the answer matches an accepted answer; {@code false} otherwise
   */
  @Override
  public boolean isCorrect(String answer) {
    if (this.caseSensitive) {
      return acceptedAnswer.contains(answer);
    }
    return acceptedAnswer.contains(answer.toLowerCase());
  }

  /**
   * Returns the list of accepted answers.
   *
   * @return the accepted answers
   */
  public List<String> getAcceptedAnswer() {
    return acceptedAnswer;
  }

  /**
   * Returns the description of this task.
   *
   * @return the task description
   */
  public String getTaskDescription() {
    return taskDescription;
  }
}
