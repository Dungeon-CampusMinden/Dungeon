package engine.tracking;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration for one authoritative tracking session.
 *
 * @param roomId stable room identifier
 * @param endpoint optional self-hosted HTTP endpoint
 * @param apiKey optional bearer credential
 * @param outboxDirectory directory for local append-only tracking files
 * @param operatorEmail operator email shown when remote upload remains pending
 */
record TrackingConfig(
    String roomId,
    Optional<URI> endpoint,
    Optional<String> apiKey,
    Path outboxDirectory,
    String operatorEmail) {

  static final String DEFAULT_OPERATOR_EMAIL = "tracking@example.com";

  static final String ROOM_ID_PROPERTY = "dungeon.tracking.roomId";
  static final String ENDPOINT_PROPERTY = "dungeon.tracking.endpoint";
  static final String API_KEY_PROPERTY = "dungeon.tracking.apiKey";
  static final String OUTBOX_PROPERTY = "dungeon.tracking.outbox";

  private static final String ROOM_ID_ENV = "DUNGEON_TRACKING_ROOM_ID";
  private static final String ENDPOINT_ENV = "DUNGEON_TRACKING_ENDPOINT";
  private static final String API_KEY_ENV = "DUNGEON_TRACKING_API_KEY";
  private static final String OUTBOX_ENV = "DUNGEON_TRACKING_OUTBOX";

  private static final Map<String, String> PROPERTY_ENVIRONMENT_MAPPING =
      Map.of(
          ROOM_ID_PROPERTY,
          ROOM_ID_ENV,
          ENDPOINT_PROPERTY,
          ENDPOINT_ENV,
          API_KEY_PROPERTY,
          API_KEY_ENV,
          OUTBOX_PROPERTY,
          OUTBOX_ENV);

  /** Validates and normalizes one immutable configuration. */
  TrackingConfig {
    roomId = requireText(roomId, "roomId");
    endpoint = Objects.requireNonNull(endpoint, "endpoint");
    endpoint.ifPresent(TrackingConfig::validateEndpoint);
    apiKey = optionalText(apiKey, "apiKey");
    outboxDirectory = Objects.requireNonNull(outboxDirectory, "outboxDirectory").toAbsolutePath();
    operatorEmail = requireText(operatorEmail, "operatorEmail");
  }

  /** Creates a builder with the required stable room ID. */
  private static Builder builder(String roomId) {
    return new Builder(roomId);
  }

  /**
   * Creates room configuration while retaining deployment settings from properties or environment.
   */
  static TrackingConfig forRoom(String roomId) {
    Builder builder = builder(roomId);
    applyDeploymentValues(builder);
    return builder.build();
  }

  /** Creates room configuration with an explicit operator email. */
  static TrackingConfig forRoom(String roomId, String operatorEmail) {
    Builder builder = builder(roomId).operatorEmail(operatorEmail);
    applyDeploymentValues(builder);
    return builder.build();
  }

  /** Reads configuration from system properties first and environment variables second. */
  static Optional<TrackingConfig> fromEnvironment() {
    return value(ROOM_ID_PROPERTY, ROOM_ID_ENV)
        .map(
            roomId -> {
              Builder builder = builder(roomId);
              applyDeploymentValues(builder);
              return builder.build();
            });
  }

  /**
   * Returns child-environment overrides for nonblank tracking system properties.
   *
   * <p>Credentials stay out of command-line arguments. Inherited environment values remain
   * unchanged when no system property overrides them.
   *
   * @return immutable child-environment overrides
   */
  static Map<String, String> childEnvironmentOverrides() {
    Map<String, String> overrides = new HashMap<>();
    PROPERTY_ENVIRONMENT_MAPPING.forEach(
        (property, environmentName) -> {
          String propertyValue = System.getProperty(property);
          if (propertyValue != null && !propertyValue.isBlank()) {
            overrides.put(environmentName, propertyValue.strip());
          }
        });
    return Map.copyOf(overrides);
  }

  private static void applyDeploymentValues(Builder builder) {
    value(ENDPOINT_PROPERTY, ENDPOINT_ENV).map(URI::create).ifPresent(builder::endpoint);
    value(API_KEY_PROPERTY, API_KEY_ENV).ifPresent(builder::apiKey);
    value(OUTBOX_PROPERTY, OUTBOX_ENV).map(Path::of).ifPresent(builder::outboxDirectory);
  }

  private static Optional<String> value(String property, String environment) {
    String propertyValue = System.getProperty(property);
    if (propertyValue != null && !propertyValue.isBlank()) {
      return Optional.of(propertyValue.strip());
    }
    String environmentValue = System.getenv(environment);
    return environmentValue == null || environmentValue.isBlank()
        ? Optional.empty()
        : Optional.of(environmentValue.strip());
  }

  private static Optional<String> optionalText(Optional<String> value, String name) {
    return Objects.requireNonNull(value, name).map(text -> requireText(text, name));
  }

  private static String requireText(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value.strip();
  }

  private static void validateEndpoint(URI endpoint) {
    if (!endpoint.isAbsolute()
        || endpoint.getHost() == null
        || !(endpoint.getScheme().equalsIgnoreCase("http")
            || endpoint.getScheme().equalsIgnoreCase("https"))
        || endpoint.getUserInfo() != null
        || endpoint.getQuery() != null
        || endpoint.getFragment() != null) {
      throw new IllegalArgumentException(
          "endpoint must be an absolute HTTP(S) URI without user-info, query, or fragment");
    }
  }

  /** Builder for explicit room configuration. */
  private static final class Builder {
    private final String roomId;
    private URI endpoint;
    private String apiKey;
    private Path outboxDirectory = Path.of("tracking-outbox");
    private String operatorEmail = DEFAULT_OPERATOR_EMAIL;

    private Builder(String roomId) {
      this.roomId = requireText(roomId, "roomId");
    }

    /**
     * Sets the optional self-hosted HTTP endpoint.
     *
     * @param endpoint absolute HTTP(S) base URI
     * @return this builder
     */
    private Builder endpoint(URI endpoint) {
      this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
      return this;
    }

    /**
     * Sets the optional bearer credential.
     *
     * @param apiKey bearer credential
     * @return this builder
     */
    private Builder apiKey(String apiKey) {
      this.apiKey = requireText(apiKey, "apiKey");
      return this;
    }

    /**
     * Sets the local outbox directory.
     *
     * @param outboxDirectory local directory
     * @return this builder
     */
    private Builder outboxDirectory(Path outboxDirectory) {
      this.outboxDirectory = Objects.requireNonNull(outboxDirectory, "outboxDirectory");
      return this;
    }

    /**
     * Sets the operator email for failed-upload recovery.
     *
     * @param operatorEmail operator email
     * @return this builder
     */
    private Builder operatorEmail(String operatorEmail) {
      this.operatorEmail = requireText(operatorEmail, "operatorEmail");
      return this;
    }

    /**
     * Builds the immutable configuration.
     *
     * @return validated tracking configuration
     */
    private TrackingConfig build() {
      return new TrackingConfig(
          roomId,
          Optional.ofNullable(endpoint),
          Optional.ofNullable(apiKey),
          outboxDirectory,
          operatorEmail);
    }
  }
}
