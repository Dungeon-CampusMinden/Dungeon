package core.network.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.level.utils.Coordinate;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DoorTileState;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Direction;
import core.utils.Point;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for field-level snapshot delta compression. */
public class SnapshotDeltaCompressorTest {
  /** Verifies unchanged snapshots do not produce a delta. */
  @Test
  void compressReturnsEmptyWhenNothingChanged() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).position(new Point(1, 2)).build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).position(new Point(1, 2)).build()),
            new LevelState(Set.of()));

    assertTrue(SnapshotDeltaCompressor.compress(baseline, current).isEmpty());
  }

  /** Verifies changed fields are sent without unrelated unchanged fields. */
  @Test
  void compressIncludesOnlyChangedEntityFields() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder()
                    .entityId(1)
                    .position(new Point(1, 2))
                    .viewDirection(Direction.LEFT)
                    .currentHealth(5)
                    .build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(
                EntityState.builder()
                    .entityId(1)
                    .position(new Point(3, 4))
                    .viewDirection(Direction.LEFT)
                    .currentHealth(5)
                    .build()),
            new LevelState(Set.of()));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();
    EntityDelta entityDelta = delta.entityDeltas().getFirst();

    assertEquals(1, entityDelta.entityId());
    assertEquals(new Point(3, 4), entityDelta.changedState().position().orElseThrow());
    assertTrue(entityDelta.changedState().viewDirection().isEmpty());
    assertTrue(entityDelta.changedState().currentHealth().isEmpty());

    SnapshotMessage materialized =
        SnapshotDeltaCompressor.materializeChangedSnapshot(baseline, delta);
    EntityState merged = materialized.entities().getFirst();
    assertEquals(new Point(3, 4), merged.position().orElseThrow());
    assertEquals("LEFT", merged.viewDirection().orElseThrow());
    assertEquals(5, merged.currentHealth().orElseThrow());
  }

  /** Verifies fields can be explicitly cleared by a delta. */
  @Test
  void compressMarksRemovedFieldsAsCleared() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).tintColor(0x11223344).build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();
    assertTrue(
        delta.entityDeltas().getFirst().clearedFields().contains(EntityStateField.TINT_COLOR));

    SnapshotMessage materialized =
        SnapshotDeltaCompressor.materializeChangedSnapshot(baseline, delta);
    assertTrue(materialized.entities().getFirst().tintColor().isEmpty());
  }

  /** Verifies removals and level changes are represented in deltas. */
  @Test
  void compressIncludesRemovedEntitiesAndLevelDelta() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder().entityId(1).build(),
                EntityState.builder().entityId(2).build()),
            new LevelState(Set.of(new DoorTileState(new Coordinate(4, 7), false))));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).build()),
            new LevelState(Set.of(new DoorTileState(new Coordinate(4, 7), true))));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();

    assertEquals(List.of(2), delta.removedEntityIds());
    DoorTileState doorState =
        delta.levelStateDeltaOptional().orElseThrow().doorStates().iterator().next();
    assertEquals(new Coordinate(4, 7), doorState.coordinate());
    assertTrue(doorState.open());
  }
}
