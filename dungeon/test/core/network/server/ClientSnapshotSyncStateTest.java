package core.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link ClientSnapshotSyncState}. */
class ClientSnapshotSyncStateTest {

  /** Verifies acknowledgements store the highest tick. */
  @Test
  void acknowledgeStoresHighestAckOnly() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.acknowledge(12);

    assertEquals(12, state.lastAckedSnapshotTick());
    assertTrue(state.hasAck());
  }

  /** Verifies old acknowledgements do not move state backwards. */
  @Test
  void olderAckDoesNotMoveBackwards() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.acknowledge(9);

    assertEquals(10, state.lastAckedSnapshotTick());
  }

  /** Verifies a full snapshot is due before any full snapshot was sent. */
  @Test
  void fullSnapshotDueWhenNoFullSent() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    assertTrue(state.fullSnapshotDue(10, 5));
  }

  /** Verifies the full snapshot interval is respected. */
  @Test
  void fullSnapshotDueAfterInterval() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.markFullSnapshotSent(10);

    assertFalse(state.fullSnapshotDue(14, 5));
    assertTrue(state.fullSnapshotDue(15, 5));
  }

  /** Verifies scheduled full snapshots also pace the next full snapshot attempt. */
  @Test
  void fullSnapshotDueUsesScheduledAttempts() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.markFullSnapshotScheduled(10);

    assertFalse(state.fullSnapshotDue(14, 5));
    assertTrue(state.fullSnapshotDue(15, 5));
  }

  /** Verifies failed full-snapshot attempts can be retried immediately. */
  @Test
  void failedFullSnapshotAttemptClearsScheduleGate() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.markFullSnapshotScheduled(10);
    state.markFullSnapshotSendFailed(10);

    assertTrue(state.fullSnapshotDue(11, 5));
  }

  /** Verifies recovery full snapshots are rate-limited while one is in flight. */
  @Test
  void recoveryFullSnapshotWaitsForRetryIntervalWhenUnackedFullIsInFlight() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.markFullSnapshotScheduled(20);

    assertFalse(state.fullSnapshotRecoveryDue(79, 60));
    assertTrue(state.fullSnapshotRecoveryDue(80, 60));
  }

  /** Verifies recovery can send immediately after the acknowledged baseline catches up. */
  @Test
  void recoveryFullSnapshotDueAfterAckCatchesFullAttempt() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.markFullSnapshotScheduled(20);
    state.acknowledge(20);

    assertTrue(state.fullSnapshotRecoveryDue(21, 60));
  }

  /** Verifies protected ticks include acknowledged and in-flight snapshot baselines. */
  @Test
  void protectedSnapshotTicksIncludeAckAndInFlightSnapshots() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.markFullSnapshotScheduled(20);
    state.markDeltaSnapshotSent(21);

    assertEquals(Set.of(10, 20, 21), state.protectedSnapshotTicks());
  }

  /** Verifies clear resets ack and full-snapshot state. */
  @Test
  void clearResetsState() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.markFullSnapshotSent(10);
    state.markDeltaSnapshotSent(11);
    state.clear();

    assertEquals(-1, state.lastAckedSnapshotTick());
    assertEquals(-1, state.lastFullSnapshotTick());
    assertEquals(-1, state.lastFullSnapshotAttemptTick());
    assertEquals(-1, state.lastDeltaSnapshotTick());
    assertFalse(state.hasAck());
    assertTrue(state.protectedSnapshotTicks().isEmpty());
  }
}
