package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;
import java.util.Objects;

/**
 * Authoritative server-side debug telemetry snapshot.
 *
 * @param requestId echoed request identifier
 * @param serverTimeMs server wall-clock snapshot time in milliseconds
 * @param serverTimeNanos server monotonic capture time
 * @param transport transport counters
 * @param debugTransport debug-protocol transport counters
 * @param udp UDP failure and fallback counters
 * @param snapshots snapshot counters
 * @param windows rolling-window counters
 * @param timings timing diagnostics
 * @param clients per-client telemetry values
 */
public record DebugTelemetrySnapshot(
    long requestId,
    long serverTimeMs,
    long serverTimeNanos,
    Transport transport,
    Transport debugTransport,
    Udp udp,
    Snapshots snapshots,
    Windows windows,
    Timings timings,
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
    timings = Objects.requireNonNull(timings, "timings");
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
   * @param lastFullReason reason for the last full snapshot
   * @param staleFullBytes serialized bytes of stale full snapshots dropped by clients
   * @param periodicFullSent full snapshots sent for periodic baselines
   * @param fallbackFullSent full snapshots sent for fallback/resync reasons
   * @param missingBaselineFullFallbacks full snapshots sent because baseline history was missing
   * @param historyServerTick server tick of the latest history sample
   * @param historySize number of retained snapshots
   * @param historyCapacityTicks snapshot history capacity in ticks
   * @param historyCapacitySeconds snapshot history capacity in seconds
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
      long lastBuildMicros,
      String lastFullReason,
      long staleFullBytes,
      long periodicFullSent,
      long fallbackFullSent,
      long missingBaselineFullFallbacks,
      int historyServerTick,
      int historySize,
      int historyCapacityTicks,
      double historyCapacitySeconds) {
    /**
     * Creates a snapshot counter payload.
     *
     * @param lastFullReason reason for the last full snapshot
     */
    public Snapshots {
      lastFullReason = Objects.requireNonNullElse(lastFullReason, "n/a");
    }
  }

  /**
   * Rolling-window counters.
   *
   * @param transportOutBytesLastSecond transport outbound bytes over the last second
   * @param transportOutBytesLastFiveSeconds transport outbound bytes over the last five seconds
   * @param transportOutBytesLastThirtySeconds transport outbound bytes over the last thirty seconds
   * @param snapshotsSentLastSecond snapshots sent over the last second
   * @param snapshotsSentLastFiveSeconds snapshots sent over the last five seconds
   * @param snapshotsSentLastThirtySeconds snapshots sent over the last thirty seconds
   * @param fullSnapshotsSentLastSecond full snapshots sent over the last second
   * @param fullSnapshotsSentLastFiveSeconds full snapshots sent over the last five seconds
   * @param fullSnapshotsSentLastThirtySeconds full snapshots sent over the last thirty seconds
   * @param fullSnapshotBytesLastSecond full-snapshot bytes over the last second
   * @param fullSnapshotBytesLastFiveSeconds full-snapshot bytes over the last five seconds
   * @param fullSnapshotBytesLastThirtySeconds full-snapshot bytes over the last thirty seconds
   */
  public record Windows(
      long transportOutBytesLastSecond,
      long transportOutBytesLastFiveSeconds,
      long transportOutBytesLastThirtySeconds,
      long snapshotsSentLastSecond,
      long snapshotsSentLastFiveSeconds,
      long snapshotsSentLastThirtySeconds,
      long fullSnapshotsSentLastSecond,
      long fullSnapshotsSentLastFiveSeconds,
      long fullSnapshotsSentLastThirtySeconds,
      long fullSnapshotBytesLastSecond,
      long fullSnapshotBytesLastFiveSeconds,
      long fullSnapshotBytesLastThirtySeconds) {}

  /**
   * Timing diagnostics for receive/decode/dispatch/frame/GC paths.
   *
   * @param lastTcpDecodeType message type of the last TCP decode
   * @param lastTcpDecodeMicros duration of the last TCP decode
   * @param maxTcpDecodeMicrosLastTenSeconds max TCP decode duration in the last ten seconds
   * @param maxTcpDecodeTypeLastTenSeconds message type for the max TCP decode
   * @param lastQueueAgeMicros last message queue age
   * @param maxQueueAgeMicrosLastTenSeconds max queue age in the last ten seconds
   * @param maxQueueAgeTypeLastTenSeconds message type for the max queue age
   * @param lastDispatchMicros last message dispatch duration
   * @param maxDispatchMicrosLastTenSeconds max dispatch duration in the last ten seconds
   * @param maxDispatchTypeLastTenSeconds message type for the max dispatch duration
   * @param lastNetworkDispatchMicros latest network dispatch batch duration
   * @param maxNetworkDispatchMicrosLastTenSeconds max network dispatch batch duration
   * @param lastFrameMicros latest frame or authoritative tick duration
   * @param maxFrameMicrosLastTenSeconds max frame/tick duration in the last ten seconds
   * @param lastGcPauseMs latest observed GC collection-time delta
   * @param maxGcPauseMsLastTenSeconds max observed GC collection-time delta in the last ten seconds
   */
  public record Timings(
      String lastTcpDecodeType,
      long lastTcpDecodeMicros,
      long maxTcpDecodeMicrosLastTenSeconds,
      String maxTcpDecodeTypeLastTenSeconds,
      long lastQueueAgeMicros,
      long maxQueueAgeMicrosLastTenSeconds,
      String maxQueueAgeTypeLastTenSeconds,
      long lastDispatchMicros,
      long maxDispatchMicrosLastTenSeconds,
      String maxDispatchTypeLastTenSeconds,
      long lastNetworkDispatchMicros,
      long maxNetworkDispatchMicrosLastTenSeconds,
      long lastFrameMicros,
      long maxFrameMicrosLastTenSeconds,
      long lastGcPauseMs,
      long maxGcPauseMsLastTenSeconds) {
    /**
     * Creates a timing diagnostics payload.
     *
     * @param lastTcpDecodeType message type of the last TCP decode
     * @param maxTcpDecodeTypeLastTenSeconds message type for the max TCP decode
     * @param maxQueueAgeTypeLastTenSeconds message type for the max queue age
     * @param maxDispatchTypeLastTenSeconds message type for the max dispatch duration
     */
    public Timings {
      lastTcpDecodeType = Objects.requireNonNullElse(lastTcpDecodeType, "n/a");
      maxTcpDecodeTypeLastTenSeconds =
          Objects.requireNonNullElse(maxTcpDecodeTypeLastTenSeconds, "n/a");
      maxQueueAgeTypeLastTenSeconds =
          Objects.requireNonNullElse(maxQueueAgeTypeLastTenSeconds, "n/a");
      maxDispatchTypeLastTenSeconds =
          Objects.requireNonNullElse(maxDispatchTypeLastTenSeconds, "n/a");
    }

    /**
     * Returns an empty timing diagnostics payload.
     *
     * @return empty timing diagnostics
     */
    public static Timings empty() {
      return new Timings(
          "n/a", -1L, -1L, "n/a", -1L, -1L, "n/a", -1L, -1L, "n/a", -1L, -1L, -1L, -1L, -1L, -1L);
    }
  }

  /**
   * Per-client server telemetry values.
   *
   * @param clientId client id
   * @param udpReady whether the server currently considers UDP ready for this client
   * @param rttEstimateMs server-side RTT estimate, or -1 when unknown
   * @param lastActivityAgeMs age of the latest client activity in milliseconds
   * @param latestAckedSnapshotTick latest snapshot tick acknowledged by this client
   * @param serverCurrentTick latest sampled server tick
   * @param ackAgeTicks age of the latest acknowledgement in server ticks
   * @param ackAgeMs age of the latest acknowledgement in milliseconds
   * @param ackBaselineInHistory whether the acknowledged baseline is still retained
   * @param historyCapacityTicks snapshot history capacity in ticks
   * @param historyCapacitySeconds snapshot history capacity in seconds
   * @param missingBaselineFullFallbacks full snapshots sent because baseline history was missing
   * @param fullSnapshotsLastSecond full snapshots sent to this client over the last second
   * @param fullSnapshotsLastFiveSeconds full snapshots sent to this client over the last five
   *     seconds
   * @param fullSnapshotsLastThirtySeconds full snapshots sent to this client over the last thirty
   *     seconds
   * @param fullSnapshotBytesLastSecond full-snapshot bytes sent over the last second
   * @param fullSnapshotBytesLastFiveSeconds full-snapshot bytes sent over the last five seconds
   * @param fullSnapshotBytesLastThirtySeconds full-snapshot bytes sent over the last thirty seconds
   * @param periodicFullSnapshots periodic full snapshots sent to this client
   * @param fallbackFullSnapshots fallback/resync full snapshots sent to this client
   * @param lastFullSnapshotReason reason for the last full snapshot sent to this client
   * @param lastFullSnapshotAgeMs age of the last full snapshot sent to this client
   * @param lastFullSnapshotTick tick of the last full snapshot sent to this client
   * @param lastFullSnapshotBytes serialized byte size of the last full snapshot sent to this client
   */
  public record Client(
      short clientId,
      boolean udpReady,
      float rttEstimateMs,
      long lastActivityAgeMs,
      int latestAckedSnapshotTick,
      int serverCurrentTick,
      int ackAgeTicks,
      long ackAgeMs,
      boolean ackBaselineInHistory,
      int historyCapacityTicks,
      double historyCapacitySeconds,
      long missingBaselineFullFallbacks,
      long fullSnapshotsLastSecond,
      long fullSnapshotsLastFiveSeconds,
      long fullSnapshotsLastThirtySeconds,
      long fullSnapshotBytesLastSecond,
      long fullSnapshotBytesLastFiveSeconds,
      long fullSnapshotBytesLastThirtySeconds,
      long periodicFullSnapshots,
      long fallbackFullSnapshots,
      String lastFullSnapshotReason,
      long lastFullSnapshotAgeMs,
      int lastFullSnapshotTick,
      int lastFullSnapshotBytes) {
    /**
     * Creates a per-client telemetry payload.
     *
     * @param lastFullSnapshotReason reason for the last full snapshot sent to this client
     */
    public Client {
      lastFullSnapshotReason = Objects.requireNonNullElse(lastFullSnapshotReason, "n/a");
    }
  }
}
