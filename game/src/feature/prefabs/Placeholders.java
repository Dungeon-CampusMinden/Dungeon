package feature.prefabs;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {@code {alias}} placeholders in texts with artifact values, e.g. {@code "The code is
 * {code}."}.
 *
 * <p>Aliases use the characters allowed by {@link PrefabLink#isValidAlias}. Braces around other
 * text, and placeholders without a value, are kept as written.
 */
public final class Placeholders {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z0-9_-]+)}");

  private Placeholders() {}

  /**
   * Returns the placeholder names used in a text.
   *
   * @param text text to scan
   * @return names in order of first use
   */
  public static Set<String> names(String text) {
    Set<String> result = new LinkedHashSet<>();
    if (text == null) return result;
    Matcher matcher = PLACEHOLDER.matcher(text);
    while (matcher.find()) result.add(matcher.group(1));
    return result;
  }

  /**
   * Replaces placeholders with values.
   *
   * @param text text containing placeholders
   * @param values values by alias
   * @param missing text used for known aliases without a value, e.g. {@code "???"}
   * @param known aliases that are linked, whose placeholders are replaced by {@code missing} if
   *     they have no value
   * @return text with replaced placeholders
   */
  public static String apply(
      String text, Map<String, String> values, String missing, Set<String> known) {
    Matcher matcher = PLACEHOLDER.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String name = matcher.group(1);
      String replacement;
      if (values.containsKey(name)) replacement = values.get(name);
      else if (known.contains(name)) replacement = missing;
      else replacement = matcher.group();
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }
}
