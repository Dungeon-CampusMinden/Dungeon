package audience.relay;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

/**
 * Runtime settings of the relay, read from a system property {@code dungeon.relay.<name>} or else
 * the environment variable.
 *
 * @param bindHost address the HTTP server binds to
 * @param port HTTP port
 * @param maxSessions sessions that may exist at the same time
 * @param maxParticipants devices that may join one session
 * @param idleTimeout a session whose game has not contacted the relay for this long is removed
 * @param createKey key a game must send to create a session; without one, anybody can
 */
record RelayConfig(
    String bindHost,
    int port,
    int maxSessions,
    int maxParticipants,
    Duration idleTimeout,
    Optional<String> createKey) {

  /** Request bodies larger than this are rejected; survey definitions are the largest bodies. */
  static final int MAX_BODY_BYTES = 512 * 1024;

  /** Device inputs larger than this are rejected; survey answers are the largest inputs. */
  static final int MAX_INPUT_BYTES = 32 * 1024;

  static RelayConfig load() {
    return new RelayConfig(
        setting("bindHost", "DUNGEON_RELAY_BIND_HOST").orElse("127.0.0.1"),
        integer("port", "DUNGEON_RELAY_PORT", 8090),
        integer("maxSessions", "DUNGEON_RELAY_MAX_SESSIONS", 50),
        integer("maxParticipants", "DUNGEON_RELAY_MAX_PARTICIPANTS", 300),
        Duration.ofMinutes(integer("idleMinutes", "DUNGEON_RELAY_IDLE_MINUTES", 2)),
        setting("createKey", "DUNGEON_RELAY_CREATE_KEY"));
  }

  InetSocketAddress address() {
    return new InetSocketAddress(bindHost, port);
  }

  private static int integer(String property, String environment, int defaultValue) {
    int value =
        setting(property, environment).map(Integer::parseInt).orElse(defaultValue).intValue();
    if (value < 1) throw new IllegalArgumentException(property + " must be positive");
    return value;
  }

  private static Optional<String> setting(String property, String environment) {
    String value = System.getProperty("dungeon.relay." + property);
    if (value == null) value = System.getenv(environment);
    return Optional.ofNullable(value).map(String::strip).filter(v -> !v.isEmpty());
  }
}
