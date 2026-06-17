package core.network;

import core.network.config.NetworkConfig;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.DebugPing;
import core.network.messages.c2s.DebugTelemetryRequest;
import core.network.messages.s2c.DebugPong;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import core.network.server.Session;
import core.network.telemetry.NetworkTelemetryReport;
import core.network.telemetry.TelemetryLine;
import core.network.telemetry.TelemetrySection;
import core.network.telemetry.TelemetrySeverity;
import core.network.telemetry.TelemetrySpan;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe multiplayer network telemetry collector.
 *
 * <p>This class is passive diagnostics infrastructure. It records counters at transport and
 * snapshot boundaries, builds server snapshots on request, and formats client-side debug overlay
 * text. It must not change routing, reliability, snapshot contents, or simulation state.
 */
public final class NetworkTelemetry {

  private static final long NANOS_PER_MICRO = 1_000L;

  private static final List<GarbageCollectorMXBean> GC_BEANS =
      ManagementFactory.getGarbageCollectorMXBeans();

  private static final AtomicLong nextRequestId = new AtomicLong(1L);

  private static final LongAdder tcpOutboundMessages = new LongAdder();
  private static final LongAdder tcpOutboundBytes = new LongAdder();
  private static final LongAdder tcpInboundMessages = new LongAdder();
  private static final LongAdder tcpInboundBytes = new LongAdder();
  private static final LongAdder udpOutboundMessages = new LongAdder();
  private static final LongAdder udpOutboundBytes = new LongAdder();
  private static final LongAdder udpInboundMessages = new LongAdder();
  private static final LongAdder udpInboundBytes = new LongAdder();

  private static final LongAdder debugTcpOutboundMessages = new LongAdder();
  private static final LongAdder debugTcpOutboundBytes = new LongAdder();
  private static final LongAdder debugTcpInboundMessages = new LongAdder();
  private static final LongAdder debugTcpInboundBytes = new LongAdder();
  private static final LongAdder debugUdpOutboundMessages = new LongAdder();
  private static final LongAdder debugUdpOutboundBytes = new LongAdder();
  private static final LongAdder debugUdpInboundMessages = new LongAdder();
  private static final LongAdder debugUdpInboundBytes = new LongAdder();

  private static final LongAdder udpFallbacks = new LongAdder();
  private static final LongAdder udpOversizedPackets = new LongAdder();
  private static final LongAdder udpSendFailures = new LongAdder();
  private static final LongAdder udpDroppedPackets = new LongAdder();

  private static final LongAdder fullSnapshotsSent = new LongAdder();
  private static final LongAdder deltaSnapshotsSent = new LongAdder();
  private static final LongAdder fullSnapshotsApplied = new LongAdder();
  private static final LongAdder deltaSnapshotsApplied = new LongAdder();
  private static final LongAdder staleFullSnapshots = new LongAdder();
  private static final LongAdder staleDeltaSnapshots = new LongAdder();
  private static final LongAdder staleFullSnapshotBytes = new LongAdder();
  private static final LongAdder earlyStaleFullSnapshots = new LongAdder();
  private static final LongAdder earlyStaleDeltaSnapshots = new LongAdder();
  private static final LongAdder handlerStaleFullSnapshots = new LongAdder();
  private static final LongAdder handlerStaleDeltaSnapshots = new LongAdder();
  private static final LongAdder missingLocalDeltaBaselines = new LongAdder();
  private static final LongAdder clientSnapshotResyncRequests = new LongAdder();
  private static final LongAdder periodicFullSnapshotsSent = new LongAdder();
  private static final LongAdder fallbackFullSnapshotsSent = new LongAdder();
  private static final LongAdder missingBaselineFullFallbacks = new LongAdder();

  private static final RollingCounter transportOutBytesLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter transportOutBytesLastFiveSeconds = new RollingCounter(5_000L);
  private static final RollingCounter transportOutBytesLastThirtySeconds =
      new RollingCounter(30_000L);
  private static final RollingCounter snapshotsSentLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter snapshotsSentLastFiveSeconds = new RollingCounter(5_000L);
  private static final RollingCounter snapshotsSentLastThirtySeconds = new RollingCounter(30_000L);
  private static final RollingCounter fullSnapshotsSentLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter fullSnapshotsSentLastFiveSeconds = new RollingCounter(5_000L);
  private static final RollingCounter fullSnapshotsSentLastThirtySeconds =
      new RollingCounter(30_000L);
  private static final RollingCounter fullSnapshotBytesLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter fullSnapshotBytesLastFiveSeconds = new RollingCounter(5_000L);
  private static final RollingCounter fullSnapshotBytesLastThirtySeconds =
      new RollingCounter(30_000L);

  private static final RollingMax tcpDecodeMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax queueAgeMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax queueDepthLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax dispatchMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax networkDispatchMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax frameMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax gcPauseMsLastTenSeconds = new RollingMax(10_000L);

  private static final Map<Short, ClientTelemetry> serverClientTelemetry =
      new ConcurrentHashMap<>();
  private static final Map<PendingFullSnapshotKey, PendingFullSnapshot> pendingFullSnapshots =
      new ConcurrentHashMap<>();
  private static final Map<Integer, Integer> inboundFullSnapshotBytesByTick =
      new ConcurrentHashMap<>();

  private static volatile int lastFullSnapshotSentTick = -1;
  private static volatile int lastFullSnapshotSentBytes = -1;
  private static volatile int lastFullSnapshotSentEntities = -1;
  private static volatile String lastFullSnapshotSentReason = "n/a";
  private static volatile long lastFullSnapshotSentTimeMs = -1L;
  private static volatile int lastDeltaSnapshotSentTick = -1;
  private static volatile int lastDeltaSnapshotSentBytes = -1;
  private static volatile int lastDeltaSnapshotSentEntityDeltas = -1;
  private static volatile int lastDeltaSnapshotSentRemovals = -1;
  private static volatile long lastSnapshotBuildMicros = -1L;
  private static volatile int lastSnapshotHistoryServerTick = -1;
  private static volatile int lastSnapshotHistorySize = -1;
  private static volatile int lastSnapshotHistoryCapacityTicks = -1;
  private static volatile double lastSnapshotHistoryCapacitySeconds = -1.0;

  private static volatile String lastAppliedSnapshotKind = "n/a";
  private static volatile int lastAppliedSnapshotTick = -1;
  private static volatile int lastAppliedSnapshotEntities = -1;
  private static volatile int lastAppliedSnapshotRemovals = -1;
  private static volatile long lastAppliedSnapshotMicros = -1L;
  private static volatile long lastSnapshotStaleCheckMicros = -1L;
  private static volatile long lastFullSnapshotApplyMicros = -1L;
  private static volatile long lastDeltaMaterializeMicros = -1L;
  private static volatile long lastEntityReconcileMicros = -1L;
  private static volatile long lastSnapshotAckMicros = -1L;
  private static volatile boolean lastSnapshotHandlerStale;
  private static volatile String lastStaleSnapshotDropStage = "n/a";
  private static volatile String lastStaleSnapshotDropKind = "n/a";
  private static volatile int lastStaleSnapshotDropTick = -1;
  private static volatile int lastMissingLocalBaseTick = -1;
  private static volatile int lastMissingLocalDeltaTick = -1;

  private static volatile String lastUdpFallbackReason = "n/a";
  private static volatile String lastUdpDropReason = "n/a";
  private static volatile String lastUdpFailureReason = "n/a";

  private static volatile String lastTcpDecodeType = "n/a";
  private static volatile long lastTcpDecodeMicros = -1L;
  private static volatile long lastQueueAgeMicros = -1L;
  private static volatile int lastQueueDepth = -1;
  private static volatile int lastQueueDrainCount = -1;
  private static volatile long lastMessageDispatchMicros = -1L;
  private static volatile long lastNetworkDispatchMicros = -1L;
  private static volatile long lastFrameMicros = -1L;
  private static volatile long lastGcPauseMs = -1L;
  private static volatile long previousGcCollectionCount = -1L;
  private static volatile long previousGcCollectionTimeMs = -1L;

  private static volatile boolean clientConnected;
  private static volatile short clientId;
  private static volatile boolean clientUdpReady;
  private static volatile boolean clientUdpRetryMode = true;
  private static volatile long clientUdpLastAckAgeMs = -1L;
  private static volatile int clientLatestAppliedSnapshotTick = -1;
  private static volatile float latestDebugRttMs = -1f;

  private static volatile DebugTelemetrySnapshot latestServerSnapshot;
  private static volatile long latestServerSnapshotReceivedTimeMs = -1L;
  private static volatile long latestServerSnapshotClientCaptureNanos = -1L;

  private NetworkTelemetry() {}

  /**
   * Returns the next client-generated debug request identifier.
   *
   * @return a positive request id
   */
  public static long nextRequestId() {
    return nextRequestId.getAndIncrement();
  }

