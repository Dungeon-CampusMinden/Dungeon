package core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import contrib.entities.CharacterClass;
import core.network.messages.c2s.DebugPing;
import core.network.messages.c2s.SnapshotAck;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import core.network.server.Session;
import core.network.telemetry.NetworkTelemetryReport;
import core.network.telemetry.TelemetrySeverity;
import core.network.telemetry.TelemetrySpan;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link NetworkTelemetry}. */
public class NetworkTelemetryTest {

  /** Clears global telemetry state before every test. */
  @BeforeEach
  public void setup() {
    NetworkTelemetry.reset();
  }

  /** Verifies the legacy text API delegates to the structured report. */
  @Test
  public void debugTextDelegatesToDebugReportPlainText() {
    assertEquals(NetworkTelemetry.debugReport().plainText(), NetworkTelemetry.debugText());
  }

  /** Verifies debug control statuses do not add persistent noise to the report. */
  @Test
  public void debugReportOmitsDebugControlStatuses() {
    String plainText = NetworkTelemetry.debugReport().plainText();

    assertTrue(plainText.contains("RTT: n/a"));
    assertFalse(plainText.contains("Debug:"));
    assertFalse(plainText.contains("debug="));
    assertFalse(plainText.contains("debug tcp="));
    assertFalse(plainText.contains("capture(c/s)="));
  }

  /** Verifies transport counters distinguish successful UDP from oversized fallback. */
  @Test
  public void transportCountersTrackOversizedUdpFallback() {
    SnapshotAck snapshotAck = new SnapshotAck(1);

    NetworkTelemetry.recordOutboundTcp(snapshotAck, 10);
    NetworkTelemetry.recordInboundTcp(snapshotAck, 11);
    NetworkTelemetry.recordOutboundUdp(snapshotAck, 12);
    NetworkTelemetry.recordInboundUdp(snapshotAck, 13);
    NetworkTelemetry.recordUdpOversized(snapshotAck, 1_500);
    NetworkTelemetry.recordUdpFallback("test oversized");

    DebugTelemetrySnapshot snapshot = NetworkTelemetry.buildServerSnapshot(99L, List.of());

    assertEquals(1, snapshot.transport().tcpOutboundMessages());
    assertEquals(10, snapshot.transport().tcpOutboundBytes());
    assertEquals(1, snapshot.transport().tcpInboundMessages());
    assertEquals(11, snapshot.transport().tcpInboundBytes());
    assertEquals(1, snapshot.transport().udpOutboundMessages());
    assertEquals(12, snapshot.transport().udpOutboundBytes());
    assertEquals(1, snapshot.transport().udpInboundMessages());
    assertEquals(13, snapshot.transport().udpInboundBytes());
    assertEquals(1, snapshot.udp().oversizedPackets());
    assertEquals(1, snapshot.udp().fallbacks());
    assertTrue(snapshot.windows().transportOutBytesLastSecond() >= 22);
  }

  /** Verifies debug protocol traffic is separated from gameplay transport counters. */
  @Test
  public void debugTrafficUsesSeparateCounters() {
    DebugPing ping = new DebugPing(1L, 2L);

    NetworkTelemetry.recordOutboundTcp(ping, 10);
    NetworkTelemetry.recordInboundTcp(ping, 11);
    NetworkTelemetry.recordOutboundUdp(ping, 12);
    NetworkTelemetry.recordInboundUdp(ping, 13);

    DebugTelemetrySnapshot snapshot = NetworkTelemetry.buildServerSnapshot(99L, List.of());

    assertEquals(0, snapshot.transport().tcpOutboundMessages());
    assertEquals(0, snapshot.transport().udpOutboundMessages());
    assertEquals(1, snapshot.debugTransport().tcpOutboundMessages());
    assertEquals(10, snapshot.debugTransport().tcpOutboundBytes());
    assertEquals(1, snapshot.debugTransport().tcpInboundMessages());
    assertEquals(11, snapshot.debugTransport().tcpInboundBytes());
    assertEquals(1, snapshot.debugTransport().udpOutboundMessages());
    assertEquals(12, snapshot.debugTransport().udpOutboundBytes());
    assertEquals(1, snapshot.debugTransport().udpInboundMessages());
    assertEquals(13, snapshot.debugTransport().udpInboundBytes());
  }

