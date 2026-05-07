package core.network.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link SnapshotHistory}. */
class SnapshotHistoryTest {

  /** Verifies snapshots can be stored and found by tick. */
  @Test
  void storesAndFindsSnapshotsByTick() {
    SnapshotHistory history = new SnapshotHistory(2);
    SnapshotMessage snapshot = snapshot(10);

    history.add(snapshot);

    assertEquals(snapshot, history.snapshot(10).orElseThrow());
    assertTrue(history.contains(10));
  }

  /** Verifies the oldest snapshot is evicted when capacity is exceeded. */
  @Test
  void evictsOldestSnapshotsWhenCapacityExceeded() {
    SnapshotHistory history = new SnapshotHistory(2);

    history.add(snapshot(1));
    history.add(snapshot(2));
    history.add(snapshot(3));

    assertFalse(history.contains(1));
    assertTrue(history.contains(2));
    assertTrue(history.contains(3));
    assertEquals(2, history.size());
  }

  /** Verifies clear removes every retained snapshot. */
  @Test
  void clearRemovesAllSnapshots() {
    SnapshotHistory history = new SnapshotHistory(2);
    history.add(snapshot(1));

    history.clear();

    assertEquals(0, history.size());
    assertTrue(history.newest().isEmpty());
  }

  /** Verifies newest returns the latest inserted snapshot. */
  @Test
  void newestReturnsLatestSnapshot() {
    SnapshotHistory history = new SnapshotHistory(2);
    SnapshotMessage newest = snapshot(2);

    history.add(snapshot(1));
    history.add(newest);

    assertEquals(newest, history.newest().orElseThrow());
  }

  private static SnapshotMessage snapshot(int tick) {
    return new SnapshotMessage(tick, List.of(), new LevelState(Set.of()));
  }
}
