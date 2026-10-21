package feature.tasks;

import java.util.Objects;

/** A code entered digit by digit, e.g. on a keypad, that is correct when it matches exactly. */
public final class NumericCodeTask extends Task<String> {

  private final String code;

  /**
   * Creates a numeric code task.
   *
   * @param code the correct code
   */
  public NumericCodeTask(final String code) {
    super("");
    this.code = Objects.requireNonNull(code, "code");
  }

  @Override
  public boolean isCorrect(final String answer) {
    return code.equals(answer);
  }

  @Override
  public String answerKind() {
    return "numeric-code";
  }

  /**
   * Returns the entered digits unchanged.
   *
   * @param answer the entered digits
   * @return the entered digits
   */
  @Override
  public String trackedAnswer(final String answer) {
    return answer;
  }
}
