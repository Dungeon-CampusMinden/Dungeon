package core.network;

import core.network.messages.NetworkMessage;
import core.network.messages.c2s.DebugPing;
import core.network.messages.c2s.DebugTelemetryRequest;
import core.network.messages.s2c.DebugPong;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import core.network.server.Session;
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

  private static final long SERVER_SNAPSHOT_STALE_AFTER_MS = 2_000L;
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
  private static final RollingMax dispatchMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax networkDispatchMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax frameMicrosLastTenSeconds = new RollingMax(10_000L);
  private static final RollingMax gcPauseMsLastTenSeconds = new RollingMax(10_000L);

  private static final Map<Short, ClientTelemetry> serverClientTelemetry =
      new ConcurrentHashMap<>();
  private static final Map<Short, PendingFullSnapshot> pendingFullSnapshots =
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

  private static volatile String lastUdpFallbackReason = "n/a";
  private static volatile String lastUdpDropReason = "n/a";
  private static volatile String lastUdpFailureReason = "n/a";

  private static volatile String lastTcpDecodeType = "n/a";
  private static volatile long lastTcpDecodeMicros = -1L;
  private static volatile long lastQueueAgeMicros = -1L;
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
  private static volatile String debugRequestStatus = "n/a";

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
    lastUdpFallbackReason = "n/a";
    lastUdpDropReason = "n/a";
    lastUdpFailureReason = "n/a";
    lastTcpDecodeType = "n/a";
    lastTcpDecodeMicros = -1L;
    lastQueueAgeMicros = -1L;
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
    debugRequestStatus = "n/a";
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
    if (message instanceof SnapshotMessage snapshot) {
      inboundFullSnapshotBytesByTick.put(snapshot.serverTick(), nonNegative(bytes));
    }
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
    if (isDebugTelemetryMessage(message)) {
      debugUdpInboundMessages.increment();
      debugUdpInboundBytes.add(nonNegative(bytes));
      return;
    }
    udpInboundMessages.increment();
    udpInboundBytes.add(nonNegative(bytes));
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
        clientId,
        new PendingFullSnapshot(
            serverTick,
            Math.max(0, entityCount),
            normalizedReason,
            java.lang.System.currentTimeMillis()));
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
   * @param ackSendNanos snapshot ack send duration
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
    lastAppliedSnapshotKind = delta ? "delta" : "full";
    lastAppliedSnapshotTick = serverTick;
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
    recordStaleSnapshot(delta, -1);
  }

  /**
   * Records a stale snapshot dropped by the client before application.
   *
   * @param delta true when this was a delta snapshot
   * @param serverTick stale snapshot server tick, or -1 when unknown
   */
  public static void recordStaleSnapshot(boolean delta, int serverTick) {
    if (delta) {
      staleDeltaSnapshots.increment();
    } else {
      staleFullSnapshots.increment();
      Integer bytes = inboundFullSnapshotBytesByTick.remove(serverTick);
      if (bytes != null) {
        staleFullSnapshotBytes.add(nonNegative(bytes));
      }
    }
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
    debugRequestStatus = "pong " + formatRtt(latestDebugRttMs);
  }

  /**
   * Records the latest debug telemetry request status for the client overlay.
   *
   * @param status short status text
   */
  public static void recordDebugRequestStatus(String status) {
    debugRequestStatus = cleanReason(status);
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
            gcPauseMsLastTenSeconds.max().value()),
        clients);
  }

  /**
   * Builds the current debug overlay text.
   *
   * @return multiline telemetry text
   */
  public static String debugText() {
    refreshGcTelemetry();
    StringBuilder text = new StringBuilder("Network Telemetry");
    text.append("\nClient local: ")
        .append(clientConnected ? "connected" : "disconnected")
        .append(" id=")
        .append(clientId)
        .append(" udp=")
        .append(clientUdpReady ? "ready" : "fallback")
        .append(" mode=")
        .append(clientUdpRetryMode ? "retry" : "keepalive")
        .append(" ackAge=")
        .append(formatMillis(clientUdpLastAckAgeMs))
        .append(" snap=")
        .append(formatTick(clientLatestAppliedSnapshotTick))
        .append(" rtt=")
        .append(formatRtt(latestDebugRttMs))
        .append(" debug=")
        .append(debugRequestStatus);
    text.append("\nClient snapshots: full=")
        .append(fullSnapshotsApplied.sum())
        .append(" delta=")
        .append(deltaSnapshotsApplied.sum())
        .append(" stale(f/d)=")
        .append(staleFullSnapshots.sum())
        .append("/")
        .append(staleDeltaSnapshots.sum())
        .append(" last=")
        .append(lastAppliedSnapshotKind)
        .append("@")
        .append(formatTick(lastAppliedSnapshotTick))
        .append(" e/r=")
        .append(formatCount(lastAppliedSnapshotEntities))
        .append("/")
        .append(formatCount(lastAppliedSnapshotRemovals))
        .append(" ")
        .append(formatMicros(lastAppliedSnapshotMicros));
    text.append("\nClient snapshot path: staleCheck=")
        .append(formatMicros(lastSnapshotStaleCheckMicros))
        .append(" fullApply=")
        .append(formatMicros(lastFullSnapshotApplyMicros))
        .append(" deltaMat=")
        .append(formatMicros(lastDeltaMaterializeMicros))
        .append(" reconcile=")
        .append(formatMicros(lastEntityReconcileMicros))
        .append(" ack=")
        .append(formatMicros(lastSnapshotAckMicros))
        .append(" stale=")
        .append(lastSnapshotHandlerStale)
        .append(" staleFullBytes=")
        .append(formatBytes(staleFullSnapshotBytes.sum()));
    RollingMax.Sample queueMax = queueAgeMicrosLastTenSeconds.max();
    RollingMax.Sample dispatchMax = dispatchMicrosLastTenSeconds.max();
    RollingMax.Sample tcpDecodeMax = tcpDecodeMicrosLastTenSeconds.max();
    text.append("\nClient timing: frame=")
        .append(formatMicros(lastFrameMicros))
        .append(" net=")
        .append(formatMicros(lastNetworkDispatchMicros))
        .append(" queue=")
        .append(formatMicros(lastQueueAgeMicros))
        .append(" dispatch=")
        .append(formatMicros(lastMessageDispatchMicros))
        .append(" tcpDecode=")
        .append(lastTcpDecodeType)
        .append(" ")
        .append(formatMicros(lastTcpDecodeMicros))
        .append(" max10(q/d/dec)=")
        .append(formatMicros(queueMax.value()))
        .append("/")
        .append(formatMicros(dispatchMax.value()))
        .append("/")
        .append(tcpDecodeMax.label())
        .append(" ")
        .append(formatMicros(tcpDecodeMax.value()))
        .append(" gc=")
        .append(formatMillis(lastGcPauseMs));
    text.append("\nClient transport out: tcp=")
        .append(tcpOutboundMessages.sum())
        .append("/")
        .append(formatBytes(tcpOutboundBytes.sum()))
        .append(" udp=")
        .append(udpOutboundMessages.sum())
        .append("/")
        .append(formatBytes(udpOutboundBytes.sum()));
    text.append("\nClient transport in:  tcp=")
        .append(tcpInboundMessages.sum())
        .append("/")
        .append(formatBytes(tcpInboundBytes.sum()))
        .append(" udp=")
        .append(udpInboundMessages.sum())
        .append("/")
        .append(formatBytes(udpInboundBytes.sum()));
    text.append("\nClient debug tx/rx: tcp=")
        .append(debugTcpOutboundMessages.sum())
        .append("/")
        .append(debugTcpInboundMessages.sum())
        .append(" udp=")
        .append(debugUdpOutboundMessages.sum())
        .append("/")
        .append(debugUdpInboundMessages.sum());
    appendServerSnapshot(text);
    return text.toString();
  }

  private static void appendServerSnapshot(StringBuilder text) {
    DebugTelemetrySnapshot snapshot = latestServerSnapshot;
    long receivedMs = latestServerSnapshotReceivedTimeMs;
    if (snapshot == null || receivedMs < 0L) {
      text.append("\nServer authoritative: n/a");
      return;
    }

    long age = java.lang.System.currentTimeMillis() - receivedMs;
    text.append("\nServer authoritative: ");
    if (age > SERVER_SNAPSHOT_STALE_AFTER_MS) {
      text.append("server telemetry stale ").append(formatMillis(age)).append(" ");
    }
    text.append("clients=")
        .append(snapshot.clients().size())
        .append(" udp=")
        .append(snapshot.clients().stream().filter(DebugTelemetrySnapshot.Client::udpReady).count())
        .append("/")
        .append(snapshot.clients().size());
    text.append(" capture(c/s)=")
        .append(formatNanos(latestServerSnapshotClientCaptureNanos))
        .append("/")
        .append(formatNanos(snapshot.serverTimeNanos()));
    text.append("\nServer snapshots: full=")
        .append(snapshot.snapshots().fullSent())
        .append(" last=")
        .append(
            formatSnapshot(
                snapshot.snapshots().lastFullTick(),
                snapshot.snapshots().lastFullBytes(),
                snapshot.snapshots().lastFullEntities()))
        .append(" | delta=")
        .append(snapshot.snapshots().deltaSent())
        .append(" last=")
        .append(formatDeltaSnapshot(snapshot))
        .append(" build=")
        .append(formatMicros(snapshot.snapshots().lastBuildMicros()))
        .append(" reason=")
        .append(snapshot.snapshots().lastFullReason())
        .append(" periodic/fallback=")
        .append(snapshot.snapshots().periodicFullSent())
        .append("/")
        .append(snapshot.snapshots().fallbackFullSent())
        .append(" missingBase=")
        .append(snapshot.snapshots().missingBaselineFullFallbacks())
        .append(" staleFullBytes=")
        .append(formatBytes(snapshot.snapshots().staleFullBytes()))
        .append(" rate1/5/30=")
        .append(snapshot.windows().snapshotsSentLastSecond())
        .append("/")
        .append(snapshot.windows().snapshotsSentLastFiveSeconds())
        .append("/")
        .append(snapshot.windows().snapshotsSentLastThirtySeconds());
    text.append("\nServer history: tick=")
        .append(formatTick(snapshot.snapshots().historyServerTick()))
        .append(" size=")
        .append(formatCount(snapshot.snapshots().historySize()))
        .append("/")
        .append(formatCount(snapshot.snapshots().historyCapacityTicks()))
        .append(" cap=")
        .append(formatSeconds(snapshot.snapshots().historyCapacitySeconds()));
    text.append("\nServer transport out: tcp=")
        .append(snapshot.transport().tcpOutboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().tcpOutboundBytes()))
        .append(" udp=")
        .append(snapshot.transport().udpOutboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().udpOutboundBytes()))
        .append(" bytes1/5/30=")
        .append(formatBytes(snapshot.windows().transportOutBytesLastSecond()))
        .append("/")
        .append(formatBytes(snapshot.windows().transportOutBytesLastFiveSeconds()))
        .append("/")
        .append(formatBytes(snapshot.windows().transportOutBytesLastThirtySeconds()));
    text.append("\nServer transport in:  tcp=")
        .append(snapshot.transport().tcpInboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().tcpInboundBytes()))
        .append(" udp=")
        .append(snapshot.transport().udpInboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().udpInboundBytes()));
    text.append("\nServer debug tx/rx: tcp=")
        .append(snapshot.debugTransport().tcpOutboundMessages())
        .append("/")
        .append(snapshot.debugTransport().tcpInboundMessages())
        .append(" udp=")
        .append(snapshot.debugTransport().udpOutboundMessages())
        .append("/")
        .append(snapshot.debugTransport().udpInboundMessages());
    text.append("\nServer UDP: fallback=")
        .append(snapshot.udp().fallbacks())
        .append(" oversized=")
        .append(snapshot.udp().oversizedPackets())
        .append(" sendFail=")
        .append(snapshot.udp().sendFailures())
        .append(" dropped=")
        .append(snapshot.udp().droppedPackets())
        .append(" last(f/drop/fail)=")
        .append(snapshot.udp().lastFallbackReason())
        .append("/")
        .append(snapshot.udp().lastDropReason())
        .append("/")
        .append(snapshot.udp().lastFailureReason());
    DebugTelemetrySnapshot.Timings timings = snapshot.timings();
    text.append("\nServer timing: frame=")
        .append(formatMicros(timings.lastFrameMicros()))
        .append(" max10=")
        .append(formatMicros(timings.maxFrameMicrosLastTenSeconds()))
        .append(" net=")
        .append(formatMicros(timings.lastNetworkDispatchMicros()))
        .append(" max10=")
        .append(formatMicros(timings.maxNetworkDispatchMicrosLastTenSeconds()))
        .append(" queue=")
        .append(formatMicros(timings.lastQueueAgeMicros()))
        .append(" max10=")
        .append(formatMicros(timings.maxQueueAgeMicrosLastTenSeconds()))
        .append(" tcpDecode=")
        .append(timings.lastTcpDecodeType())
        .append(" ")
        .append(formatMicros(timings.lastTcpDecodeMicros()))
        .append(" max10=")
        .append(timings.maxTcpDecodeTypeLastTenSeconds())
        .append(" ")
        .append(formatMicros(timings.maxTcpDecodeMicrosLastTenSeconds()))
        .append(" gc=")
        .append(formatMillis(timings.lastGcPauseMs()))
        .append("/")
        .append(formatMillis(timings.maxGcPauseMsLastTenSeconds()));
    appendServerClientTelemetry(text, snapshot.clients());
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
    PendingFullSnapshot pending = pendingFullSnapshots.remove(clientId);
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
    return clientState.lastClientTick() > 0L && estimate > 0f ? estimate : -1f;
  }

  private static long nanosToMicros(long nanos) {
    return nanos < 0L ? -1L : Math.max(0L, nanos / NANOS_PER_MICRO);
  }

  private static void appendServerClientTelemetry(
      StringBuilder text, List<DebugTelemetrySnapshot.Client> clients) {
    for (DebugTelemetrySnapshot.Client client : clients) {
      text.append("\nServer client ")
          .append(client.clientId())
          .append(": ack=")
          .append(formatTick(client.latestAckedSnapshotTick()))
          .append(" age=")
          .append(formatCount(client.ackAgeTicks()))
          .append("t/")
          .append(formatMillis(client.ackAgeMs()))
          .append(" hist=")
          .append(client.ackBaselineInHistory())
          .append(" cap=")
          .append(formatCount(client.historyCapacityTicks()))
          .append("t/")
          .append(formatSeconds(client.historyCapacitySeconds()))
          .append(" full1/5/30=")
          .append(client.fullSnapshotsLastSecond())
          .append("/")
          .append(client.fullSnapshotsLastFiveSeconds())
          .append("/")
          .append(client.fullSnapshotsLastThirtySeconds())
          .append(" bytes=")
          .append(formatBytes(client.fullSnapshotBytesLastSecond()))
          .append("/")
          .append(formatBytes(client.fullSnapshotBytesLastFiveSeconds()))
          .append("/")
          .append(formatBytes(client.fullSnapshotBytesLastThirtySeconds()))
          .append(" periodic/fallback=")
          .append(client.periodicFullSnapshots())
          .append("/")
          .append(client.fallbackFullSnapshots())
          .append(" missingBase=")
          .append(client.missingBaselineFullFallbacks())
          .append(" lastFull=")
          .append(client.lastFullSnapshotReason())
          .append("@")
          .append(formatTick(client.lastFullSnapshotTick()))
          .append("/")
          .append(formatBytes(client.lastFullSnapshotBytes()))
          .append(" age=")
          .append(formatMillis(client.lastFullSnapshotAgeMs()));
    }
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
    return String.format(Locale.ROOT, "%.1f s", millis / 1000.0);
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
    return String.format(Locale.ROOT, "%.2f s", seconds);
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

  private static String formatCount(int count) {
    return count < 0 ? "n/a" : Integer.toString(count);
  }

  private static String formatSnapshot(int tick, int bytes, int entities) {
    if (tick < 0 || bytes < 0) {
      return "n/a";
    }
    return "t" + tick + "/" + formatBytes(bytes) + "/e=" + formatCount(entities);
  }

  private static String formatDeltaSnapshot(DebugTelemetrySnapshot snapshot) {
    DebugTelemetrySnapshot.Snapshots snapshots = snapshot.snapshots();
    if (snapshots.lastDeltaTick() < 0 || snapshots.lastDeltaBytes() < 0) {
      return "n/a";
    }
    return "t"
        + snapshots.lastDeltaTick()
        + "/"
        + formatBytes(snapshots.lastDeltaBytes())
        + "/d="
        + formatCount(snapshots.lastDeltaEntityDeltas())
        + "/r="
        + formatCount(snapshots.lastDeltaRemovals());
  }

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