  /**
   * Clears all telemetry counters and last-value fields.
   *
   * <p>This is intended for tests and explicit debug-session reset only.
   */
  public static void reset() {
    reset(tcpOutboundMessages);
    reset(tcpOutboundBytes);
    reset(tcpInboundMessages);
    reset(tcpInboundBytes);
    reset(udpOutboundMessages);
    reset(udpOutboundBytes);
    reset(udpInboundMessages);
    reset(udpInboundBytes);
    reset(debugTcpOutboundMessages);
    reset(debugTcpOutboundBytes);
    reset(debugTcpInboundMessages);
    reset(debugTcpInboundBytes);
    reset(debugUdpOutboundMessages);
    reset(debugUdpOutboundBytes);
    reset(debugUdpInboundMessages);
    reset(debugUdpInboundBytes);
    reset(udpFallbacks);
    reset(udpOversizedPackets);
    reset(udpSendFailures);
    reset(udpDroppedPackets);
    reset(fullSnapshotsSent);
    reset(deltaSnapshotsSent);
    reset(fullSnapshotsApplied);
    reset(deltaSnapshotsApplied);
    reset(staleFullSnapshots);
    reset(staleDeltaSnapshots);
    reset(staleFullSnapshotBytes);
    reset(earlyStaleFullSnapshots);
    reset(earlyStaleDeltaSnapshots);
    reset(handlerStaleFullSnapshots);
    reset(handlerStaleDeltaSnapshots);
    reset(missingLocalDeltaBaselines);
    reset(clientSnapshotResyncRequests);
    reset(periodicFullSnapshotsSent);
    reset(fallbackFullSnapshotsSent);
    reset(missingBaselineFullFallbacks);
    transportOutBytesLastSecond.reset();
    transportOutBytesLastFiveSeconds.reset();
    transportOutBytesLastThirtySeconds.reset();
    snapshotsSentLastSecond.reset();
    snapshotsSentLastFiveSeconds.reset();
    snapshotsSentLastThirtySeconds.reset();
    fullSnapshotsSentLastSecond.reset();
    fullSnapshotsSentLastFiveSeconds.reset();
    fullSnapshotsSentLastThirtySeconds.reset();
    fullSnapshotBytesLastSecond.reset();
    fullSnapshotBytesLastFiveSeconds.reset();
    fullSnapshotBytesLastThirtySeconds.reset();
    tcpDecodeMicrosLastTenSeconds.reset();
    queueAgeMicrosLastTenSeconds.reset();
    queueDepthLastTenSeconds.reset();
    dispatchMicrosLastTenSeconds.reset();
    networkDispatchMicrosLastTenSeconds.reset();
    frameMicrosLastTenSeconds.reset();
    gcPauseMsLastTenSeconds.reset();
    serverClientTelemetry.clear();
    pendingFullSnapshots.clear();
    inboundFullSnapshotBytesByTick.clear();
    lastFullSnapshotSentTick = -1;
    lastFullSnapshotSentBytes = -1;
    lastFullSnapshotSentEntities = -1;
    lastFullSnapshotSentReason = "n/a";
    lastFullSnapshotSentTimeMs = -1L;
    lastDeltaSnapshotSentTick = -1;
    lastDeltaSnapshotSentBytes = -1;
    lastDeltaSnapshotSentEntityDeltas = -1;
    lastDeltaSnapshotSentRemovals = -1;
    lastSnapshotBuildMicros = -1L;
    lastSnapshotHistoryServerTick = -1;
    lastSnapshotHistorySize = -1;
    lastSnapshotHistoryCapacityTicks = -1;
    lastSnapshotHistoryCapacitySeconds = -1.0;
    lastAppliedSnapshotKind = "n/a";
    lastAppliedSnapshotTick = -1;
    lastAppliedSnapshotEntities = -1;
    lastAppliedSnapshotRemovals = -1;
    lastAppliedSnapshotMicros = -1L;
    lastSnapshotStaleCheckMicros = -1L;
    lastFullSnapshotApplyMicros = -1L;
    lastDeltaMaterializeMicros = -1L;
    lastEntityReconcileMicros = -1L;
    lastSnapshotAckMicros = -1L;
    lastSnapshotHandlerStale = false;
    lastStaleSnapshotDropStage = "n/a";
    lastStaleSnapshotDropKind = "n/a";
    lastStaleSnapshotDropTick = -1;
    lastMissingLocalBaseTick = -1;
    lastMissingLocalDeltaTick = -1;
    lastUdpFallbackReason = "n/a";
    lastUdpDropReason = "n/a";
    lastUdpFailureReason = "n/a";
    lastTcpDecodeType = "n/a";
    lastTcpDecodeMicros = -1L;
    lastQueueAgeMicros = -1L;
    lastQueueDepth = -1;
    lastQueueDrainCount = -1;
    lastMessageDispatchMicros = -1L;
    lastNetworkDispatchMicros = -1L;
    lastFrameMicros = -1L;
    lastGcPauseMs = -1L;
    previousGcCollectionCount = -1L;
    previousGcCollectionTimeMs = -1L;
    clientConnected = false;
    clientId = 0;
    clientUdpReady = false;
    clientUdpRetryMode = true;
    clientUdpLastAckAgeMs = -1L;
    clientLatestAppliedSnapshotTick = -1;
    latestDebugRttMs = -1f;
    latestServerSnapshot = null;
    latestServerSnapshotReceivedTimeMs = -1L;
    latestServerSnapshotClientCaptureNanos = -1L;
  }

  /**
   * Records a successfully queued outbound TCP message.
   *
   * @param message serialized message
   * @param bytes serialized payload size in bytes
   */
  public static void recordOutboundTcp(NetworkMessage message, int bytes) {
    recordOutboundTcp(message, bytes, (short) 0);
  }

  /**
   * Records a successfully queued outbound TCP message.
   *
   * @param message serialized message
   * @param bytes serialized payload size in bytes
   * @param peerClientId target client id on the server, or 0 when unknown/not server-side
   */
  public static void recordOutboundTcp(NetworkMessage message, int bytes, short peerClientId) {
    if (isDebugTelemetryMessage(message)) {
      debugTcpOutboundMessages.increment();
      debugTcpOutboundBytes.add(nonNegative(bytes));
      return;
    }
    tcpOutboundMessages.increment();
    tcpOutboundBytes.add(nonNegative(bytes));
    recordTransportOutBytes(bytes);
    recordSnapshotSent(message, bytes, peerClientId);
  }

  /**
   * Records a successfully queued outbound UDP message.
   *
   * @param message serialized message
   * @param bytes serialized datagram payload size in bytes
   */
  public static void recordOutboundUdp(NetworkMessage message, int bytes) {
    if (isDebugTelemetryMessage(message)) {
      debugUdpOutboundMessages.increment();
      debugUdpOutboundBytes.add(nonNegative(bytes));
      return;
    }
    udpOutboundMessages.increment();
    udpOutboundBytes.add(nonNegative(bytes));
    recordTransportOutBytes(bytes);
    recordSnapshotSent(message, bytes, (short) 0);
  }

  /**
   * Records a decoded inbound TCP message.
   *
   * @param message decoded message
   * @param bytes serialized payload size in bytes
   */
  public static void recordInboundTcp(NetworkMessage message, int bytes) {
    recordInboundTcp(message, bytes, -1L);
  }

  /**
   * Records a decoded inbound TCP message and its decode duration.
   *
   * @param message decoded message
   * @param bytes serialized payload size in bytes
   * @param decodeDurationNanos protobuf decode duration in nanoseconds, or -1 when unknown
   */
  public static void recordInboundTcp(NetworkMessage message, int bytes, long decodeDurationNanos) {
    recordTcpDecode(message, decodeDurationNanos);
    recordInboundSnapshotBytes(message, bytes);
    if (isDebugTelemetryMessage(message)) {
      debugTcpInboundMessages.increment();
      debugTcpInboundBytes.add(nonNegative(bytes));
      return;
    }
    tcpInboundMessages.increment();
    tcpInboundBytes.add(nonNegative(bytes));
  }

  /**
   * Records a decoded inbound UDP message.
   *
   * @param message decoded message
   * @param bytes serialized datagram payload size in bytes
   */
  public static void recordInboundUdp(NetworkMessage message, int bytes) {
    recordInboundSnapshotBytes(message, bytes);
    if (isDebugTelemetryMessage(message)) {
      debugUdpInboundMessages.increment();
      debugUdpInboundBytes.add(nonNegative(bytes));
      return;
    }
    udpInboundMessages.increment();
    udpInboundBytes.add(nonNegative(bytes));
  }

  private static void recordInboundSnapshotBytes(NetworkMessage message, int bytes) {
    if (message instanceof SnapshotMessage snapshot) {
      inboundFullSnapshotBytesByTick.put(snapshot.serverTick(), nonNegative(bytes));
    }
  }

  /**
   * Records a UDP payload that exceeded the safe MTU.
   *
   * @param message message that was too large
   * @param bytes serialized payload size in bytes
   */
  public static void recordUdpOversized(NetworkMessage message, int bytes) {
    udpOversizedPackets.increment();
    lastUdpFailureReason = messageName(message) + " oversized (" + nonNegative(bytes) + " B)";
  }

  /**
   * Records a UDP send failure before TCP fallback.
   *
   * @param message message that failed on UDP
   * @param reason short reason
   */
  public static void recordUdpSendFailure(NetworkMessage message, String reason) {
    udpSendFailures.increment();
    lastUdpFailureReason = messageName(message) + ": " + cleanReason(reason);
  }

  /**
   * Records an unreliable message routed through TCP fallback.
   *
   * @param reason short fallback reason
   */
  public static void recordUdpFallback(String reason) {
    udpFallbacks.increment();
    lastUdpFallbackReason = cleanReason(reason);
  }

  /**
   * Records an inbound UDP packet dropped before dispatch.
   *
   * @param reason short drop reason
   */
  public static void recordUdpDrop(String reason) {
    udpDroppedPackets.increment();
    lastUdpDropReason = cleanReason(reason);
  }

