package feature.tasks;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A code entered digit by digit, e.g. on a keypad, that is correct when it matches exactly.
 *
 * <p>The code is resolved on every check, so it can come from another source, e.g. a code generated
 * by another prefab. An empty code means that no answer is correct.
 */
public final class NumericCodeTask extends Task<String> {

  private final Supplier<String> code;

  /**
   * Creates a numeric code task with a fixed code.
   *
   * @param code the correct code
   */
  public NumericCodeTask(final String code) {
    this(() -> code);
    Objects.requireNonNull(code, "code");
  }

  /**
   * Creates a numeric code task whose code is resolved whenever it is needed.
   *
   * @param code supplies the current correct code
   */
  public NumericCodeTask(final Supplier<String> code) {
    super("");
    this.code = Objects.requireNonNull(code, "code");
  }

  @Override
  public boolean isCorrect(final String answer) {
    String current = code.get();
    return !current.isEmpty() && current.equals(answer);
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
