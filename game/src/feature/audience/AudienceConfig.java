package feature.audience;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Settings of an audience-mode room.
 *
 * <p>The relay URL comes from the build: {@code DUNGEON_AUDIENCE_RELAY} at build time is embedded
 * in {@code audience.properties}. The system property {@code dungeon.audience.relay} or the
 * environment variable {@code DUNGEON_AUDIENCE_RELAY} at launch override it, for example to test
 * against a local relay. Without any of them, audience mode uses a relay on this machine at {@link
 * #LOCAL_RELAY}.
 *
 * <p>A public relay only lets games with its key create sessions. The key never goes into the
 * build; the game reads it at launch from the system property {@code dungeon.audience.relayKey} or
 * the environment variable {@code DUNGEON_AUDIENCE_RELAY_KEY}.
 *
 * @param relay base URL of the relay, the page the audience opens, such as {@code
 *     https://example.org/play}
 * @param relayKey key the relay requires to create a session, if it requires one
 * @param moveWindow voting window for a movement step; it starts with the first vote
 * @param puzzleWindow voting window for a puzzle answer; it starts when the puzzle opens
 * @param continueWindow longest wait before a text dialog closes; it closes earlier once a majority
 *     of the voting players pressed "Weiter"
 * @param countdown countdown between the host's start and the first window
 * @param resultDisplay how long the audience shows the distribution of a closed window
 * @param closeAfterEnd the session closes on its own this long after the game ended
 */
public record AudienceConfig(
    URI relay,
    Optional<String> relayKey,
    Duration moveWindow,
    Duration puzzleWindow,
    Duration continueWindow,
    Duration countdown,
    Duration resultDisplay,
    Duration closeAfterEnd) {

  /** Relay used when neither the build nor the launch names one. */
  public static final URI LOCAL_RELAY = URI.create("http://127.0.0.1:8090/play");

  /** Validates the settings. */
  public AudienceConfig {
    Objects.requireNonNull(relay, "relay");
    Objects.requireNonNull(relayKey, "relayKey");
    for (Duration duration :
        new Duration[] {
          moveWindow, puzzleWindow, continueWindow, countdown, resultDisplay, closeAfterEnd
        }) {
      if (duration == null || duration.isNegative() || duration.isZero()) {
        throw new IllegalArgumentException("durations must be positive");
      }
    }
  }

  /**
   * Returns the default settings: 10-second movement windows, 25-second puzzle windows, and the
   * relay and key of the build or launch.
   *
   * @return default settings
   */
  public static AudienceConfig defaults() {
    URI relay =
        Optional.ofNullable(System.getProperty("dungeon.audience.relay"))
            .or(() -> Optional.ofNullable(System.getenv("DUNGEON_AUDIENCE_RELAY")))
            .or(AudienceConfig::buildRelay)
            .filter(value -> !value.isBlank())
            .map(URI::create)
            .orElse(LOCAL_RELAY);
    Optional<String> relayKey =
        Optional.ofNullable(System.getProperty("dungeon.audience.relayKey"))
            .or(() -> Optional.ofNullable(System.getenv("DUNGEON_AUDIENCE_RELAY_KEY")))
            .map(String::strip)
            .filter(value -> !value.isEmpty());
    return new AudienceConfig(
        relay,
        relayKey,
        Duration.ofSeconds(10),
        Duration.ofSeconds(25),
        Duration.ofSeconds(15),
        Duration.ofSeconds(5),
        Duration.ofSeconds(5),
        Duration.ofMinutes(15));
  }

  // Reads the relay that game/build.gradle writes into audience.properties.
  private static Optional<String> buildRelay() {
    try (InputStream input = AudienceConfig.class.getResourceAsStream("/audience.properties")) {
      if (input == null) return Optional.empty();
      Properties properties = new Properties();
      properties.load(input);
      return Optional.ofNullable(properties.getProperty("relay")).filter(value -> !value.isBlank());
    } catch (IOException exception) {
      return Optional.empty();
    }
  }
}