  /**
   * Records authoritative snapshot build duration.
   *
   * @param durationNanos build duration in nanoseconds
   */
  public static void recordSnapshotBuild(long durationNanos) {
    lastSnapshotBuildMicros = nanosToMicros(durationNanos);
  }

  /**
   * Records current server-side snapshot-history capacity.
   *
   * @param serverTick current authoritative snapshot tick
   * @param historySize number of retained snapshots
   * @param historyCapacityTicks maximum retained snapshots in tick-equivalent slots
   * @param tickRate server tick rate
   */
  public static void recordSnapshotHistory(
      int serverTick, int historySize, int historyCapacityTicks, int tickRate) {
    lastSnapshotHistoryServerTick = serverTick;
    lastSnapshotHistorySize = Math.max(0, historySize);
    lastSnapshotHistoryCapacityTicks = Math.max(0, historyCapacityTicks);
    lastSnapshotHistoryCapacitySeconds =
        tickRate > 0 ? lastSnapshotHistoryCapacityTicks / (double) tickRate : -1.0;
  }

  /**
   * Records per-client acknowledgement and baseline-history health.
   *
   * @param clientId client id
   * @param serverTick current server tick
   * @param ackTick latest acknowledged snapshot tick, or -1
   * @param baselineInHistory whether the acknowledged baseline is still retained
   * @param historyCapacityTicks snapshot history capacity in ticks
   * @param tickRate server tick rate
   */
  public static void recordBaselineHealth(
      short clientId,
      int serverTick,
      int ackTick,
      boolean baselineInHistory,
      int historyCapacityTicks,
      int tickRate) {
    if (clientId <= 0) {
      return;
    }
    ClientTelemetry telemetry = clientTelemetry(clientId);
    telemetry.serverCurrentTick = serverTick;
    telemetry.latestAckedSnapshotTick = ackTick;
    telemetry.ackAgeTicks = ackTick >= 0 ? Math.max(0, serverTick - ackTick) : -1;
    telemetry.ackAgeMs =
        telemetry.ackAgeTicks >= 0 && tickRate > 0
            ? Math.round(telemetry.ackAgeTicks * 1000.0 / tickRate)
            : -1L;
    telemetry.ackBaselineInHistory = baselineInHistory;
    telemetry.historyCapacityTicks = Math.max(0, historyCapacityTicks);
    telemetry.historyCapacitySeconds =
        tickRate > 0 ? telemetry.historyCapacityTicks / (double) tickRate : -1.0;
  }

  /**
   * Records that the server decided to send a full snapshot to a client.
   *
   * <p>The serialized byte count is attached later when the transport confirms the send.
   *
   * @param clientId target client id
   * @param serverTick snapshot tick
   * @param entityCount entity count in the snapshot
   * @param reason reason for sending the full snapshot
   */
  public static void recordFullSnapshotScheduled(
      short clientId, int serverTick, int entityCount, FullSnapshotSendReason reason) {
    if (clientId <= 0) {
      return;
    }
    FullSnapshotSendReason normalizedReason = normalizeReason(reason);
    pendingFullSnapshots.put(
        new PendingFullSnapshotKey(clientId, serverTick),
        new PendingFullSnapshot(
            serverTick,
            Math.max(0, entityCount),
            normalizedReason,
            java.lang.System.currentTimeMillis()));
  }

  /**
   * Clears pending full-snapshot reason telemetry when a scheduled send failed before accounting.
   *
   * @param clientId target client id
   * @param serverTick snapshot tick
   */
  public static void recordFullSnapshotSendFailed(short clientId, int serverTick) {
    if (clientId <= 0) {
      return;
    }
    pendingFullSnapshots.remove(new PendingFullSnapshotKey(clientId, serverTick));
  }

  /**
   * Records queue age and dispatch duration for a message handed to the game-thread dispatcher.
   *
   * @param message dispatched message
   * @param queueAgeNanos age between Netty receive and dispatch start
   * @param dispatchDurationNanos dispatch handler duration
   */
  public static void recordQueuedMessageDispatch(
      NetworkMessage message, long queueAgeNanos, long dispatchDurationNanos) {
    lastQueueAgeMicros = nanosToMicros(queueAgeNanos);
    lastMessageDispatchMicros = nanosToMicros(dispatchDurationNanos);
    String label = messageName(message);
    queueAgeMicrosLastTenSeconds.add(lastQueueAgeMicros, label);
    dispatchMicrosLastTenSeconds.add(lastMessageDispatchMicros, label);
  }

  /**
   * Records the inbound queue depth observed at the start of one network poll.
   *
   * @param queuedMessages messages waiting before the poll started
   */
  public static void recordInboundQueueDepth(int queuedMessages) {
    int depth = Math.max(0, queuedMessages);
    lastQueueDepth = depth;
    queueDepthLastTenSeconds.add(depth, "queue");
  }

  /**
   * Records how many inbound messages were drained during the latest network poll.
   *
   * @param drainedMessages messages removed from the inbound queue
   */
  public static void recordInboundQueueDrain(int drainedMessages) {
    lastQueueDrainCount = Math.max(0, drainedMessages);
  }

  /**
   * Records total duration spent draining network messages in one loop iteration.
   *
   * @param durationNanos network dispatch batch duration
   */
  public static void recordNetworkDispatchBatch(long durationNanos) {
    lastNetworkDispatchMicros = nanosToMicros(durationNanos);
    networkDispatchMicrosLastTenSeconds.add(lastNetworkDispatchMicros, "batch");
  }

  /**
   * Records latest frame or authoritative tick duration.
   *
   * @param durationNanos frame duration in nanoseconds
   */
  public static void recordFrameTime(long durationNanos) {
    lastFrameMicros = nanosToMicros(durationNanos);
    frameMicrosLastTenSeconds.add(lastFrameMicros, "frame");
  }

  /**
   * Records latest client render frame delta.
   *
   * @param deltaSeconds frame delta in seconds
   */
  public static void recordFrameDelta(float deltaSeconds) {
    if (deltaSeconds < 0f) {
      return;
    }
    recordFrameTime((long) (deltaSeconds * 1_000_000_000L));
  }

  /**
   * Records the timing split for a client snapshot handler.
   *
   * @param delta true when the handled message was a delta snapshot
   * @param serverTick snapshot server tick
   * @param staleCheckNanos stale-check duration
   * @param fullApplyNanos full-snapshot apply duration
   * @param deltaMaterializeNanos delta materialization duration
   * @param entityReconcileNanos entity reconciliation duration
   * @param ackSendNanos snapshot ack queueing or send duration
   * @param stale true when the snapshot was dropped as stale
   */
  public static void recordSnapshotHandlerTiming(
      boolean delta,
      int serverTick,
      long staleCheckNanos,
      long fullApplyNanos,
      long deltaMaterializeNanos,
      long entityReconcileNanos,
      long ackSendNanos,
      boolean stale) {
    lastSnapshotStaleCheckMicros = nanosToMicros(staleCheckNanos);
    lastFullSnapshotApplyMicros = nanosToMicros(fullApplyNanos);
    lastDeltaMaterializeMicros = nanosToMicros(deltaMaterializeNanos);
    lastEntityReconcileMicros = nanosToMicros(entityReconcileNanos);
    lastSnapshotAckMicros = nanosToMicros(ackSendNanos);
    lastSnapshotHandlerStale = stale;
  }

  /**
   * Records a snapshot accepted and applied by the client.
   *
   * @param delta true when this was a delta snapshot
   * @param serverTick snapshot server tick
   * @param entityCount number of entity states applied
   * @param removedCount number of removed entities applied
   * @param durationNanos apply duration in nanoseconds
   */
  public static void recordSnapshotApplied(
      boolean delta, int serverTick, int entityCount, int removedCount, long durationNanos) {
    if (delta) {
      deltaSnapshotsApplied.increment();
      lastAppliedSnapshotKind = "delta";
    } else {
      fullSnapshotsApplied.increment();
      lastAppliedSnapshotKind = "full";
    }
    lastAppliedSnapshotTick = serverTick;
    lastAppliedSnapshotEntities = entityCount;
    lastAppliedSnapshotRemovals = removedCount;
    lastAppliedSnapshotMicros = nanosToMicros(durationNanos);
    if (!delta) {
      inboundFullSnapshotBytesByTick.remove(serverTick);
    }
  }

  /**
   * Records a stale snapshot dropped by the client before application.
   *
   * @param delta true when this was a delta snapshot
   */
  public static void recordStaleSnapshot(boolean delta) {
    recordHandlerStaleSnapshot(delta, -1);
  }

  /**
   * Records a stale snapshot dropped by the client before application.
   *
   * @param delta true when this was a delta snapshot
   * @param serverTick stale snapshot server tick, or -1 when unknown
   */
  public static void recordStaleSnapshot(boolean delta, int serverTick) {
    recordHandlerStaleSnapshot(delta, serverTick);
  }

  /**
   * Records a stale snapshot dropped by the client before dispatch to a handler.
   *
   * @param delta true when this was a delta snapshot
   * @param serverTick stale snapshot server tick, or -1 when unknown
   */
  public static void recordEarlyStaleSnapshot(boolean delta, int serverTick) {
    recordStaleSnapshot(delta, serverTick, true);
  }

  /**
   * Records a stale snapshot dropped by the client-side snapshot handler.
   *
   * @param delta true when this was a delta snapshot
   * @param serverTick stale snapshot server tick, or -1 when unknown
   */
  public static void recordHandlerStaleSnapshot(boolean delta, int serverTick) {
    recordStaleSnapshot(delta, serverTick, false);
  }

