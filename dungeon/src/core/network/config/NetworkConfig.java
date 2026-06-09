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
  public static SnapshotTranslator SNAPSHOT_TRANSLATOR = new DefaultSnapshotTranslator();

  /**
   * Strategy for converting entities into spawn messages.
   *
   * <p>The default strategy requires {@code PositionComponent} and {@code DrawComponent}.
   * Subprojects may replace this to support data-only entities.
   */
  public static EntitySpawnStrategy ENTITY_SPAWN_STRATEGY = new DefaultEntitySpawnStrategy();

  /** Maximum size of serialized payload for TCP communication, in bytes. */
  public static final int MAX_TCP_OBJECT_SIZE = 1 << 20; // 1 MiB

  /** Maximum size of serialized payload for UDP communication, in bytes. */
  public static final int MAX_UDP_OBJECT_SIZE = 1 << 15; // 32 KiB

  /** Safe UDP Maximum Transmission Unit (MTU) size to avoid fragmentation, in bytes. */
  public static final int SAFE_UDP_MTU = 1400;

  /** Protocol version used by multiplayer clients and servers during the connection handshake. */
  public static final short PROTOCOL_VERSION = 2;

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

  /** Initial delay before the next UDP retry attempt, in milliseconds. */
  public static final int UDP_RETRY_INITIAL_DELAY_MS = 500;

  /** Multiplier applied to the UDP retry delay after each failed retry cycle. */
  public static final int UDP_RETRY_MULTIPLIER = 2;

  /** Maximum delay between UDP retry attempts, in milliseconds. */
  public static final int UDP_RETRY_MAX_DELAY_MS = 120_000;

  /** Interval for UDP keepalive re-registration while UDP is healthy, in milliseconds. */
  public static final int UDP_KEEPALIVE_INTERVAL_MS = 2_000;

  /** Time without a successful UDP acknowledgement after which UDP is considered stale. */
  public static final int UDP_STALE_AFTER_MS = 4_500;

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

  /** Timeout for TCP connection attempts, in milliseconds. */
  public static final int TCP_CONNECT_TIMEOUT_MS = 2_500;

  /**
   * Server tick rate, in Hertz (Hz).
   *
   * <p>This defines how many times per second the server updates the game state.
   */
  public static final int SERVER_TICK_HZ = 60;

  /**
   * Server delta snapshot rate, in Hertz (Hz).
   *
   * <p>Delta snapshots are sent between full baseline snapshots and contain only changed fields.
   */
  public static final int SERVER_DELTA_SNAPSHOT_HZ = 60;

  /**
   * Interval between full baseline snapshots, in server ticks.
   *
   * <p>Full snapshots are sent reliably and provide the baseline for delta snapshots.
   */
  public static final int FULL_SNAPSHOT_INTERVAL_TICKS = SERVER_TICK_HZ * 6;

  /**
   * Minimum retry interval for recovery full snapshots, in server ticks.
   *
   * <p>Reliable full snapshots are used for connect, reconnect, level-change, and missing-baseline
   * recovery. Retrying faster than this creates avoidable TCP bursts while the previous full
   * snapshot is still in flight.
   */
  public static final int FULL_SNAPSHOT_RECOVERY_RETRY_INTERVAL_TICKS = SERVER_TICK_HZ;

  /**
   * Number of full snapshots retained server-side for delta baselines.
   *
   * <p>This must comfortably exceed the normal full-baseline interval so an acknowledged baseline
   * stays available during stable play instead of falling back to repeated full snapshots.
   */
  public static final int SERVER_DELTA_HISTORY_SIZE = SERVER_TICK_HZ * 10;

  /**
   * Number of fully applied snapshots retained client-side for delta materialization.
   *
   * <p>This mirrors the server-side retention so a normal client can keep materializing deltas
   * across the steady-state full-baseline window.
   */
  public static final int CLIENT_DELTA_HISTORY_SIZE = SERVER_TICK_HZ * 10;

  /**
   * Delay before sending an explicit reliable snapshot acknowledgement.
   *
   * <p>This gives normal client input messages a short window to piggyback the latest applied
   * snapshot tick via {@code InputMessage.lastSnapshotTick}. When no recent input carries that
   * acknowledgement, the client still sends a coalesced reliable {@code SnapshotAck}.
   */
  public static final int SNAPSHOT_ACK_EXPLICIT_DELAY_MS = 100;

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
