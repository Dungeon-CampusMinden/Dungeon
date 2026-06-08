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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

  private static final RollingCounter transportOutBytesLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter transportOutBytesLastFiveSeconds = new RollingCounter(5_000L);
  private static final RollingCounter snapshotsSentLastSecond = new RollingCounter(1_000L);
  private static final RollingCounter snapshotsSentLastFiveSeconds = new RollingCounter(5_000L);

  private static volatile int lastFullSnapshotSentTick = -1;
  private static volatile int lastFullSnapshotSentBytes = -1;
  private static volatile int lastFullSnapshotSentEntities = -1;
  private static volatile int lastDeltaSnapshotSentTick = -1;
  private static volatile int lastDeltaSnapshotSentBytes = -1;
  private static volatile int lastDeltaSnapshotSentEntityDeltas = -1;
  private static volatile int lastDeltaSnapshotSentRemovals = -1;
  private static volatile long lastSnapshotBuildMicros = -1L;

  private static volatile String lastAppliedSnapshotKind = "n/a";
  private static volatile int lastAppliedSnapshotTick = -1;
  private static volatile int lastAppliedSnapshotEntities = -1;
  private static volatile int lastAppliedSnapshotRemovals = -1;
  private static volatile long lastAppliedSnapshotMicros = -1L;

  private static volatile String lastUdpFallbackReason = "n/a";
  private static volatile String lastUdpDropReason = "n/a";
  private static volatile String lastUdpFailureReason = "n/a";

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
    transportOutBytesLastSecond.reset();
    transportOutBytesLastFiveSeconds.reset();
    snapshotsSentLastSecond.reset();
    snapshotsSentLastFiveSeconds.reset();
    lastFullSnapshotSentTick = -1;
    lastFullSnapshotSentBytes = -1;
    lastFullSnapshotSentEntities = -1;
    lastDeltaSnapshotSentTick = -1;
    lastDeltaSnapshotSentBytes = -1;
    lastDeltaSnapshotSentEntityDeltas = -1;
    lastDeltaSnapshotSentRemovals = -1;
    lastSnapshotBuildMicros = -1L;
    lastAppliedSnapshotKind = "n/a";
    lastAppliedSnapshotTick = -1;
    lastAppliedSnapshotEntities = -1;
    lastAppliedSnapshotRemovals = -1;
    lastAppliedSnapshotMicros = -1L;
    lastUdpFallbackReason = "n/a";
    lastUdpDropReason = "n/a";
    lastUdpFailureReason = "n/a";
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
  }

  /**
   * Records a successfully queued outbound TCP message.
   *
   * @param message serialized message
   * @param bytes serialized payload size in bytes
   */
  public static void recordOutboundTcp(NetworkMessage message, int bytes) {
    if (isDebugTelemetryMessage(message)) {
      debugTcpOutboundMessages.increment();
      debugTcpOutboundBytes.add(nonNegative(bytes));
      return;
    }
    tcpOutboundMessages.increment();
    tcpOutboundBytes.add(nonNegative(bytes));
    recordTransportOutBytes(bytes);
    recordSnapshotSent(message, bytes);
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
    recordSnapshotSent(message, bytes);
  }

  /**
   * Records a decoded inbound TCP message.
   *
   * @param message decoded message
   * @param bytes serialized payload size in bytes
   */
  public static void recordInboundTcp(NetworkMessage message, int bytes) {
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
    lastSnapshotBuildMicros = Math.max(0L, durationNanos / 1_000L);
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
    lastAppliedSnapshotMicros = Math.max(0L, durationNanos / 1_000L);
  }

  /**
   * Records a stale snapshot dropped by the client before application.
   *
   * @param delta true when this was a delta snapshot
   */
  public static void recordStaleSnapshot(boolean delta) {
    if (delta) {
      staleDeltaSnapshots.increment();
    } else {
      staleFullSnapshots.increment();
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
    long now = java.lang.System.currentTimeMillis();
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
      clients.add(
          new DebugTelemetrySnapshot.Client(
              clientState.clientId(),
              session.udpReady(),
              debugRttEstimate(clientState),
              Math.max(0L, now - clientState.lastActivityTimeMs()),
              clientState.snapshotSync().lastAckedSnapshotTick()));
    }

    return new DebugTelemetrySnapshot(
        requestId,
        now,
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
            lastSnapshotBuildMicros),
        new DebugTelemetrySnapshot.Windows(
            transportOutBytesLastSecond.sum(),
            transportOutBytesLastFiveSeconds.sum(),
            snapshotsSentLastSecond.sum(),
            snapshotsSentLastFiveSeconds.sum()),
        clients);
  }

  /**
   * Builds the current debug overlay text.
   *
   * @return multiline telemetry text
   */
  public static String debugText() {
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
        .append(" rate1/5=")
        .append(snapshot.windows().snapshotsSentLastSecond())
        .append("/")
        .append(snapshot.windows().snapshotsSentLastFiveSeconds());
    text.append("\nServer transport out: tcp=")
        .append(snapshot.transport().tcpOutboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().tcpOutboundBytes()))
        .append(" udp=")
        .append(snapshot.transport().udpOutboundMessages())
        .append("/")
        .append(formatBytes(snapshot.transport().udpOutboundBytes()))
        .append(" bytes1/5=")
        .append(formatBytes(snapshot.windows().transportOutBytesLastSecond()))
        .append("/")
        .append(formatBytes(snapshot.windows().transportOutBytesLastFiveSeconds()));
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
        .append(" last=")
        .append(snapshot.udp().lastFallbackReason());
  }

  private static void reset(LongAdder adder) {
    adder.reset();
  }

  private static void recordTransportOutBytes(int bytes) {
    long value = nonNegative(bytes);
    transportOutBytesLastSecond.add(value);
    transportOutBytesLastFiveSeconds.add(value);
  }

  private static void recordSnapshotSent(NetworkMessage message, int bytes) {
    if (message instanceof SnapshotMessage snapshot) {
      fullSnapshotsSent.increment();
      lastFullSnapshotSentTick = snapshot.serverTick();
      lastFullSnapshotSentBytes = nonNegative(bytes);
      lastFullSnapshotSentEntities = snapshot.entities().size();
      snapshotsSentLastSecond.add(1L);
      snapshotsSentLastFiveSeconds.add(1L);
    } else if (message instanceof DeltaSnapshotMessage delta) {
      deltaSnapshotsSent.increment();
      lastDeltaSnapshotSentTick = delta.serverTick();
      lastDeltaSnapshotSentBytes = nonNegative(bytes);
      lastDeltaSnapshotSentEntityDeltas = delta.entityDeltas().size();
      lastDeltaSnapshotSentRemovals = delta.removedEntityIds().size();
      snapshotsSentLastSecond.add(1L);
      snapshotsSentLastFiveSeconds.add(1L);
    }
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

  private static float debugRttEstimate(ClientState clientState) {
    float estimate = clientState.rttEstimateMs();
    return clientState.lastClientTick() > 0L && estimate > 0f ? estimate : -1f;
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