  /**
   * Records a delta that could not be applied because its local baseline was no longer retained.
   *
   * @param missingBaseTick delta baseline tick missing on this client
   * @param deltaTick delta snapshot tick that referenced the missing baseline
   */
  public static void recordMissingLocalDeltaBaseline(int missingBaseTick, int deltaTick) {
    missingLocalDeltaBaselines.increment();
    lastMissingLocalBaseTick = missingBaseTick;
    lastMissingLocalDeltaTick = deltaTick;
  }

  /**
   * Records a reliable client request for a recovery full snapshot.
   *
   * @param missingBaseTick delta baseline tick missing on this client
   * @param deltaTick delta snapshot tick that referenced the missing baseline
   */
  public static void recordClientSnapshotResyncRequest(int missingBaseTick, int deltaTick) {
    clientSnapshotResyncRequests.increment();
    lastMissingLocalBaseTick = missingBaseTick;
    lastMissingLocalDeltaTick = deltaTick;
  }

  private static void recordStaleSnapshot(boolean delta, int serverTick, boolean early) {
    if (delta) {
      staleDeltaSnapshots.increment();
      if (early) {
        earlyStaleDeltaSnapshots.increment();
      } else {
        handlerStaleDeltaSnapshots.increment();
      }
    } else {
      staleFullSnapshots.increment();
      if (early) {
        earlyStaleFullSnapshots.increment();
      } else {
        handlerStaleFullSnapshots.increment();
      }
      Integer bytes = inboundFullSnapshotBytesByTick.remove(serverTick);
      if (bytes != null) {
        staleFullSnapshotBytes.add(nonNegative(bytes));
      }
    }
    lastStaleSnapshotDropStage = early ? "early" : "handler";
    lastStaleSnapshotDropKind = delta ? "delta" : "full";
    lastStaleSnapshotDropTick = serverTick;
  }

  /**
   * Records the latest client transport state.
   *
   * @param connected whether the client TCP transport is connected
   * @param id assigned client id, or 0 if unknown
   * @param udpReady whether UDP is currently ready
   * @param udpRetryMode whether UDP maintenance is in retry mode
   * @param udpLastAckAgeMs age of the last UDP registration ack, or -1 if unknown
   * @param latestAppliedSnapshotTick latest locally applied snapshot tick, or -1 if unknown
   */
  public static void recordClientState(
      boolean connected,
      short id,
      boolean udpReady,
      boolean udpRetryMode,
      long udpLastAckAgeMs,
      int latestAppliedSnapshotTick) {
    clientConnected = connected;
    clientId = id;
    clientUdpReady = udpReady;
    clientUdpRetryMode = udpRetryMode;
    clientUdpLastAckAgeMs = udpLastAckAgeMs;
    clientLatestAppliedSnapshotTick = latestAppliedSnapshotTick;
  }

  /**
   * Records a server telemetry snapshot received by the client.
   *
   * @param snapshot server snapshot
   */
  public static void recordServerSnapshot(DebugTelemetrySnapshot snapshot) {
    latestServerSnapshot = snapshot;
    latestServerSnapshotReceivedTimeMs = java.lang.System.currentTimeMillis();
    latestServerSnapshotClientCaptureNanos = java.lang.System.nanoTime();
  }

  /**
   * Records a debug pong received by the client and updates RTT.
   *
   * @param pong received debug pong
   */
  public static void recordDebugPong(DebugPong pong) {
    long elapsedNanos = java.lang.System.nanoTime() - pong.clientTimeNanos();
    latestDebugRttMs = Math.max(0f, elapsedNanos / 1_000_000f);
  }

  /**
   * Returns the latest client-measured debug RTT.
   *
   * @return latest debug RTT in milliseconds, or negative when unknown
   */
  public static float latestDebugRttMs() {
    return latestDebugRttMs;
  }

  /**
   * Builds an authoritative server telemetry snapshot from current counters and sessions.
   *
   * @param requestId request id to echo
   * @param sessions server sessions
   * @return telemetry snapshot
   */
  public static DebugTelemetrySnapshot buildServerSnapshot(
      long requestId, Collection<Session> sessions) {
    refreshGcTelemetry();
    long now = java.lang.System.currentTimeMillis();
    long captureNanos = java.lang.System.nanoTime();
    List<DebugTelemetrySnapshot.Client> clients = new ArrayList<>();
    for (Session session : sessions) {
      if (session == null || session.isClosed()) {
        continue;
      }
      Optional<ClientState> state = session.clientState();
      if (state.isEmpty()) {
        continue;
      }
      ClientState clientState = state.orElseThrow();
      ClientTelemetry telemetry = clientTelemetry(clientState.clientId());
      clients.add(
          new DebugTelemetrySnapshot.Client(
              clientState.clientId(),
              session.udpReady(),
              debugRttEstimate(clientState),
              Math.max(0L, now - clientState.lastActivityTimeMs()),
              clientState.snapshotSync().lastAckedSnapshotTick(),
              telemetry.serverCurrentTick,
              telemetry.ackAgeTicks,
              telemetry.ackAgeMs,
              telemetry.ackBaselineInHistory,
              telemetry.historyCapacityTicks,
              telemetry.historyCapacitySeconds,
              telemetry.missingBaselineFullFallbacks.sum(),
              telemetry.fullSnapshotsLastSecond.sum(),
              telemetry.fullSnapshotsLastFiveSeconds.sum(),
              telemetry.fullSnapshotsLastThirtySeconds.sum(),
              telemetry.fullSnapshotBytesLastSecond.sum(),
              telemetry.fullSnapshotBytesLastFiveSeconds.sum(),
              telemetry.fullSnapshotBytesLastThirtySeconds.sum(),
              telemetry.periodicFullSnapshots.sum(),
              telemetry.fallbackFullSnapshots.sum(),
              telemetry.lastFullSnapshotReason,
              telemetry.lastFullSnapshotTimeMs < 0L
                  ? -1L
                  : Math.max(0L, now - telemetry.lastFullSnapshotTimeMs),
              telemetry.lastFullSnapshotTick,
              telemetry.lastFullSnapshotBytes));
    }

    return new DebugTelemetrySnapshot(
        requestId,
        now,
        captureNanos,
        new DebugTelemetrySnapshot.Transport(
            tcpOutboundMessages.sum(),
            tcpOutboundBytes.sum(),
            tcpInboundMessages.sum(),
            tcpInboundBytes.sum(),
            udpOutboundMessages.sum(),
            udpOutboundBytes.sum(),
            udpInboundMessages.sum(),
            udpInboundBytes.sum()),
        new DebugTelemetrySnapshot.Transport(
            debugTcpOutboundMessages.sum(),
            debugTcpOutboundBytes.sum(),
            debugTcpInboundMessages.sum(),
            debugTcpInboundBytes.sum(),
            debugUdpOutboundMessages.sum(),
            debugUdpOutboundBytes.sum(),
            debugUdpInboundMessages.sum(),
            debugUdpInboundBytes.sum()),
        new DebugTelemetrySnapshot.Udp(
            udpFallbacks.sum(),
            udpOversizedPackets.sum(),
            udpSendFailures.sum(),
            udpDroppedPackets.sum(),
            lastUdpFallbackReason,
            lastUdpDropReason,
            lastUdpFailureReason),
        new DebugTelemetrySnapshot.Snapshots(
            fullSnapshotsSent.sum(),
            deltaSnapshotsSent.sum(),
            lastFullSnapshotSentTick,
            lastFullSnapshotSentBytes,
            lastFullSnapshotSentEntities,
            lastDeltaSnapshotSentTick,
            lastDeltaSnapshotSentBytes,
            lastDeltaSnapshotSentEntityDeltas,
            lastDeltaSnapshotSentRemovals,
            lastSnapshotBuildMicros,
            lastFullSnapshotSentReason,
            staleFullSnapshotBytes.sum(),
            periodicFullSnapshotsSent.sum(),
            fallbackFullSnapshotsSent.sum(),
            missingBaselineFullFallbacks.sum(),
            lastSnapshotHistoryServerTick,
            lastSnapshotHistorySize,
            lastSnapshotHistoryCapacityTicks,
            lastSnapshotHistoryCapacitySeconds),
        new DebugTelemetrySnapshot.Windows(
            transportOutBytesLastSecond.sum(),
            transportOutBytesLastFiveSeconds.sum(),
            transportOutBytesLastThirtySeconds.sum(),
            snapshotsSentLastSecond.sum(),
            snapshotsSentLastFiveSeconds.sum(),
            snapshotsSentLastThirtySeconds.sum(),
            fullSnapshotsSentLastSecond.sum(),
            fullSnapshotsSentLastFiveSeconds.sum(),
            fullSnapshotsSentLastThirtySeconds.sum(),
            fullSnapshotBytesLastSecond.sum(),
            fullSnapshotBytesLastFiveSeconds.sum(),
            fullSnapshotBytesLastThirtySeconds.sum()),
        new DebugTelemetrySnapshot.Timings(
            lastTcpDecodeType,
            lastTcpDecodeMicros,
            tcpDecodeMicrosLastTenSeconds.max().value(),
            tcpDecodeMicrosLastTenSeconds.max().label(),
            lastQueueAgeMicros,
            queueAgeMicrosLastTenSeconds.max().value(),
            queueAgeMicrosLastTenSeconds.max().label(),
            lastMessageDispatchMicros,
            dispatchMicrosLastTenSeconds.max().value(),
            dispatchMicrosLastTenSeconds.max().label(),
            lastNetworkDispatchMicros,
            networkDispatchMicrosLastTenSeconds.max().value(),
            lastFrameMicros,
            frameMicrosLastTenSeconds.max().value(),
            lastGcPauseMs,
            gcPauseMsLastTenSeconds.max().value(),
            lastQueueDepth,
            (int) queueDepthLastTenSeconds.max().value(),
            lastQueueDrainCount),
        clients);
  }

