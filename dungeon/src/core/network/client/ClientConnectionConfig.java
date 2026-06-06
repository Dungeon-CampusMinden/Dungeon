package core.network.client;

import java.util.Objects;

/**
 * Connection settings for a multiplayer client.
 *
 * @param host server hostname or IP address
 * @param port server TCP/UDP port
 */
public record ClientConnectionConfig(String host, int port) {
  /** Default host used when the dialog host field is empty. */
  public static final String DEFAULT_HOST = "127.0.0.1";

  /** Default port used when the dialog port field is empty. */
  public static final int DEFAULT_PORT = 7777;

  /**
   * Creates a validated client connection config.
   *
   * @param host server hostname or IP address
   * @param port server TCP/UDP port
   */
  public ClientConnectionConfig {
    host = validateHost(host);
    validatePort(port);
  }

  /**
   * Parses dialog field values using the default host and port.
   *
   * @param hostField raw host field value
   * @param portField raw port field value
   * @return validated connection config
   * @throws IllegalArgumentException if host or port are invalid
   */
  public static ClientConnectionConfig fromFields(String hostField, String portField) {
    return fromFields(hostField, portField, DEFAULT_HOST, DEFAULT_PORT);
  }

  /**
   * Parses dialog field values using explicit fallback values.
   *
   * @param hostField raw host field value
   * @param portField raw port field value
   * @param defaultHost fallback host used when hostField is empty
   * @param defaultPort fallback port used when portField is empty
   * @return validated connection config
   * @throws IllegalArgumentException if host or port are invalid
   */
  public static ClientConnectionConfig fromFields(
      String hostField, String portField, String defaultHost, int defaultPort) {
    String parsedHost = fieldOrDefault(hostField, defaultHost);
    int parsedPort = parsePort(portField, defaultPort);
    return new ClientConnectionConfig(parsedHost, parsedPort);
  }

  private static String fieldOrDefault(String fieldValue, String defaultValue) {
    String trimmed = fieldValue == null ? "" : fieldValue.trim();
    if (!trimmed.isEmpty()) {
      return trimmed;
    }
    return Objects.requireNonNullElse(defaultValue, "");
  }

  private static int parsePort(String portField, int defaultPort) {
    String trimmed = portField == null ? "" : portField.trim();
    if (trimmed.isEmpty()) {
      return defaultPort;
    }
    try {
      return Integer.parseInt(trimmed);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Port muss eine Zahl sein.", e);
    }
  }

  private static String validateHost(String host) {
    String trimmed = Objects.requireNonNullElse(host, "").trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Host darf nicht leer sein.");
    }
    return trimmed;
  }

  private static void validatePort(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("Port muss zwischen 1 und 65535 liegen.");
    }
  }
}
