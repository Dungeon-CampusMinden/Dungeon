package feature.prefabs;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Naming rules for prefab instances. */
public final class PrefabNames {

  private static final Pattern TRAILING_NUMBER = Pattern.compile("^(.*?)(\\d+)$");

  /**
   * Orders names like people read them: case-insensitive, and numbers by their value, so {@code
   * "lever-2"} comes before {@code "lever-10"}.
   */
  public static final Comparator<String> NATURAL_ORDER = PrefabNames::compareNatural;

  private PrefabNames() {}

  /**
   * Derives a free name from a name, e.g. for a copy of a prefab. A trailing number is incremented,
   * otherwise {@code -1} is appended. The number keeps being incremented until the name is free.
   *
   * @param name source name
   * @param taken whether a name is already in use
   * @return unused derived name
   */
  public static String nextName(String name, Predicate<String> taken) {
    Matcher matcher = TRAILING_NUMBER.matcher(name);
    String prefix;
    String digits;
    if (matcher.matches()) {
      prefix = matcher.group(1);
      digits = matcher.group(2);
    } else {
      prefix = name + "-";
      digits = "0";
    }
    BigInteger number = new BigInteger(digits);
    String candidate;
    do {
      number = number.add(BigInteger.ONE);
      String formatted = number.toString();
      if (formatted.length() < digits.length()) {
        formatted = "0".repeat(digits.length() - formatted.length()) + formatted;
      }
      candidate = prefix + formatted;
    } while (taken.test(candidate));
    return candidate;
  }

  private static int compareNatural(String first, String second) {
    int i = 0;
    int j = 0;
    while (i < first.length() && j < second.length()) {
      char a = first.charAt(i);
      char b = second.charAt(j);
      if (Character.isDigit(a) && Character.isDigit(b)) {
        int endA = digitsEnd(first, i);
        int endB = digitsEnd(second, j);
        int compared =
            new BigInteger(first.substring(i, endA))
                .compareTo(new BigInteger(second.substring(j, endB)));
        if (compared != 0) return compared;
        i = endA;
        j = endB;
        continue;
      }
      int compared = Character.compare(Character.toLowerCase(a), Character.toLowerCase(b));
      if (compared != 0) return compared;
      i++;
      j++;
    }
    int byLength = Integer.compare(first.length() - i, second.length() - j);
    return byLength != 0 ? byLength : first.compareTo(second);
  }

  private static int digitsEnd(String text, int start) {
    int end = start;
    while (end < text.length() && Character.isDigit(text.charAt(end))) end++;
    return end;
  }
}
