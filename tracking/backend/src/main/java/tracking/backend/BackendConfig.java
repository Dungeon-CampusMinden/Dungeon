package tracking.backend;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

record BackendConfig(
    String bindHost,
    int port,
    String databaseUrl,
    String databaseUser,
    String databasePassword,
    Optional<String> runtimeDatabaseUser,
    Optional<String> apiKey,
    int maxBodyBytes,
    int maxBatchEvents) {
  static BackendConfig load() {
    return new BackendConfig(
        setting("bindHost", "DUNGEON_TRACKING_BIND_HOST", "127.0.0.1"),
        integer("port", "DUNGEON_TRACKING_PORT", 8088),
        required("databaseUrl", "DUNGEON_TRACKING_DATABASE_URL"),
        setting("databaseUser", "DUNGEON_TRACKING_DATABASE_USER", ""),
        configuredSecret("databasePassword", "DUNGEON_TRACKING_DATABASE_PASSWORD").orElse(""),
        direct("runtimeDatabaseUser", "DUNGEON_TRACKING_RUNTIME_DATABASE_USER")
            .filter(value -> !value.isBlank()),
        configuredSecret("apiKey", "DUNGEON_TRACKING_API_KEY").map(String::strip),
        integer("maxBodyBytes", "DUNGEON_TRACKING_MAX_BODY_BYTES", 1_048_576),
        integer("maxBatchEvents", "DUNGEON_TRACKING_MAX_BATCH_EVENTS", 500));
  }

  InetSocketAddress address() {
    return new InetSocketAddress(bindHost, port);
  }

  private static String required(final String property, final String environment) {
    String value = setting(property, environment, "");
    if (value.isBlank()) {
      throw new IllegalArgumentException(
          "Missing database URL: set dungeon.tracking." + property + " or " + environment);
    }
    return value;
  }

  private static int integer(
      final String property, final String environment, final int defaultValue) {
    int value = Integer.parseInt(setting(property, environment, Integer.toString(defaultValue)));
    if (value < 1) {
      throw new IllegalArgumentException(property + " must be positive");
    }
    return value;
  }

  private static String setting(
      final String property, final String environment, final String defaultValue) {
    return direct(property, environment).orElse(defaultValue);
  }

  private static Optional<String> configuredSecret(
      final String property, final String environment) {
    Optional<String> value = direct(property, environment);
    if (value.isPresent() && value.orElseThrow().isBlank()) {
      throw new IllegalArgumentException(environment + " must not be blank");
    }
    return value;
  }

  private static Optional<String> direct(final String property, final String environment) {
    String systemValue = System.getProperty("dungeon.tracking." + property);
    if (systemValue != null) {
      return Optional.of(systemValue);
    }
    return Optional.ofNullable(Map.copyOf(System.getenv()).get(environment));
  }
}
