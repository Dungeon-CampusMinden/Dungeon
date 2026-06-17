package core.network.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.ArrayList;
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

  /** Verifies protected snapshots are retained outside the normal rolling capacity. */
  @Test
  void retainsProtectedSnapshotsBeyondRollingCapacity() {
    SnapshotHistory history = new SnapshotHistory(2);

    history.add(snapshot(1));
    history.add(snapshot(2), Set.of(1));
    history.add(snapshot(3), Set.of(1));
    history.add(snapshot(4), Set.of(1));

    assertTrue(history.contains(1));
    assertFalse(history.contains(2));
    assertTrue(history.contains(3));
    assertTrue(history.contains(4));
    assertEquals(3, history.size());
  }

  /** Verifies snapshots stop being protected when callers no longer include their tick. */
  @Test
  void evictsPreviouslyProtectedSnapshotWhenProtectionIsRemoved() {
    SnapshotHistory history = new SnapshotHistory(2);

    history.add(snapshot(1));
    history.add(snapshot(2), Set.of(1));
    history.add(snapshot(3), Set.of(1));
    history.add(snapshot(4), Set.of());

    assertFalse(history.contains(1));
    assertFalse(history.contains(2));
    assertTrue(history.contains(3));
    assertTrue(history.contains(4));
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

  /** Verifies stored snapshots are isolated from later source-list mutations. */
  @Test
  void storedSnapshotsAreNotAffectedByOriginalEntityListMutation() {
    SnapshotHistory history = new SnapshotHistory(2);
    List<EntityState> entities = new ArrayList<>();
    entities.add(EntityState.builder().entityId(1).build());
    SnapshotMessage snapshot = new SnapshotMessage(10, entities, new LevelState(Set.of()));

    history.add(snapshot);
    entities.add(EntityState.builder().entityId(2).build());

    SnapshotMessage stored = history.snapshot(10).orElseThrow();
    assertEquals(1, stored.entities().size());
    assertEquals(1, stored.entities().getFirst().entityId());
  }

  /** Verifies returned snapshots cannot mutate the retained history entry. */
  @Test
  void returnedSnapshotsCannotMutateHistory() {
    SnapshotHistory history = new SnapshotHistory(2);
    history.add(
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of())));

    SnapshotMessage returned = history.snapshot(10).orElseThrow();
    assertThrows(
        UnsupportedOperationException.class,
        () -> returned.entities().add(EntityState.builder().entityId(2).build()));

    SnapshotMessage secondRead = history.snapshot(10).orElseThrow();
    assertEquals(1, secondRead.entities().size());
    assertEquals(1, secondRead.entities().getFirst().entityId());
  }

  private static SnapshotMessage snapshot(int tick) {
    return new SnapshotMessage(tick, List.of(), new LevelState(Set.of()));
  }
}
