package wizard.runner.contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Immutable language-neutral validation issue.
 *
 * @param severity blocking or warning severity
 * @param phase ordered validation phase
 * @param code stable issue code
 * @param messageKey stable localization key
 * @param arguments sorted JSON-scalar message arguments
 * @param path RFC 6901 pointer into the DEER document
 * @param entity optional entity identity
 * @param relatedPaths sorted unique related RFC 6901 pointers
 */
public record ValidationIssue(
    ValidationSeverity severity,
    ValidationPhase phase,
    IssueCode code,
    String messageKey,
    Map<String, Object> arguments,
    String path,
    Optional<Entity> entity,
    List<String> relatedPaths) {

  /** Creates a closed, defensively copied issue value. */
  public ValidationIssue {
    Objects.requireNonNull(severity, "severity");
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(code, "code");
    requireNonBlank(messageKey, "messageKey");
    path = requirePointer(path, "path");
    entity = Objects.requireNonNull(entity, "entity");
    arguments = immutableArguments(arguments);
    relatedPaths = immutableRelatedPaths(relatedPaths);
  }

  static int compareCodePoints(final String left, final String right) {
    int leftOffset = 0;
    int rightOffset = 0;
    while (leftOffset < left.length() && rightOffset < right.length()) {
      int leftCodePoint = left.codePointAt(leftOffset);
      int rightCodePoint = right.codePointAt(rightOffset);
      int comparison = Integer.compare(leftCodePoint, rightCodePoint);
      if (comparison != 0) {
        return comparison;
      }
      leftOffset += Character.charCount(leftCodePoint);
      rightOffset += Character.charCount(rightCodePoint);
    }
    return Integer.compare(left.length() - leftOffset, right.length() - rightOffset);
  }

  private static Map<String, Object> immutableArguments(final Map<String, Object> source) {
    Objects.requireNonNull(source, "arguments");
    TreeMap<String, Object> sorted = new TreeMap<>(ValidationIssue::compareCodePoints);
    source.forEach(
        (key, value) -> {
          requireNonBlank(key, "argument key");
          sorted.put(key, requireScalar(value, key));
        });
    return Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
  }

  private static Object requireScalar(final Object value, final String key) {
    Objects.requireNonNull(value, "argument value for " + key);
    if (value instanceof String string) {
      return requireValidUnicode(string, "argument value for " + key);
    }
    if (value instanceof Boolean || value instanceof BigInteger) {
      return value;
    }
    if (value instanceof Byte
        || value instanceof Short
        || value instanceof Integer
        || value instanceof Long) {
      return value;
    }
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    if (value instanceof Float floating && Float.isFinite(floating)) {
      return floating;
    }
    if (value instanceof Double floating && Double.isFinite(floating)) {
      return floating;
    }
    throw new IllegalArgumentException("Argument " + key + " is not a finite JSON scalar");
  }

  private static List<String> immutableRelatedPaths(final List<String> source) {
    Objects.requireNonNull(source, "relatedPaths");
    TreeSet<String> sorted = new TreeSet<>(ValidationIssue::compareCodePoints);
    source.forEach(path -> sorted.add(requirePointer(path, "relatedPath")));
    return List.copyOf(sorted);
  }

  private static String requirePointer(final String value, final String label) {
    Objects.requireNonNull(value, label);
    requireValidUnicode(value, label);
    if (!value.isEmpty() && value.charAt(0) != '/') {
      throw new IllegalArgumentException(label + " must be an RFC 6901 pointer");
    }
    for (int index = 0; index < value.length(); index++) {
      if (value.charAt(index) == '~') {
        if (index + 1 >= value.length()
            || (value.charAt(index + 1) != '0' && value.charAt(index + 1) != '1')) {
          throw new IllegalArgumentException(label + " contains an invalid RFC 6901 escape");
        }
        index++;
      }
    }
    return value;
  }

  private static String requireNonBlank(final String value, final String label) {
    Objects.requireNonNull(value, label);
    requireValidUnicode(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }

  private static String requireValidUnicode(final String value, final String label) {
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (Character.isHighSurrogate(character)) {
        if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1))) {
          throw new IllegalArgumentException(label + " contains an unpaired surrogate");
        }
        index++;
      } else if (Character.isLowSurrogate(character)) {
        throw new IllegalArgumentException(label + " contains an unpaired surrogate");
      }
    }
    return value;
  }

  /**
   * Stable entity identity attached to an issue.
   *
   * @param kind entity kind
   * @param id entity identifier
   */
  public record Entity(String kind, String id) {
    /** Creates a non-blank entity identity. */
    public Entity {
      requireNonBlank(kind, "entity kind");
      requireNonBlank(id, "entity id");
    }
  }
}