  /**
   * Builds the current structured debug overlay report.
   *
   * @return grouped telemetry report with per-span severity
   */
  public static NetworkTelemetryReport debugReport() {
    refreshGcTelemetry();
    return new NetworkTelemetryReport(List.of(clientSection(), serverSection()));
  }

  /**
   * Builds the current debug overlay text.
   *
   * @return multiline telemetry text
   */
  public static String debugText() {
    return debugReport().plainText();
  }

  private static TelemetrySection clientSection() {
    List<TelemetryLine> lines = new ArrayList<>();
    lines.add(clientStateLine());
    lines.add(clientUdpLine());
    lines.add(clientRttLine());
    lines.add(clientSnapshotsLine());
    lines.add(clientRecoveryLine());
    lines.add(clientApplyTimingsLine());
    lines.add(clientRuntimeTimingsLine());
    lines.add(clientRuntimeMaxTimingsLine());
    lines.add(clientTransportLine());
    return new TelemetrySection("Client", lines);
  }

  private static TelemetryLine clientStateLine() {
    return line(
        "State",
        text(clientConnected ? "connected" : "disconnected"),
        text(" id=" + clientId),
        text(" snap=" + formatTick(clientLatestAppliedSnapshotTick)));
  }

  private static TelemetryLine clientUdpLine() {
    TelemetrySeverity udpSeverity =
        NetworkTelemetryThresholds.badIfFalse(clientUdpReady, clientConnected);
    return line(
        "UDP",
        value(clientUdpReady ? "ready" : "fallback", udpSeverity, " (expected ready)"),
        text(" mode=" + (clientUdpRetryMode ? "retry" : "keepalive")),
        text(" ackAge="),
        valueAtMost(
            formatMillis(clientUdpLastAckAgeMs),
            clientUdpLastAckAgeMs,
            NetworkConfig.UDP_STALE_AFTER_MS,
            formatMillis(NetworkConfig.UDP_STALE_AFTER_MS),
            clientConnected));
  }

  private static TelemetryLine clientRttLine() {
    return line(
        "RTT",
        valueAtMost(
            formatRtt(latestDebugRttMs),
            latestDebugRttMs,
            NetworkTelemetryThresholds.DEBUG_RTT_MAX_MS,
            formatRtt(NetworkTelemetryThresholds.DEBUG_RTT_MAX_MS),
            true));
  }

  private static TelemetryLine clientSnapshotsLine() {
    return line(
        "Snapshots",
        text("applied full=" + fullSnapshotsApplied.sum()),
        text(" delta=" + deltaSnapshotsApplied.sum()),
        text(" stale="),
        valueExpectedZero(staleFullSnapshots.sum()),
        text("/"),
        valueExpectedZero(staleDeltaSnapshots.sum()),
        text(" early="),
        valueExpectedZero(earlyStaleFullSnapshots.sum()),
        text("/"),
        valueExpectedZero(earlyStaleDeltaSnapshots.sum()),
        text(" handler="),
        valueExpectedZero(handlerStaleFullSnapshots.sum()),
        text("/"),
        valueExpectedZero(handlerStaleDeltaSnapshots.sum()),
        text(" last=" + lastAppliedSnapshotKind + "@" + formatTick(lastAppliedSnapshotTick)),
        text(" e/r=" + formatCount(lastAppliedSnapshotEntities)),
        text("/" + formatCount(lastAppliedSnapshotRemovals)));
  }

  private static TelemetryLine clientRecoveryLine() {
    return line(
        "Recovery",
        text("missingLocalBase="),
        valueExpectedZero(missingLocalDeltaBaselines.sum()),
        text(" resyncReq="),
        valueExpectedZero(clientSnapshotResyncRequests.sum()),
        text(
            " lastMissing="
                + formatTick(lastMissingLocalBaseTick)
                + "->"
                + formatTick(lastMissingLocalDeltaTick)),
        text(" staleFullBytes="),
        valueExpectedZero(formatBytes(staleFullSnapshotBytes.sum()), staleFullSnapshotBytes.sum()));
  }

  private static TelemetryLine clientApplyTimingsLine() {
    long substepMicros = NetworkTelemetryThresholds.substepMicros();
    long fullApplyMicros = NetworkTelemetryThresholds.fullSnapshotApplyMicros();
    long lastApplyMicros = "full".equals(lastAppliedSnapshotKind) ? fullApplyMicros : substepMicros;
    return line(
        "Apply timings",
        text("last="),
        valueAtMost(
            formatMicros(lastAppliedSnapshotMicros),
            lastAppliedSnapshotMicros,
            lastApplyMicros,
            formatMicros(lastApplyMicros),
            true),
        text(" staleCheck="),
        valueAtMost(
            formatMicros(lastSnapshotStaleCheckMicros),
            lastSnapshotStaleCheckMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" fullApply="),
        valueAtMost(
            formatMicros(lastFullSnapshotApplyMicros),
            lastFullSnapshotApplyMicros,
            fullApplyMicros,
            formatMicros(fullApplyMicros),
            true),
        text(" deltaMat="),
        valueAtMost(
            formatMicros(lastDeltaMaterializeMicros),
            lastDeltaMaterializeMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" reconcile="),
        valueAtMost(
            formatMicros(lastEntityReconcileMicros),
            lastEntityReconcileMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" ackQueue="),
        valueAtMost(
            formatMicros(lastSnapshotAckMicros),
            lastSnapshotAckMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" stale="),
        value(
            Boolean.toString(lastSnapshotHandlerStale),
            lastSnapshotHandlerStale ? TelemetrySeverity.BAD : TelemetrySeverity.NORMAL,
            " (expected false)"));
  }

  private static TelemetryLine clientRuntimeTimingsLine() {
    long frameMicros = NetworkTelemetryThresholds.clientFrameMicros();
    long queueBacklogMicros = NetworkTelemetryThresholds.queueBacklogMicros();
    long substepMicros = NetworkTelemetryThresholds.substepMicros();
    int maxQueueDepth = NetworkConfig.SERVER_TICK_HZ;
    return line(
        "Runtime timings",
        text("frame="),
        valueAtMost(
            formatMicros(lastFrameMicros),
            lastFrameMicros,
            frameMicros,
            formatMicros(frameMicros),
            true),
        text(" net="),
        valueAtMost(
            formatMicros(lastNetworkDispatchMicros),
            lastNetworkDispatchMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" queue="),
        valueAtMost(
            formatMicros(lastQueueAgeMicros),
            lastQueueAgeMicros,
            queueBacklogMicros,
            formatMicros(queueBacklogMicros),
            true),
        text(" dispatch="),
        valueAtMost(
            formatMicros(lastMessageDispatchMicros),
            lastMessageDispatchMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" tcpDecode=" + lastTcpDecodeType + " "),
        valueAtMost(
            formatMicros(lastTcpDecodeMicros),
            lastTcpDecodeMicros,
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" qDepth="),
        valueAtMost(
            formatCount(lastQueueDepth),
            lastQueueDepth,
            maxQueueDepth,
            formatCount(maxQueueDepth),
            true),
        text(" drain=" + formatCount(lastQueueDrainCount)),
        text(" gc="),
        valueAtMost(
            formatMillis(lastGcPauseMs),
            lastGcPauseMs,
            NetworkTelemetryThresholds.gcPauseMs(),
            formatMillis(NetworkTelemetryThresholds.gcPauseMs()),
            true));
  }

