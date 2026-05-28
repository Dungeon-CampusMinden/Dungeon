package core.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  /** Verifies clear resets ack and full-snapshot state. */
  @Test
  void clearResetsState() {
    ClientSnapshotSyncState state = new ClientSnapshotSyncState();

    state.acknowledge(10);
    state.markFullSnapshotSent(10);
    state.clear();

    assertEquals(-1, state.lastAckedSnapshotTick());
    assertEquals(-1, state.lastFullSnapshotTick());
    assertFalse(state.hasAck());
  }
}
