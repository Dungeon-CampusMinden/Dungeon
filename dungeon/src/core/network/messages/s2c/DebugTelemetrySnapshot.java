package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;
import java.util.Objects;

/**
 * Authoritative server-side debug telemetry snapshot.
 *
 * @param requestId echoed request identifier
 * @param serverTimeMs server wall-clock snapshot time in milliseconds
 * @param transport transport counters
 * @param debugTransport debug-protocol transport counters
 * @param udp UDP failure and fallback counters
 * @param snapshots snapshot counters
 * @param windows rolling-window counters
 * @param clients per-client telemetry values
 */
public record DebugTelemetrySnapshot(
    long requestId,
    long serverTimeMs,
    Transport transport,
    Transport debugTransport,
    Udp udp,
    Snapshots snapshots,
    Windows windows,
    List<Client> clients)
    implements NetworkMessage {

  /**
   * Creates an immutable telemetry snapshot.
   *
   * @param clients per-client telemetry values
   */
  public DebugTelemetrySnapshot {
    transport = Objects.requireNonNull(transport, "transport");
    debugTransport = Objects.requireNonNull(debugTransport, "debugTransport");
    udp = Objects.requireNonNull(udp, "udp");
    snapshots = Objects.requireNonNull(snapshots, "snapshots");
    windows = Objects.requireNonNull(windows, "windows");
    clients = List.copyOf(Objects.requireNonNull(clients, "clients"));
  }

  /**
   * Transport message and byte counters.
   *
   * @param tcpOutboundMessages TCP outbound message count
   * @param tcpOutboundBytes TCP outbound byte count
   * @param tcpInboundMessages TCP inbound message count
   * @param tcpInboundBytes TCP inbound byte count
   * @param udpOutboundMessages UDP outbound message count
   * @param udpOutboundBytes UDP outbound byte count
   * @param udpInboundMessages UDP inbound message count
   * @param udpInboundBytes UDP inbound byte count
   */
  public record Transport(
      long tcpOutboundMessages,
      long tcpOutboundBytes,
      long tcpInboundMessages,
      long tcpInboundBytes,
      long udpOutboundMessages,
      long udpOutboundBytes,
      long udpInboundMessages,
      long udpInboundBytes) {

    /**
     * Returns an empty transport counter snapshot.
     *
     * @return empty transport counters
     */
    public static Transport empty() {
      return new Transport(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }
  }

  /**
   * UDP failure and fallback counters.
   *
   * @param fallbacks unreliable messages routed through TCP fallback
   * @param oversizedPackets UDP payloads rejected because they exceeded the safe MTU
   * @param sendFailures UDP send attempts that failed before fallback
   * @param droppedPackets inbound UDP packets dropped before dispatch
   * @param lastFallbackReason last UDP fallback reason
   * @param lastDropReason last inbound UDP drop reason
   * @param lastFailureReason last UDP send failure reason
   */
  public record Udp(
      long fallbacks,
      long oversizedPackets,
      long sendFailures,
      long droppedPackets,
      String lastFallbackReason,
      String lastDropReason,
      String lastFailureReason) {
    /**
     * Creates a UDP counter snapshot.
     *
     * @param lastFallbackReason last UDP fallback reason
     * @param lastDropReason last inbound UDP drop reason
     * @param lastFailureReason last UDP send failure reason
     */
    public Udp {
      lastFallbackReason = Objects.requireNonNullElse(lastFallbackReason, "n/a");
      lastDropReason = Objects.requireNonNullElse(lastDropReason, "n/a");
      lastFailureReason = Objects.requireNonNullElse(lastFailureReason, "n/a");
    }
  }

  /**
   * Snapshot transmission counters.
   *
   * @param fullSent full snapshots sent to clients
   * @param deltaSent delta snapshots sent to clients
   * @param lastFullTick server tick of the last full snapshot
   * @param lastFullBytes serialized byte size of the last full snapshot
   * @param lastFullEntities entity count of the last full snapshot
   * @param lastDeltaTick server tick of the last delta snapshot
   * @param lastDeltaBytes serialized byte size of the last delta snapshot
   * @param lastDeltaEntityDeltas entity delta count of the last delta snapshot
   * @param lastDeltaRemovals removal count of the last delta snapshot
   * @param lastBuildMicros last authoritative snapshot build duration in microseconds
   */
  public record Snapshots(
      long fullSent,
      long deltaSent,
      int lastFullTick,
      int lastFullBytes,
      int lastFullEntities,
      int lastDeltaTick,
      int lastDeltaBytes,
      int lastDeltaEntityDeltas,
      int lastDeltaRemovals,
      long lastBuildMicros) {}

  /**
   * Rolling-window counters.
   *
   * @param transportOutBytesLastSecond transport outbound bytes over the last second
   * @param transportOutBytesLastFiveSeconds transport outbound bytes over the last five seconds
   * @param snapshotsSentLastSecond snapshots sent over the last second
   * @param snapshotsSentLastFiveSeconds snapshots sent over the last five seconds
   */
  public record Windows(
      long transportOutBytesLastSecond,
      long transportOutBytesLastFiveSeconds,
      long snapshotsSentLastSecond,
      long snapshotsSentLastFiveSeconds) {}

  /**
   * Per-client server telemetry values.
   *
   * @param clientId client id
   * @param udpReady whether the server currently considers UDP ready for this client
   * @param rttEstimateMs server-side RTT estimate, or -1 when unknown
   * @param lastActivityAgeMs age of the latest client activity in milliseconds
   * @param latestAckedSnapshotTick latest snapshot tick acknowledged by this client
   */
  public record Client(
      short clientId,
      boolean udpReady,
      float rttEstimateMs,
      long lastActivityAgeMs,
      int latestAckedSnapshotTick) {}
}
