package tracking.core;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

final class TrackingChecks {
  private TrackingChecks() {}

  static int schemaVersion(final int value) {
    if (value < 1) {
      throw new IllegalArgumentException("schemaVersion must be at least 1");
    }
    return value;
  }

  static long nonNegative(final long value, final String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " must not be negative");
    }
    return value;
  }

  static int positive(final int value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException(name + " must be at least 1");
    }
    return value;
  }

  static long positive(final long value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException(name + " must be at least 1");
    }
    return value;
  }

  static String text(final String value, final String name) {
    Objects.requireNonNull(value, name);
    String result = value.strip();
    if (result.isEmpty()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return result;
  }

  static Instant utc(final Instant value, final String name) {
    return Objects.requireNonNull(value, name);
  }

  static UUID uuid(final UUID value, final String name) {
    return Objects.requireNonNull(value, name);
  }

  static JsonNode object(final JsonNode value, final String name) {
    Objects.requireNonNull(value, name);
    if (!value.isObject()) {
      throw new IllegalArgumentException(name + " must be a JSON object");
    }
    return value.deepCopy();
  }
}