  private static TelemetryLine clientRuntimeMaxTimingsLine() {
    RollingMax.Sample queueMax = queueAgeMicrosLastTenSeconds.max();
    RollingMax.Sample dispatchMax = dispatchMicrosLastTenSeconds.max();
    RollingMax.Sample tcpDecodeMax = tcpDecodeMicrosLastTenSeconds.max();
    long queueBacklogMicros = NetworkTelemetryThresholds.queueBacklogMicros();
    long substepMicros = NetworkTelemetryThresholds.substepMicros();
    int maxQueueDepth = NetworkConfig.SERVER_TICK_HZ;
    return line(
        "Runtime max10",
        text("queue=" + queueMax.label() + " "),
        valueAtMost(
            formatMicros(queueMax.value()),
            queueMax.value(),
            queueBacklogMicros,
            formatMicros(queueBacklogMicros),
            true),
        text(" dispatch=" + dispatchMax.label() + " "),
        valueAtMost(
            formatMicros(dispatchMax.value()),
            dispatchMax.value(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" tcpDecode=" + tcpDecodeMax.label() + " "),
        valueAtMost(
            formatMicros(tcpDecodeMax.value()),
            tcpDecodeMax.value(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" qDepth="),
        valueAtMost(
            formatCount((int) queueDepthLastTenSeconds.max().value()),
            queueDepthLastTenSeconds.max().value(),
            maxQueueDepth,
            formatCount(maxQueueDepth),
            true));
  }

  private static TelemetryLine clientTransportLine() {
    return line(
        "Transport",
        text("out tcp=" + tcpOutboundMessages.sum() + "/" + formatBytes(tcpOutboundBytes.sum())),
        text(" udp=" + udpOutboundMessages.sum() + "/" + formatBytes(udpOutboundBytes.sum())),
        text(" in tcp=" + tcpInboundMessages.sum() + "/" + formatBytes(tcpInboundBytes.sum())),
        text(" udp=" + udpInboundMessages.sum() + "/" + formatBytes(udpInboundBytes.sum())));
  }

  private static TelemetrySection serverSection() {
    DebugTelemetrySnapshot snapshot = latestServerSnapshot;
    long receivedMs = latestServerSnapshotReceivedTimeMs;
    if (snapshot == null || receivedMs < 0L) {
      return new TelemetrySection("Server", List.of(line("Authoritative", text("n/a"))));
    }

    List<TelemetryLine> lines = new ArrayList<>();
    lines.add(serverAuthoritativeLine(snapshot, receivedMs));
    lines.add(serverSnapshotsLine(snapshot));
    lines.add(serverRecoveryLine(snapshot));
    lines.add(serverHistoryLine(snapshot));
    lines.add(serverTransportLine(snapshot));
    lines.add(serverUdpLine(snapshot));
    lines.add(serverTimingsLine(snapshot.timings()));
    lines.add(serverMaxTimingsLine(snapshot.timings()));
    snapshot.clients().forEach(client -> lines.add(serverClientLine(client)));
    return new TelemetrySection("Server", lines);
  }

  private static TelemetryLine serverAuthoritativeLine(
      DebugTelemetrySnapshot snapshot, long receivedMs) {
    long age = java.lang.System.currentTimeMillis() - receivedMs;
    long readyClients =
        snapshot.clients().stream().filter(DebugTelemetrySnapshot.Client::udpReady).count();
    return line(
        "Authoritative",
        text("age="),
        valueAtMost(
            formatMillis(age),
            age,
            NetworkTelemetryThresholds.SERVER_SNAPSHOT_STALE_AFTER_MS,
            formatMillis(NetworkTelemetryThresholds.SERVER_SNAPSHOT_STALE_AFTER_MS),
            true),
        text(" clients=" + snapshot.clients().size()),
        text(" udp=" + readyClients + "/" + snapshot.clients().size()));
  }

  private static TelemetryLine serverSnapshotsLine(DebugTelemetrySnapshot snapshot) {
    DebugTelemetrySnapshot.Snapshots snapshots = snapshot.snapshots();
    return line(
        "Snapshots",
        text("full=" + snapshots.fullSent()),
        text(" lastFull=" + formatTickLabel(snapshots.lastFullTick()) + " bytes="),
        valueAtMost(
            formatBytes(snapshots.lastFullBytes()),
            snapshots.lastFullBytes(),
            NetworkTelemetryThresholds.FULL_SNAPSHOT_SOFT_MAX_BYTES,
            formatBytes(NetworkTelemetryThresholds.FULL_SNAPSHOT_SOFT_MAX_BYTES),
            true),
        text(" entities=" + formatCount(snapshots.lastFullEntities())),
        text(" delta=" + snapshots.deltaSent()),
        text(" lastDelta=" + formatTickLabel(snapshots.lastDeltaTick()) + " bytes="),
        valueAtMost(
            formatBytes(snapshots.lastDeltaBytes()),
            snapshots.lastDeltaBytes(),
            NetworkConfig.SAFE_UDP_MTU,
            formatBytes(NetworkConfig.SAFE_UDP_MTU),
            true),
        text(" deltas=" + formatCount(snapshots.lastDeltaEntityDeltas())),
        text(" removals=" + formatCount(snapshots.lastDeltaRemovals())),
        text(" build="),
        valueAtMost(
            formatMicros(snapshots.lastBuildMicros()),
            snapshots.lastBuildMicros(),
            NetworkTelemetryThresholds.substepMicros(),
            formatMicros(NetworkTelemetryThresholds.substepMicros()),
            true),
        text(" reason="),
        value(
            snapshots.lastFullReason(),
            NetworkTelemetryThresholds.badIfReason(snapshots.lastFullReason()),
            " (expected stable baseline)"),
        text(
            " rate1/5/30="
                + snapshot.windows().snapshotsSentLastSecond()
                + "/"
                + snapshot.windows().snapshotsSentLastFiveSeconds()
                + "/"
                + snapshot.windows().snapshotsSentLastThirtySeconds()));
  }

  private static TelemetryLine serverRecoveryLine(DebugTelemetrySnapshot snapshot) {
    DebugTelemetrySnapshot.Snapshots snapshots = snapshot.snapshots();
    return line(
        "Recovery",
        text(
            "periodic/recovery="
                + snapshots.periodicFullSent()
                + "/"
                + snapshots.fallbackFullSent()),
        text(" missingBase="),
        valueExpectedZero(snapshots.missingBaselineFullFallbacks()),
        text(" staleFullBytes="),
        valueExpectedZero(formatBytes(snapshots.staleFullBytes()), snapshots.staleFullBytes()));
  }

  private static TelemetryLine serverHistoryLine(DebugTelemetrySnapshot snapshot) {
    DebugTelemetrySnapshot.Snapshots snapshots = snapshot.snapshots();
    return line(
        "History",
        text("tick=" + formatTick(snapshots.historyServerTick())),
        text(" retained=" + formatCount(snapshots.historySize())),
        text("/" + formatCount(snapshots.historyCapacityTicks())),
        text(" cap=" + formatSeconds(snapshots.historyCapacitySeconds())));
  }

  private static TelemetryLine serverTransportLine(DebugTelemetrySnapshot snapshot) {
    return line(
        "Transport",
        text(
            "out tcp="
                + snapshot.transport().tcpOutboundMessages()
                + "/"
                + formatBytes(snapshot.transport().tcpOutboundBytes())),
        text(
            " udp="
                + snapshot.transport().udpOutboundMessages()
                + "/"
                + formatBytes(snapshot.transport().udpOutboundBytes())),
        text(
            " in tcp="
                + snapshot.transport().tcpInboundMessages()
                + "/"
                + formatBytes(snapshot.transport().tcpInboundBytes())),
        text(
            " udp="
                + snapshot.transport().udpInboundMessages()
                + "/"
                + formatBytes(snapshot.transport().udpInboundBytes())),
        text(
            " bytes1/5/30="
                + formatBytes(snapshot.windows().transportOutBytesLastSecond())
                + "/"
                + formatBytes(snapshot.windows().transportOutBytesLastFiveSeconds())
                + "/"
                + formatBytes(snapshot.windows().transportOutBytesLastThirtySeconds())));
  }

  private static TelemetryLine serverUdpLine(DebugTelemetrySnapshot snapshot) {
    return line(
        "UDP",
        text("fallback="),
        valueExpectedZero(snapshot.udp().fallbacks()),
        text(" oversized="),
        valueExpectedZero(snapshot.udp().oversizedPackets()),
        text(" sendFail="),
        valueExpectedZero(snapshot.udp().sendFailures()),
        text(" dropped="),
        valueExpectedZero(snapshot.udp().droppedPackets()),
        text(
            " last="
                + snapshot.udp().lastFallbackReason()
                + "/"
                + snapshot.udp().lastDropReason()
                + "/"
                + snapshot.udp().lastFailureReason()));
  }

  private static TelemetryLine serverTimingsLine(DebugTelemetrySnapshot.Timings timings) {
    long tickMicros = NetworkTelemetryThresholds.tickMicros();
    long queueBacklogMicros = NetworkTelemetryThresholds.queueBacklogMicros();
    long substepMicros = NetworkTelemetryThresholds.substepMicros();
    int maxQueueDepth = NetworkConfig.SERVER_TICK_HZ;
    return line(
        "Timings",
        text("frame="),
        valueAtMost(
            formatMicros(timings.lastFrameMicros()),
            timings.lastFrameMicros(),
            tickMicros,
            formatMicros(tickMicros),
            true),
        text(" net="),
        valueAtMost(
            formatMicros(timings.lastNetworkDispatchMicros()),
            timings.lastNetworkDispatchMicros(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" queue="),
        valueAtMost(
            formatMicros(timings.lastQueueAgeMicros()),
            timings.lastQueueAgeMicros(),
            queueBacklogMicros,
            formatMicros(queueBacklogMicros),
            true),
        text(" qDepth="),
        valueAtMost(
            formatCount(timings.lastQueueDepth()),
            timings.lastQueueDepth(),
            maxQueueDepth,
            formatCount(maxQueueDepth),
            true),
        text(" drain=" + formatCount(timings.lastQueueDrainCount())),
        text(" tcpDecode=" + timings.lastTcpDecodeType() + " "),
        valueAtMost(
            formatMicros(timings.lastTcpDecodeMicros()),
            timings.lastTcpDecodeMicros(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" gc="),
        valueAtMost(
            formatMillis(timings.lastGcPauseMs()),
            timings.lastGcPauseMs(),
            NetworkTelemetryThresholds.gcPauseMs(),
            formatMillis(NetworkTelemetryThresholds.gcPauseMs()),
            true));
  }

  private static TelemetryLine serverMaxTimingsLine(DebugTelemetrySnapshot.Timings timings) {
    long tickMicros = NetworkTelemetryThresholds.tickMicros();
    long queueBacklogMicros = NetworkTelemetryThresholds.queueBacklogMicros();
    long substepMicros = NetworkTelemetryThresholds.substepMicros();
    int maxQueueDepth = NetworkConfig.SERVER_TICK_HZ;
    return line(
        "Timings max10",
        text("frame="),
        valueAtMost(
            formatMicros(timings.maxFrameMicrosLastTenSeconds()),
            timings.maxFrameMicrosLastTenSeconds(),
            tickMicros,
            formatMicros(tickMicros),
            true),
        text(" net="),
        valueAtMost(
            formatMicros(timings.maxNetworkDispatchMicrosLastTenSeconds()),
            timings.maxNetworkDispatchMicrosLastTenSeconds(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" queue=" + timings.maxQueueAgeTypeLastTenSeconds() + " "),
        valueAtMost(
            formatMicros(timings.maxQueueAgeMicrosLastTenSeconds()),
            timings.maxQueueAgeMicrosLastTenSeconds(),
            queueBacklogMicros,
            formatMicros(queueBacklogMicros),
            true),
        text(" qDepth="),
        valueAtMost(
            formatCount(timings.maxQueueDepthLastTenSeconds()),
            timings.maxQueueDepthLastTenSeconds(),
            maxQueueDepth,
            formatCount(maxQueueDepth),
            true),
        text(" tcpDecode=" + timings.maxTcpDecodeTypeLastTenSeconds() + " "),
        valueAtMost(
            formatMicros(timings.maxTcpDecodeMicrosLastTenSeconds()),
            timings.maxTcpDecodeMicrosLastTenSeconds(),
            substepMicros,
            formatMicros(substepMicros),
            true),
        text(" gc="),
        valueAtMost(
            formatMillis(timings.maxGcPauseMsLastTenSeconds()),
            timings.maxGcPauseMsLastTenSeconds(),
            NetworkTelemetryThresholds.gcPauseMs(),
            formatMillis(NetworkTelemetryThresholds.gcPauseMs()),
            true));
  }

  private static TelemetryLine serverClientLine(DebugTelemetrySnapshot.Client client) {
    boolean ackKnown = client.latestAckedSnapshotTick() >= 0;
    return line(
        "Client " + client.clientId(),
        text("udp="),
        value(
            Boolean.toString(client.udpReady()),
            NetworkTelemetryThresholds.badIfFalse(client.udpReady(), true),
            " (expected true)"),
        text(" rtt="),
        valueAtMost(
            formatRtt(client.rttEstimateMs()),
            client.rttEstimateMs(),
            NetworkTelemetryThresholds.DEBUG_RTT_MAX_MS,
            formatRtt(NetworkTelemetryThresholds.DEBUG_RTT_MAX_MS),
            true),
        text(" activity=" + formatMillis(client.lastActivityAgeMs())),
        text(" ack=" + formatTick(client.latestAckedSnapshotTick())),
        text(" age=" + formatCount(client.ackAgeTicks()) + "t/" + formatMillis(client.ackAgeMs())),
        text(" hist="),
        value(
            Boolean.toString(client.ackBaselineInHistory()),
            NetworkTelemetryThresholds.badIfFalse(client.ackBaselineInHistory(), ackKnown),
            " (expected true)"),
        text(
            " cap="
                + formatCount(client.historyCapacityTicks())
                + "t/"
                + formatSeconds(client.historyCapacitySeconds())),
        text(" full1/5/30="),
        valueAtMost(
            Long.toString(client.fullSnapshotsLastSecond()),
            client.fullSnapshotsLastSecond(),
            1L,
            "1",
            true),
        text(
            "/"
                + client.fullSnapshotsLastFiveSeconds()
                + "/"
                + client.fullSnapshotsLastThirtySeconds()),
        text(
            " bytes="
                + formatBytes(client.fullSnapshotBytesLastSecond())
                + "/"
                + formatBytes(client.fullSnapshotBytesLastFiveSeconds())
                + "/"
                + formatBytes(client.fullSnapshotBytesLastThirtySeconds())),
        text(" missingBase="),
        valueExpectedZero(client.missingBaselineFullFallbacks()),
        text(" lastFull="),
        value(
            client.lastFullSnapshotReason(),
            NetworkTelemetryThresholds.badIfReason(client.lastFullSnapshotReason()),
            " (expected stable baseline)"),
        text("@" + formatTick(client.lastFullSnapshotTick()) + "/"),
        valueAtMost(
            formatBytes(client.lastFullSnapshotBytes()),
            client.lastFullSnapshotBytes(),
            NetworkTelemetryThresholds.FULL_SNAPSHOT_SOFT_MAX_BYTES,
            formatBytes(NetworkTelemetryThresholds.FULL_SNAPSHOT_SOFT_MAX_BYTES),
            true),
        text(" age=" + formatMillis(client.lastFullSnapshotAgeMs())));
  }

  private static TelemetryLine line(String label, TelemetrySpan... spans) {
    List<TelemetrySpan> lineSpans = new ArrayList<>();
    lineSpans.add(text(label + ": "));
    lineSpans.addAll(List.of(spans));
    return new TelemetryLine(lineSpans);
  }

  private static TelemetrySpan text(String text) {
    return new TelemetrySpan(text, TelemetrySeverity.NORMAL);
  }

  private static TelemetrySpan value(String text, TelemetrySeverity severity, String expected) {
    String suffix = severity == TelemetrySeverity.BAD ? expected : "";
    return new TelemetrySpan(text + suffix, severity);
  }

  private static TelemetrySpan valueExpectedZero(long value) {
    return valueExpectedZero(Long.toString(value), value);
  }

  private static TelemetrySpan valueExpectedZero(String text, long value) {
    return value(
        text,
        NetworkTelemetryThresholds.badIfPositive(value),
        NetworkTelemetryThresholds.expectedZero());
  }

  private static TelemetrySpan valueAtMost(
      String text, long value, long max, String formattedMax, boolean meaningful) {
    TelemetrySeverity severity =
        meaningful ? NetworkTelemetryThresholds.badIfGreater(value, max) : TelemetrySeverity.NORMAL;
    return value(text, severity, NetworkTelemetryThresholds.expectedAtMost(formattedMax));
  }

  private static TelemetrySpan valueAtMost(
      String text, double value, double max, String formattedMax, boolean meaningful) {
    TelemetrySeverity severity =
        meaningful ? NetworkTelemetryThresholds.badIfGreater(value, max) : TelemetrySeverity.NORMAL;
    return value(text, severity, NetworkTelemetryThresholds.expectedAtMost(formattedMax));
  }

  private static void reset(LongAdder adder) {
    adder.reset();
  }

  private static void recordTransportOutBytes(int bytes) {
    long value = nonNegative(bytes);
    transportOutBytesLastSecond.add(value);
    transportOutBytesLastFiveSeconds.add(value);
    transportOutBytesLastThirtySeconds.add(value);
  }

  private static void recordSnapshotSent(NetworkMessage message, int bytes, short peerClientId) {
    if (message instanceof SnapshotMessage snapshot) {
      fullSnapshotsSent.increment();
      lastFullSnapshotSentTick = snapshot.serverTick();
      lastFullSnapshotSentBytes = nonNegative(bytes);
      lastFullSnapshotSentEntities = snapshot.entities().size();
      snapshotsSentLastSecond.add(1L);
      snapshotsSentLastFiveSeconds.add(1L);
      snapshotsSentLastThirtySeconds.add(1L);
      fullSnapshotsSentLastSecond.add(1L);
      fullSnapshotsSentLastFiveSeconds.add(1L);
      fullSnapshotsSentLastThirtySeconds.add(1L);
      fullSnapshotBytesLastSecond.add(nonNegative(bytes));
      fullSnapshotBytesLastFiveSeconds.add(nonNegative(bytes));
      fullSnapshotBytesLastThirtySeconds.add(nonNegative(bytes));
      recordFullSnapshotSentForClient(peerClientId, snapshot, bytes);
    } else if (message instanceof DeltaSnapshotMessage delta) {
      deltaSnapshotsSent.increment();
      lastDeltaSnapshotSentTick = delta.serverTick();
      lastDeltaSnapshotSentBytes = nonNegative(bytes);
      lastDeltaSnapshotSentEntityDeltas = delta.entityDeltas().size();
      lastDeltaSnapshotSentRemovals = delta.removedEntityIds().size();
      snapshotsSentLastSecond.add(1L);
      snapshotsSentLastFiveSeconds.add(1L);
      snapshotsSentLastThirtySeconds.add(1L);
    }
  }

  private static void recordFullSnapshotSentForClient(
      short clientId, SnapshotMessage snapshot, int bytes) {
    if (clientId <= 0) {
      return;
    }
    ClientTelemetry telemetry = clientTelemetry(clientId);
    PendingFullSnapshot pending =
        pendingFullSnapshots.remove(new PendingFullSnapshotKey(clientId, snapshot.serverTick()));
    FullSnapshotSendReason reason =
        pending != null && pending.serverTick() == snapshot.serverTick()
            ? pending.reason()
            : FullSnapshotSendReason.SERVER_FORCED_RESYNC;
    String reasonText = reason.name();
    long now = java.lang.System.currentTimeMillis();

    telemetry.fullSnapshotsLastSecond.add(1L);
    telemetry.fullSnapshotsLastFiveSeconds.add(1L);
    telemetry.fullSnapshotsLastThirtySeconds.add(1L);
    telemetry.fullSnapshotBytesLastSecond.add(nonNegative(bytes));
    telemetry.fullSnapshotBytesLastFiveSeconds.add(nonNegative(bytes));
    telemetry.fullSnapshotBytesLastThirtySeconds.add(nonNegative(bytes));
    telemetry.lastFullSnapshotReason = reasonText;
    telemetry.lastFullSnapshotTimeMs = now;
    telemetry.lastFullSnapshotTick = snapshot.serverTick();
    telemetry.lastFullSnapshotBytes = nonNegative(bytes);
    if (reason == FullSnapshotSendReason.PERIODIC_BASELINE) {
      telemetry.periodicFullSnapshots.increment();
      periodicFullSnapshotsSent.increment();
    } else {
      telemetry.fallbackFullSnapshots.increment();
      fallbackFullSnapshotsSent.increment();
    }
    if (reason == FullSnapshotSendReason.MISSING_BASELINE_HISTORY) {
      telemetry.missingBaselineFullFallbacks.increment();
      missingBaselineFullFallbacks.increment();
    }

    lastFullSnapshotSentReason = reasonText;
    lastFullSnapshotSentTimeMs = now;
  }

  private static int nonNegative(int value) {
    return Math.max(0, value);
  }

  private static String cleanReason(String reason) {
    return reason == null || reason.isBlank() ? "unknown" : reason;
  }

  private static String messageName(NetworkMessage message) {
    return message == null ? "unknown" : message.getClass().getSimpleName();
  }

  private static boolean isDebugTelemetryMessage(NetworkMessage message) {
    return message instanceof DebugTelemetryRequest
        || message instanceof DebugPing
        || message instanceof DebugTelemetrySnapshot
        || message instanceof DebugPong;
  }

  private static ClientTelemetry clientTelemetry(short clientId) {
    return serverClientTelemetry.computeIfAbsent(clientId, ignored -> new ClientTelemetry());
  }

  private static FullSnapshotSendReason normalizeReason(FullSnapshotSendReason reason) {
    return reason == null ? FullSnapshotSendReason.SERVER_FORCED_RESYNC : reason;
  }

  private static void recordTcpDecode(NetworkMessage message, long decodeDurationNanos) {
    if (decodeDurationNanos < 0L) {
      return;
    }
    lastTcpDecodeType = messageName(message);
    lastTcpDecodeMicros = nanosToMicros(decodeDurationNanos);
    tcpDecodeMicrosLastTenSeconds.add(lastTcpDecodeMicros, lastTcpDecodeType);
  }

  private static synchronized void refreshGcTelemetry() {
    long collectionCount = 0L;
    long collectionTimeMs = 0L;
    for (GarbageCollectorMXBean bean : GC_BEANS) {
      long count = bean.getCollectionCount();
      long timeMs = bean.getCollectionTime();
      if (count > 0L) {
        collectionCount += count;
      }
      if (timeMs > 0L) {
        collectionTimeMs += timeMs;
      }
    }

    if (previousGcCollectionCount < 0L || previousGcCollectionTimeMs < 0L) {
      previousGcCollectionCount = collectionCount;
      previousGcCollectionTimeMs = collectionTimeMs;
      return;
    }

    long countDelta = Math.max(0L, collectionCount - previousGcCollectionCount);
    long timeDeltaMs = Math.max(0L, collectionTimeMs - previousGcCollectionTimeMs);
    previousGcCollectionCount = collectionCount;
    previousGcCollectionTimeMs = collectionTimeMs;
    if (countDelta > 0L || timeDeltaMs > 0L) {
      lastGcPauseMs = timeDeltaMs;
      gcPauseMsLastTenSeconds.add(timeDeltaMs, "gc");
    }
  }

  private static float debugRttEstimate(ClientState clientState) {
    float estimate = clientState.rttEstimateMs();
    return estimate > 0f ? estimate : -1f;
  }

  private static long nanosToMicros(long nanos) {
    return nanos < 0L ? -1L : Math.max(0L, nanos / NANOS_PER_MICRO);
  }

  private static String formatBytes(long bytes) {
    if (bytes < 0L) {
      return "n/a";
    }
    if (bytes < 1024L) {
      return bytes + " B";
    }
    return String.format(Locale.ROOT, "%.1f KiB", bytes / 1024.0);
  }

  private static String formatMillis(long millis) {
    if (millis < 0L) {
      return "n/a";
    }
    if (millis < 1000L) {
      return millis + " ms";
    }
    return String.format(Locale.ROOT, "%.1fs", millis / 1000.0);
  }

  private static String formatNanos(long nanos) {
    if (nanos < 0L) {
      return "n/a";
    }
    return Long.toString(nanos);
  }

  private static String formatSeconds(double seconds) {
    if (seconds < 0.0) {
      return "n/a";
    }
    return String.format(Locale.ROOT, "%.2fs", seconds);
  }

  private static String formatMicros(long micros) {
    if (micros < 0L) {
      return "n/a";
    }
    if (micros < 1000L) {
      return micros + " us";
    }
    return String.format(Locale.ROOT, "%.2f ms", micros / 1000.0);
  }

  private static String formatRtt(float rttMs) {
    if (rttMs < 0f) {
      return "n/a";
    }
    return String.format(Locale.ROOT, "%.1f ms", rttMs);
  }

  private static String formatTick(int tick) {
    return tick < 0 ? "n/a" : Integer.toString(tick);
  }

  private static String formatTickLabel(int tick) {
    return tick < 0 ? "n/a" : "t" + tick;
  }

  private static String formatCount(int count) {
    return count < 0 ? "n/a" : Integer.toString(count);
  }

  private record PendingFullSnapshotKey(short clientId, int serverTick) {}

  private record PendingFullSnapshot(
      int serverTick, int entityCount, FullSnapshotSendReason reason, long scheduledTimeMs) {}

  private static final class ClientTelemetry {
    private final RollingCounter fullSnapshotsLastSecond = new RollingCounter(1_000L);
    private final RollingCounter fullSnapshotsLastFiveSeconds = new RollingCounter(5_000L);
    private final RollingCounter fullSnapshotsLastThirtySeconds = new RollingCounter(30_000L);
    private final RollingCounter fullSnapshotBytesLastSecond = new RollingCounter(1_000L);
    private final RollingCounter fullSnapshotBytesLastFiveSeconds = new RollingCounter(5_000L);
    private final RollingCounter fullSnapshotBytesLastThirtySeconds = new RollingCounter(30_000L);
    private final LongAdder periodicFullSnapshots = new LongAdder();
    private final LongAdder fallbackFullSnapshots = new LongAdder();
    private final LongAdder missingBaselineFullFallbacks = new LongAdder();

    private volatile int serverCurrentTick = -1;
    private volatile int latestAckedSnapshotTick = -1;
    private volatile int ackAgeTicks = -1;
    private volatile long ackAgeMs = -1L;
    private volatile boolean ackBaselineInHistory;
    private volatile int historyCapacityTicks = -1;
    private volatile double historyCapacitySeconds = -1.0;
    private volatile String lastFullSnapshotReason = "n/a";
    private volatile long lastFullSnapshotTimeMs = -1L;
    private volatile int lastFullSnapshotTick = -1;
    private volatile int lastFullSnapshotBytes = -1;
  }

  private static final class RollingMax {
    private final long windowMs;
    private final ArrayDeque<Sample> samples = new ArrayDeque<>();

    private RollingMax(long windowMs) {
      this.windowMs = windowMs;
    }

    private synchronized void add(long value, String label) {
      if (value < 0L) {
        return;
      }
      long now = java.lang.System.currentTimeMillis();
      prune(now);
      samples.addLast(new Sample(now, value, cleanReason(label)));
    }

    private synchronized Sample max() {
      long now = java.lang.System.currentTimeMillis();
      prune(now);
      Sample max = Sample.empty();
      for (Sample sample : samples) {
        if (sample.value() > max.value()) {
          max = sample;
        }
      }
      return max;
    }

    private synchronized void reset() {
      samples.clear();
    }

    private void prune(long now) {
      while (!samples.isEmpty() && now - samples.peekFirst().timeMs() > windowMs) {
        samples.removeFirst();
      }
    }

    private record Sample(long timeMs, long value, String label) {
      private static Sample empty() {
        return new Sample(-1L, -1L, "n/a");
      }
    }
  }

  private static final class RollingCounter {
    private static final long BUCKET_MS = 100L;
    private static final long RESETTING_BUCKET = Long.MIN_VALUE;

    private final AtomicLongArray bucketIds;
    private final AtomicLongArray values;

    private RollingCounter(long windowMs) {
      int bucketCount = (int) (windowMs / BUCKET_MS) + 2;
      this.bucketIds = new AtomicLongArray(bucketCount);
      this.values = new AtomicLongArray(bucketCount);
    }

    private void add(long value) {
      long bucketId = java.lang.System.currentTimeMillis() / BUCKET_MS;
      int index = (int) Math.floorMod(bucketId, bucketIds.length());
      while (true) {
        long existingBucketId = bucketIds.get(index);
        if (existingBucketId == bucketId) {
          values.addAndGet(index, value);
          return;
        }
        if (existingBucketId == RESETTING_BUCKET) {
          Thread.onSpinWait();
          continue;
        }
        if (bucketIds.compareAndSet(index, existingBucketId, RESETTING_BUCKET)) {
          values.set(index, 0L);
          bucketIds.set(index, bucketId);
          values.addAndGet(index, value);
          return;
        }
      }
    }

    private long sum() {
      long currentBucketId = java.lang.System.currentTimeMillis() / BUCKET_MS;
      long total = 0L;
      for (int i = 0; i < bucketIds.length(); i++) {
        long bucketId = bucketIds.get(i);
        if (bucketId > 0L && currentBucketId - bucketId < bucketIds.length()) {
          total += values.get(i);
        }
      }
      return total;
    }

    private void reset() {
      for (int i = 0; i < bucketIds.length(); i++) {
        bucketIds.set(i, 0L);
        values.set(i, 0L);
      }
    }
  }
}
