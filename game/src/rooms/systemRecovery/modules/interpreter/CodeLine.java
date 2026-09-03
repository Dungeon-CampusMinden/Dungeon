package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One expected line of terminal code with one or more accepted patterns.
 *
 * @param patterns alternative patterns accepted for this code line
 */
public record CodeLine(Pattern... patterns) {

  /**
   * Creates an immutable code-line definition.
   *
   * @param patterns alternative patterns accepted for this code line
   */
  public CodeLine {
    if (patterns.length == 0) {
      throw new IllegalArgumentException("At least one pattern is required");
    }
    patterns = Arrays.copyOf(patterns, patterns.length);
    Arrays.stream(patterns).forEach(Objects::requireNonNull);
  }

  /**
   * Checks whether the input matches one of this line's patterns.
   *
   * @param input terminal statement to check
   * @return true if any pattern matches the complete input
   */
  public boolean check(String input) {
    return Arrays.stream(patterns).anyMatch(pattern -> pattern.matcher(input.trim()).matches());
  }

  @Override
  public Pattern[] patterns() {
    return Arrays.copyOf(patterns, patterns.length);
  }
}
