package core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.network.messages.c2s.DebugPing;
import core.network.messages.c2s.SnapshotAck;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link NetworkTelemetry}. */
public class NetworkTelemetryTest {

  /** Clears global telemetry state before every test. */
  @BeforeEach
  public void setup() {
    NetworkTelemetry.reset();
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

    assertTrue(debugText.contains("Client snapshots: full=1 delta=1 stale(f/d)=1/1"));
    assertTrue(debugText.contains("last=delta@11 e/r=3/1"));
  }
}