  /** Verifies snapshot counters distinguish full snapshots from delta snapshots. */
  @Test
  public void snapshotCountersTrackFullAndDeltaSeparately() {
    SnapshotMessage full =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).build(),
                    Set.of(EntityStateField.POSITION))),
            List.of(2),
            new LevelState(Set.of()));

    NetworkTelemetry.recordOutboundTcp(full, 200);
    NetworkTelemetry.recordOutboundUdp(delta, 80);
    NetworkTelemetry.recordSnapshotBuild(123_000L);

    DebugTelemetrySnapshot snapshot = NetworkTelemetry.buildServerSnapshot(100L, List.of());

    assertEquals(1, snapshot.snapshots().fullSent());
    assertEquals(1, snapshot.snapshots().deltaSent());
    assertEquals(10, snapshot.snapshots().lastFullTick());
    assertEquals(200, snapshot.snapshots().lastFullBytes());
    assertEquals(1, snapshot.snapshots().lastFullEntities());
    assertEquals(11, snapshot.snapshots().lastDeltaTick());
    assertEquals(80, snapshot.snapshots().lastDeltaBytes());
    assertEquals(1, snapshot.snapshots().lastDeltaEntityDeltas());
    assertEquals(1, snapshot.snapshots().lastDeltaRemovals());
    assertEquals(123, snapshot.snapshots().lastBuildMicros());
    assertTrue(snapshot.windows().snapshotsSentLastSecond() >= 2);
  }

  /** Verifies client apply/stale snapshot telemetry is visible in the debug text. */
  @Test
  public void clientSnapshotApplyAndStaleCountersReachDebugText() {
    NetworkTelemetry.recordSnapshotApplied(false, 10, 2, 0, 1_000L);
    NetworkTelemetry.recordSnapshotApplied(true, 11, 3, 1, 2_000L);
    NetworkTelemetry.recordStaleSnapshot(false);
    NetworkTelemetry.recordStaleSnapshot(true);

    String debugText = NetworkTelemetry.debugText();

    assertTrue(debugText.contains("Snapshots: applied full=1 delta=1"));
    assertTrue(debugText.contains("stale=1 (expected 0)/1 (expected 0)"));
    assertTrue(debugText.contains("last=delta@11 e/r=3/1"));
  }

  /** Verifies expected-zero counters are represented as bad spans in the report. */
  @Test
  public void debugReportMarksExpectedZeroCountersBad() {
    SnapshotAck snapshotAck = new SnapshotAck(1);
    NetworkTelemetry.recordStaleSnapshot(false, 10);
    NetworkTelemetry.recordStaleSnapshot(true, 11);
    NetworkTelemetry.recordMissingLocalDeltaBaseline(8, 12);
    NetworkTelemetry.recordClientSnapshotResyncRequest(8, 12);
    NetworkTelemetry.recordUdpFallback("test fallback");
    NetworkTelemetry.recordUdpSendFailure(snapshotAck, "test send fail");
    NetworkTelemetry.recordUdpDrop("test drop");
    NetworkTelemetry.recordServerSnapshot(NetworkTelemetry.buildServerSnapshot(1L, List.of()));

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();
    String plainText = report.plainText();

    assertTrue(plainText.contains("stale=1 (expected 0)/1 (expected 0)"));
    assertTrue(plainText.contains("missingLocalBase=1 (expected 0)"));
    assertTrue(plainText.contains("resyncReq=1 (expected 0)"));
    assertTrue(plainText.contains("fallback=1 (expected 0)"));
    assertTrue(plainText.contains("sendFail=1 (expected 0)"));
    assertTrue(plainText.contains("dropped=1 (expected 0)"));
    assertTrue(badSpanCount(report, "1 (expected 0)") >= 7);
  }

  /** Verifies oversized snapshots and slow timings are represented as bad spans. */
  @Test
  public void debugReportMarksSnapshotSizeAndTimingBreachesBad() {
    SnapshotMessage full =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).build(),
                    Set.of(EntityStateField.POSITION))),
            List.of(),
            new LevelState(Set.of()));

    NetworkTelemetry.recordOutboundTcp(full, 300 * 1024);
    NetworkTelemetry.recordOutboundUdp(delta, 1_500);
    NetworkTelemetry.recordSnapshotBuild(10_000_000L);
    NetworkTelemetry.recordSnapshotHandlerTiming(
        false, 10, 10_000_000L, 10_000_000L, 10_000_000L, 10_000_000L, 10_000_000L, true);
    NetworkTelemetry.recordServerSnapshot(NetworkTelemetry.buildServerSnapshot(1L, List.of()));

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();

    assertTrue(hasBadSpanContaining(report, "300.0 KiB"));
    assertTrue(hasBadSpanContaining(report, "1.5 KiB"));
    assertTrue(hasBadSpanContaining(report, "10.00 ms"));
    assertTrue(hasBadSpanContaining(report, "true (expected false)"));
  }

  /** Verifies full snapshot apply timings use a frame budget instead of a substep budget. */
  @Test
  public void debugReportUsesFrameBudgetForFullSnapshotApply() {
    NetworkTelemetry.recordSnapshotApplied(false, 10, 111, 0, 21_000_000L);
    NetworkTelemetry.recordSnapshotHandlerTiming(
        false, 10, 2_000L, 19_730_000L, 0L, 243_000L, 1_020_000L, false);

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();
    String plainText = report.plainText();

    assertTrue(hasBadSpanContaining(report, "21.00 ms"));
    assertTrue(hasBadSpanContaining(report, "19.73 ms"));
    assertTrue(plainText.contains("last=21.00 ms (expected <= 16.67 ms)"));
    assertTrue(plainText.contains("fullApply=19.73 ms (expected <= 16.67 ms)"));
    assertFalse(plainText.contains("fullApply=19.73 ms (expected <= 4.17 ms)"));
  }

  /** Verifies normal scheduling latency does not create noisy bad spans. */
  @Test
  public void debugReportKeepsSchedulingLatencyAndInactiveAckAgeNormal() {
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(10, 11, List.of(), List.of(), new LevelState(Set.of()));
    Session session = testSession((short) 1, 0);

    NetworkTelemetry.recordBaselineHealth((short) 1, 120, 0, true, 600, 60);
    NetworkTelemetry.recordServerSnapshot(
        NetworkTelemetry.buildServerSnapshot(1L, List.of(session)));
    NetworkTelemetry.recordFrameTime(17_120_000L);
    NetworkTelemetry.recordQueuedMessageDispatch(delta, 17_700_000L, 269_000L);
    NetworkTelemetry.recordInboundQueueDepth(3);

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();

    assertFalse(hasBadSpanContaining(report, "17.12 ms"));
    assertFalse(hasBadSpanContaining(report, "17.70 ms"));
    assertFalse(hasBadSpanContaining(report, "2.0s"));
  }

  /** Verifies ACK age is informational while baseline retention carries the health signal. */
  @Test
  public void debugReportMarksMissingAckBaselineBadWithoutDuplicatingAckAge() {
    Session session = testSession((short) 1, 0);
    NetworkTelemetry.recordBaselineHealth((short) 1, 590, 0, false, 600, 60);
    NetworkTelemetry.recordServerSnapshot(
        NetworkTelemetry.buildServerSnapshot(1L, List.of(session)));

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();

    assertFalse(hasBadSpanContaining(report, "9.8s"));
    assertTrue(hasBadSpanContaining(report, "false (expected true)"));
    assertFalse(report.plainText().contains("expected <= 8.0s"));
  }

  /** Verifies server client lines show the client-reported debug RTT. */
  @Test
  public void debugReportShowsServerClientDebugRttEstimate() {
    Session session = testSession((short) 1, 0);
    session.clientState().orElseThrow().recordDebugRttEstimate(13.9f, 1f);
    NetworkTelemetry.recordBaselineHealth((short) 1, 1, 0, true, 600, 60);
    NetworkTelemetry.recordServerSnapshot(
        NetworkTelemetry.buildServerSnapshot(1L, List.of(session)));

    NetworkTelemetryReport report = NetworkTelemetry.debugReport();

    assertTrue(report.plainText().contains("Client 1: udp=true rtt=13.9 ms"));
    assertFalse(hasBadSpanContaining(report, "13.9 ms"));
  }

  /** Verifies unknown sentinel values stay informational after reset. */
  @Test
  public void debugReportKeepsUnknownAndDisconnectedValuesNormal() {
    NetworkTelemetryReport report = NetworkTelemetry.debugReport();

    assertTrue(report.plainText().contains("disconnected"));
    assertTrue(report.plainText().contains("n/a"));
    assertFalse(hasBadSpanContaining(report, "disconnected"));
    assertFalse(hasBadSpanContaining(report, "n/a"));
  }

  private static boolean hasBadSpanContaining(NetworkTelemetryReport report, String text) {
    return report.sections().stream()
        .flatMap(section -> section.lines().stream())
        .flatMap(line -> line.spans().stream())
        .anyMatch(span -> span.severity() == TelemetrySeverity.BAD && span.text().contains(text));
  }

  private static long badSpanCount(NetworkTelemetryReport report, String text) {
    return report.sections().stream()
        .flatMap(section -> section.lines().stream())
        .flatMap(line -> line.spans().stream())
        .filter(span -> span.severity() == TelemetrySeverity.BAD)
        .map(TelemetrySpan::text)
        .filter(spanText -> spanText.contains(text))
        .count();
  }

  private static Session testSession(short clientId, int ackTick) {
    ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    when(context.channel()).thenReturn(channel);
    when(channel.isActive()).thenReturn(true);
    Session session =
        new Session(
            context,
            (target, message) -> CompletableFuture.completedFuture(true),
            (ctx, message) -> CompletableFuture.completedFuture(true));
    ClientState state =
        new ClientState(clientId, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
    if (ackTick >= 0) {
      state.snapshotSync().acknowledge(ackTick);
    }
    session.attachClientState(state);
    session.udpReady(true);
    return session;
  }
}
