package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client-to-server debug ping used for real RTT measurement.
 *
 * @param requestId client-generated request identifier
 * @param clientTimeNanos client monotonic timestamp from {@link java.lang.System#nanoTime()}
 * @param latestRttMs latest client-measured debug round-trip time, or negative when unknown
 */
public record DebugPing(long requestId, long clientTimeNanos, float latestRttMs)
    implements NetworkMessage {

  /**
   * Creates a debug ping without a known previous RTT sample.
   *
   * @param requestId client-generated request identifier
   * @param clientTimeNanos client monotonic timestamp from {@link java.lang.System#nanoTime()}
   */
  public DebugPing(long requestId, long clientTimeNanos) {
    this(requestId, clientTimeNanos, -1f);
  }

  /** Creates a validated debug ping. */
  public DebugPing {
    if (!Float.isFinite(latestRttMs) || latestRttMs < 0f) {
      latestRttMs = -1f;
    }
  }
}
