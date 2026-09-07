package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One expected line of terminal code with one or more accepted patterns.
 *
 * @param patterns alternative patterns accepted for this code line
 */
public record CodeLine(Pattern... patterns) {

  private static final Pattern NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

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

  boolean check(String input, TerminalMatchContext context) {
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(input.trim());
      if (!matcher.matches()) {
        continue;
      }
      TerminalMatchContext candidate = context.copy();
      if (bindNamedGroups(pattern, matcher, candidate)) {
        context.replaceWith(candidate);
        return true;
      }
    }
    return false;
  }

  private static boolean bindNamedGroups(
      Pattern pattern, Matcher matcher, TerminalMatchContext context) {
    Matcher groupMatcher = NAMED_GROUP.matcher(pattern.pattern());
    while (groupMatcher.find()) {
      String groupName = groupMatcher.group(1);
      String value = matcher.group(groupName);
      if (value != null && !context.bind(groupName, value)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Pattern[] patterns() {
    return Arrays.copyOf(patterns, patterns.length);
  }
}
