package foundation.room.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

final class RoomModelChecks {
  private static final Pattern STABLE_ID = Pattern.compile("[a-z][a-z0-9_-]{0,63}");
  private static final Pattern SHA_256 = Pattern.compile("[0-9a-f]{64}");
  private static final String CUSTOM_ASSET_PREFIX = "assets/custom/";

  private RoomModelChecks() {}

  static String requireText(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }

  static String requireId(final String value, final String label) {
    requireText(value, label);
    if (!STABLE_ID.matcher(value).matches()) {
      throw new IllegalArgumentException(
          label + " must be a stable lowercase identifier using letters, digits, _ or -");
    }
    return value;
  }

  static String requireSha256(final String value, final String label) {
    requireText(value, label);
    if (!SHA_256.matcher(value).matches()) {
      throw new IllegalArgumentException(label + " must be lowercase SHA-256");
    }
    return value;
  }

  static String requireCustomAssetPath(final String value) {
    requireText(value, "custom asset path");
    if (!value.startsWith(CUSTOM_ASSET_PREFIX)) {
      throw new IllegalArgumentException("custom asset path must begin with assets/custom/");
    }
    return value;
  }

  static <T> List<T> copyUnique(
      final List<T> source, final Function<T, String> identity, final String label) {
    List<T> result = List.copyOf(Objects.requireNonNull(source, label));
    if (new HashSet<>(result.stream().map(identity).toList()).size() != result.size()) {
      throw new IllegalArgumentException(label + " must have unique identifiers");
    }
    return result;
  }
}
