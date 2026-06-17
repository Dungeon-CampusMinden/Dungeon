package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client-to-server request for authoritative debug telemetry.
 *
 * @param requestId client-generated request identifier
 * @param mode request mode
 * @param intervalMs requested stream interval in milliseconds
 */
public record DebugTelemetryRequest(long requestId, Mode mode, int intervalMs)
    implements NetworkMessage {

  /**
   * Creates a validated debug telemetry request.
   *
   * @param requestId client-generated request identifier
   * @param mode request mode
   * @param intervalMs requested stream interval in milliseconds
   */
  public DebugTelemetryRequest {
    mode = mode == null ? Mode.ONCE : mode;
  }

  /** Debug telemetry request mode. */
  public enum Mode {
    /** Send one telemetry snapshot. */
    ONCE,
    /** Start periodic telemetry snapshots. */
    START_STREAM,
    /** Stop periodic telemetry snapshots. */
    STOP_STREAM
  }
}
