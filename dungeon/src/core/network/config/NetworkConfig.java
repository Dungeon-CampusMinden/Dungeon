package core.network.config;

public final class NetworkConfig {
  private NetworkConfig() {}

  // Max size of serialized payload for TCP (1 MiB)
  public static final int MAX_TCP_OBJECT_SIZE = 1 << 20;

  // Conservative UDP MTU to avoid fragmentation
  public static final int SAFE_UDP_MTU = 1400;

  // UDP client registration attempts and interval
  public static final int UDP_REGISTER_ATTEMPTS = 5;
  public static final int UDP_REGISTER_INTERVAL_MS = 500;

  // TCP length-field frame settings
  public static final int TCP_LENGTH_FIELD_OFFSET = 0;
  public static final int TCP_LENGTH_FIELD_LENGTH = 4;
  public static final int TCP_LENGTH_ADJUSTMENT = 0;
  public static final int TCP_INITIAL_BYTES_TO_STRIP = 4;

  // Server tick rates
  public static final int SERVER_TICK_HZ = 60;
  public static final int SERVER_SNAPSHOT_HZ = 60;

  public static final int MAX_SEQUENCE_GAP = 32;

  public static final int SESSION_TOKEN_LENGTH_BYTES = 24; // 192 bits
}
