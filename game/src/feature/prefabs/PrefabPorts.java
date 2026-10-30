package feature.prefabs;

import java.util.Objects;
import java.util.regex.Pattern;

/** Validation helpers shared by the prefab port types. */
final class PrefabPorts {

  private static final Pattern KEY = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

  private PrefabPorts() {}

  /**
   * Checks that a port key is a lowercase, dash-separated identifier.
   *
   * @param key port key
   * @return the key
   */
  static String requireKey(String key) {
    Objects.requireNonNull(key, "key");
    if (!KEY.matcher(key).matches()) {
      throw new IllegalArgumentException("Invalid prefab port key: '" + key + "'");
    }
    return key;
  }

  /**
   * Checks that a text is not blank.
   *
   * @param value text to check
   * @param field field name used in the error message
   * @return the text
   */
  static String requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    return value;
  }
}
