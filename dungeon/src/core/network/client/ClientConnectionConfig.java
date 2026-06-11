package core.network.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Connection settings for a multiplayer client.
 *
 * @param host server hostname or IP address
 * @param port server TCP/UDP port
 */
public record ClientConnectionConfig(String host, int port) {
  private static final String HOST_ERROR_MESSAGE = "Ungültige IP-Adresse oder ungültiger Hostname.";
  private static final String PORT_ERROR_MESSAGE = "Ungültiger Port.";
  private static final Pattern HOSTNAME_PATTERN =
      Pattern.compile(
          "(?=.{1,253}\\.?$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)"
              + "(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*\\.?");
  private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("[0-9]+");
  private static final Pattern IPV4_CANDIDATE_PATTERN = Pattern.compile("[0-9.]+");

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
      throw new IllegalArgumentException(PORT_ERROR_MESSAGE, e);
    }
  }

  private static String validateHost(String host) {
    String trimmed = Objects.requireNonNullElse(host, "").trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
    }
    if (trimmed.contains("/")) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
    }
    if (trimmed.contains(":")) {
      return validateIpv6Literal(trimmed);
    }
    if (IPV4_CANDIDATE_PATTERN.matcher(trimmed).matches() && trimmed.contains(".")) {
      return validateIpv4Literal(trimmed);
    }
    if (DIGITS_ONLY_PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
    }
    if (!HOSTNAME_PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
    }
    return trimmed;
  }

  private static String validateIpv4Literal(String host) {
    String[] octets = host.split("\\.", -1);
    if (octets.length != 4) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
    }
    for (String octet : octets) {
      if (octet.isEmpty()) {
        throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
      }
      int value;
      try {
        value = Integer.parseInt(octet);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(HOST_ERROR_MESSAGE, e);
      }
      if (value > 255) {
        throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
      }
    }
    return host;
  }

  private static String validateIpv6Literal(String host) {
    try {
      InetAddress address = InetAddress.getByName(host);
      if (address.getHostAddress().contains(":")) {
        return host;
      }
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException(HOST_ERROR_MESSAGE, e);
    }
    throw new IllegalArgumentException(HOST_ERROR_MESSAGE);
  }

  private static void validatePort(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException(PORT_ERROR_MESSAGE);
    }
  }
}
