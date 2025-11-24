package core.network.config;

import core.network.DefaultSnapshotTranslator;
import core.network.SnapshotTranslator;

/**
 * Configuration class for network-related settings.
 *
 * <p>This class contains constants used for configuring network behavior, such as snapshot
 * translation, payload sizes, UDP settings, and server tick rates.
 */
public final class NetworkConfig {

  // Private constructor to prevent instantiation
  private NetworkConfig() {}

  /**
   * Translator for converting game state snapshots to/from byte arrays. Used by both client and
   * server.
   */
  public static final SnapshotTranslator SNAPSHOT_TRANSLATOR = new DefaultSnapshotTranslator();

  /** Maximum size of serialized payload for TCP communication, in bytes. */
  public static final int MAX_TCP_OBJECT_SIZE = 1 << 20; // 1 MiB

  /** Maximum size of serialized payload for UDP communication, in bytes. */
  public static final int MAX_UDP_OBJECT_SIZE = 1 << 15; // 32 KiB

  /** Safe UDP Maximum Transmission Unit (MTU) size to avoid fragmentation, in bytes. */
  public static final int SAFE_UDP_MTU = 1400;

  /**
   * Number of attempts for UDP client registration.
   *
   * <p>This defines how many times the client will attempt to register with the server over UDP
   * before giving up.
   */
  public static final int UDP_REGISTER_ATTEMPTS = 5;

  /**
   * Interval between UDP client registration attempts, in milliseconds.
   *
   * <p>This defines how often the client will send registration requests to the server over UDP.
   */
  public static final int UDP_REGISTER_INTERVAL_MS = 500;

  /** Offset for the length field in TCP frames, in bytes. */
  public static final int TCP_LENGTH_FIELD_OFFSET = 0;

  /**
   * Length of the length field in TCP frames, in bytes.
   *
   * <p>This indicates that the length field occupies 4 bytes at the start of each TCP frame.
   */
  public static final int TCP_LENGTH_FIELD_LENGTH = 4;

  /**
   * Adjustment value for the length field in TCP frames.
   *
   * <p>This is used to account for any additional bytes that need to be considered when calculating
   * the total length of a TCP frame.
   */
  public static final int TCP_LENGTH_ADJUSTMENT = 0;

  /**
   * Number of initial bytes to strip from TCP frames.
   *
   * <p>This is used to remove the length field from the beginning of TCP frames after decoding.
   */
  public static final int TCP_INITIAL_BYTES_TO_STRIP = 4;

  /**
   * Server tick rate, in Hertz (Hz).
   *
   * <p>This defines how many times per second the server updates the game state.
   */
  public static final int SERVER_TICK_HZ = 60;

  /**
   * Server snapshot rate, in Hertz (Hz).
   *
   * <p>This defines how many times per second the server sends game state snapshots to clients.
   */
  public static final int SERVER_SNAPSHOT_HZ = 60;

  /**
   * Maximum allowed sequence gap for network packets.
   *
   * <p>This defines the maximum difference between expected and received packet sequence numbers
   * before considering packets as lost or out of order.
   */
  public static final int MAX_SEQUENCE_GAP = 32;

  /** Length of the session token, in bytes. */
  public static final int SESSION_TOKEN_LENGTH_BYTES = 24; // 192 bits
}
