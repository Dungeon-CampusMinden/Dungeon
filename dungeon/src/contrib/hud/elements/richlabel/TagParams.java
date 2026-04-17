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
   * flags.
   *
   * @param raw the raw parameter string, or null/blank for empty params
   * @return the parsed TagParams
   */
  public static TagParams parse(String raw) {
    Map<String, String> vals = new LinkedHashMap<>();
    Set<String> bools = new java.util.LinkedHashSet<>();
    if (raw != null && !raw.isBlank()) {
      for (String token : raw.trim().split("\\s+")) {
        int eq = token.indexOf('=');
        if (eq >= 0) {
          vals.put(token.substring(0, eq).trim(), token.substring(eq + 1).trim());
        } else {
          bools.add(token.trim());
          vals.put(token.trim(), "");
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
