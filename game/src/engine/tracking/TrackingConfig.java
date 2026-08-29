package engine.tracking;

import java.net.URI;
import java.nio.file.Path;
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
 * @param operatorContact optional operator contact shown when remote upload remains pending
 */
public record TrackingConfig(
    String roomId,
    Optional<URI> endpoint,
    Optional<String> apiKey,
    Path outboxDirectory,
    Optional<String> operatorContact) {

  public static final String ROOM_ID_PROPERTY = "dungeon.tracking.roomId";
  public static final String ENDPOINT_PROPERTY = "dungeon.tracking.endpoint";
  public static final String API_KEY_PROPERTY = "dungeon.tracking.apiKey";
  public static final String OUTBOX_PROPERTY = "dungeon.tracking.outbox";
  public static final String OPERATOR_CONTACT_PROPERTY = "dungeon.tracking.operatorContact";

  private static final String ROOM_ID_ENV = "DUNGEON_TRACKING_ROOM_ID";
  private static final String ENDPOINT_ENV = "DUNGEON_TRACKING_ENDPOINT";
  private static final String API_KEY_ENV = "DUNGEON_TRACKING_API_KEY";
  private static final String OUTBOX_ENV = "DUNGEON_TRACKING_OUTBOX";
  private static final String OPERATOR_CONTACT_ENV = "DUNGEON_TRACKING_OPERATOR_CONTACT";

  private static final Map<String, String> PROPERTY_ENVIRONMENT_MAPPING =
      Map.of(
          ROOM_ID_PROPERTY,
          ROOM_ID_ENV,
          ENDPOINT_PROPERTY,
          ENDPOINT_ENV,
          API_KEY_PROPERTY,
          API_KEY_ENV,
          OUTBOX_PROPERTY,
          OUTBOX_ENV,
          OPERATOR_CONTACT_PROPERTY,
          OPERATOR_CONTACT_ENV);

  /** Validates and normalizes one immutable configuration. */
  public TrackingConfig {
    roomId = requireText(roomId, "roomId");
    endpoint = Objects.requireNonNull(endpoint, "endpoint");
    endpoint.ifPresent(TrackingConfig::validateEndpoint);
    apiKey = optionalText(apiKey, "apiKey");
    outboxDirectory = Objects.requireNonNull(outboxDirectory, "outboxDirectory").toAbsolutePath();
    operatorContact = optionalText(operatorContact, "operatorContact");
  }

  /** Creates a builder with the required stable room ID. */
  public static Builder builder(String roomId) {
    return new Builder(roomId);
  }

  /**
   * Creates room configuration while retaining deployment settings from properties or environment.
   */
  public static TrackingConfig forRoom(String roomId) {
    Builder builder = builder(roomId);
    applyDeploymentValues(builder);
    return builder.build();
  }

  /** Reads configuration from system properties first and environment variables second. */
  public static Optional<TrackingConfig> fromEnvironment() {
    return value(ROOM_ID_PROPERTY, ROOM_ID_ENV)
        .map(
            roomId -> {
              Builder builder = builder(roomId);
              applyDeploymentValues(builder);
              return builder.build();
            });
  }

  /**
   * Copies nonblank tracking system properties into a child-process environment.
   *
   * <p>Inherited environment values remain unchanged when no nonblank property overrides them.
   * Credentials therefore stay out of command-line arguments.
   *
   * @param environment mutable child-process environment
   */
  public static void applySystemPropertiesToChildEnvironment(Map<String, String> environment) {
    Objects.requireNonNull(environment, "environment");
    PROPERTY_ENVIRONMENT_MAPPING.forEach(
        (property, environmentName) -> {
          String propertyValue = System.getProperty(property);
          if (propertyValue != null && !propertyValue.isBlank()) {
            environment.put(environmentName, propertyValue.strip());
          }
        });
  }

  private static void applyDeploymentValues(Builder builder) {
    value(ENDPOINT_PROPERTY, ENDPOINT_ENV).map(URI::create).ifPresent(builder::endpoint);
    value(API_KEY_PROPERTY, API_KEY_ENV).ifPresent(builder::apiKey);
    value(OUTBOX_PROPERTY, OUTBOX_ENV).map(Path::of).ifPresent(builder::outboxDirectory);
    value(OPERATOR_CONTACT_PROPERTY, OPERATOR_CONTACT_ENV).ifPresent(builder::operatorContact);
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
  public static final class Builder {
    private final String roomId;
    private URI endpoint;
    private String apiKey;
    private Path outboxDirectory = Path.of("tracking-outbox");
    private String operatorContact;

    private Builder(String roomId) {
      this.roomId = requireText(roomId, "roomId");
    }

    /**
     * Sets the optional self-hosted HTTP endpoint.
     *
     * @param endpoint absolute HTTP(S) base URI
     * @return this builder
     */
    public Builder endpoint(URI endpoint) {
      this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
      return this;
    }

    /**
     * Sets the optional bearer credential.
     *
     * @param apiKey bearer credential
     * @return this builder
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = requireText(apiKey, "apiKey");
      return this;
    }

    /**
     * Sets the local outbox directory.
     *
     * @param outboxDirectory local directory
     * @return this builder
     */
    public Builder outboxDirectory(Path outboxDirectory) {
      this.outboxDirectory = Objects.requireNonNull(outboxDirectory, "outboxDirectory");
      return this;
    }

    /**
     * Sets the optional operator contact for failed-upload recovery.
     *
     * @param operatorContact operator contact text
     * @return this builder
     */
    public Builder operatorContact(String operatorContact) {
      this.operatorContact = requireText(operatorContact, "operatorContact");
      return this;
    }

    /**
     * Builds the immutable configuration.
     *
     * @return validated tracking configuration
     */
    public TrackingConfig build() {
      return new TrackingConfig(
          roomId,
          Optional.ofNullable(endpoint),
          Optional.ofNullable(apiKey),
          outboxDirectory,
          Optional.ofNullable(operatorContact));
    }
  }
}
