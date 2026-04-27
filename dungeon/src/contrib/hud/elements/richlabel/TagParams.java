package contrib.hud.elements.richlabel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parsed named parameters from a tag's parameter string. Supports {@code key=value} pairs for
 * string/numeric parameters and bare {@code key} tokens for boolean flags. This is the standard
 * mechanism for all tags that accept named parameters.
 *
 * @param values the parsed key-value pairs (bare booleans map to empty string)
 * @param booleans the set of keys that were specified without a value (boolean flags)
 */
public record TagParams(Map<String, String> values, Set<String> booleans) {

  /**
   * Parses a space-separated parameter string into a TagParams instance. Tokens of the form {@code
   * key=value} are stored as string entries. Bare tokens (no {@code =}) are treated as boolean
   * flags. Values may be wrapped in double quotes ({@code key="value with spaces"}) to embed
   * whitespace; the surrounding quotes are stripped. A backslash inside a quoted value escapes the
   * following character (so {@code key="a \"b\" c"} yields {@code a "b" c}).
   *
   * @param raw the raw parameter string, or null/blank for empty params
   * @return the parsed TagParams
   */
  public static TagParams parse(String raw) {
    Map<String, String> vals = new LinkedHashMap<>();
    Set<String> bools = new java.util.LinkedHashSet<>();
    if (raw != null && !raw.isBlank()) {
      int i = 0;
      int n = raw.length();
      while (i < n) {
        // Skip leading whitespace.
        while (i < n && Character.isWhitespace(raw.charAt(i))) i++;
        if (i >= n) break;

        // Read the key (everything up to '=' or whitespace).
        int keyStart = i;
        while (i < n && !Character.isWhitespace(raw.charAt(i)) && raw.charAt(i) != '=') i++;
        String key = raw.substring(keyStart, i).trim();
        if (key.isEmpty()) continue;

        if (i < n && raw.charAt(i) == '=') {
          i++; // consume '='
          String value;
          if (i < n && raw.charAt(i) == '"') {
            // Quoted value: read until matching unescaped '"'.
            i++; // consume opening quote
            StringBuilder sb = new StringBuilder();
            while (i < n) {
              char c = raw.charAt(i);
              if (c == '\\' && i + 1 < n) {
                sb.append(raw.charAt(i + 1));
                i += 2;
              } else if (c == '"') {
                i++;
                break;
              } else {
                sb.append(c);
                i++;
              }
            }
            value = sb.toString();
          } else {
            int valStart = i;
            while (i < n && !Character.isWhitespace(raw.charAt(i))) i++;
            value = raw.substring(valStart, i);
          }
          vals.put(key, value);
        } else {
          bools.add(key);
          vals.put(key, "");
        }
      }
    }
    return new TagParams(vals, bools);
  }

  /**
   * Returns the string value for the given key, or the default if absent.
   *
   * @param key the parameter name
   * @param defaultValue the fallback value
   * @return the parameter value or default
   */
  public String getString(String key, String defaultValue) {
    String v = values.get(key);
    return (v != null && !v.isEmpty()) ? v : defaultValue;
  }

  /**
   * Returns the float value for the given key, or the default if absent or malformed.
   *
   * @param key the parameter name
   * @param defaultValue the fallback value
   * @return the parsed float or default
   */
  public float getFloat(String key, float defaultValue) {
    String v = values.get(key);
    if (v == null || v.isEmpty()) return defaultValue;
    try {
      return Float.parseFloat(v);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Returns true if the given key was specified as a boolean flag (bare token without a value).
   *
   * @param key the parameter name
   * @return true if the flag is present
   */
  public boolean getBoolean(String key) {
    return booleans.contains(key);
  }
}
