package engine.tracking;

import java.util.List;

/**
 * Help available when an answer was submitted, together with its final failure reasons.
 *
 * @param hintLevel number of released hint stages at submission time
 * @param automaticSolution whether the help system supplied this attempt's solution
 * @param failureReasons concrete failed conditions or runtime errors; empty for a correct answer
 */
public record AttemptDetails(
    int hintLevel, boolean automaticSolution, List<String> failureReasons) {
  /**
   * Validates the hint level and copies the nonblank failure reasons.
   *
   * @param hintLevel number of released hint stages at submission time
   * @param automaticSolution whether the help system supplied this attempt's solution
   * @param failureReasons concrete failed conditions or runtime errors; empty for a correct answer
   */
  public AttemptDetails {
    if (hintLevel < 0) throw new IllegalArgumentException("hintLevel must not be negative");
    failureReasons = List.copyOf(failureReasons);
    if (failureReasons.stream().anyMatch(String::isBlank))
      throw new IllegalArgumentException("failureReasons must not contain blank messages");
  }
}
