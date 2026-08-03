package foundation.definition;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Shared construction checks for immutable Foundation definitions. */
final class DefinitionChecks {
  private static final int MAX_ID_CHARACTERS = 64;
  private static final Pattern STABLE_ID = Pattern.compile("[a-z][a-z0-9_-]*");

  private DefinitionChecks() {}

  static String requireId(final String value, final String label) {
    requireText(value, label);
    if (!STABLE_ID.matcher(value).matches()) {
      throw new IllegalArgumentException(
          label + " must be a stable lowercase identifier using letters, digits, _ or -");
    }
    if (value.length() > MAX_ID_CHARACTERS) {
      throw new IllegalArgumentException(label + " must not exceed 64 characters");
    }
    return value;
  }

  static String requireText(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }

  static List<String> copyUniqueIds(final List<String> values, final String label) {
    Objects.requireNonNull(values, label);
    List<String> result = values.stream().map(value -> requireId(value, label)).toList();
    if (result.isEmpty()) {
      throw new IllegalArgumentException(label + " must not be empty");
    }
    if (new HashSet<>(result).size() != result.size()) {
      throw new IllegalArgumentException(label + " must contain unique identifiers");
    }
    return result;
  }

  static List<HintDefinition> copyOrderedHints(
      final List<HintDefinition> values, final String label) {
    List<HintDefinition> result = List.copyOf(Objects.requireNonNull(values, label));
    Set<String> ids = new HashSet<>();
    for (HintDefinition hint : result) {
      Objects.requireNonNull(hint, label + " entry");
      if (!ids.add(hint.id())) {
        throw new IllegalArgumentException(label + " must contain unique identifiers");
      }
    }
    return result;
  }
}
