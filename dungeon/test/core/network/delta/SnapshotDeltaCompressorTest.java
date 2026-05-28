package core.network.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import core.level.utils.Coordinate;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DoorTileState;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.InventorySlotState;
import core.network.messages.s2c.ItemState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Direction;
import core.utils.Point;
import java.util.List;
import java.util.Map;
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

    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);
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

    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);
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

  /** Verifies delta removals include entities created after the full baseline. */
  @Test
  void compressRemovesKnownEntitiesCreatedAfterBaseline() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            12, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));

    DeltaSnapshotMessage delta =
        SnapshotDeltaCompressor.compress(baseline, current, List.of(1, 2)).orElseThrow();

    assertEquals(List.of(2), delta.removedEntityIds());
  }

  /** Verifies materialized snapshots retain unchanged entities. */
  @Test
  void materializeSnapshotIncludesUnchangedEntities() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder().entityId(1).position(new Point(1, 1)).build(),
                EntityState.builder().entityId(2).position(new Point(2, 2)).build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(
                EntityState.builder().entityId(1).position(new Point(3, 3)).build(),
                EntityState.builder().entityId(2).position(new Point(2, 2)).build()),
            new LevelState(Set.of()));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();
    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);

    assertEquals(2, materialized.entities().size());
    assertEquals(new Point(2, 2), entity(materialized, 2).position().orElseThrow());
  }

  /** Verifies materialized snapshots remove deleted entities. */
  @Test
  void materializeSnapshotRemovesEntities() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder().entityId(1).build(),
                EntityState.builder().entityId(2).build()),
            new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(10, 11, List.of(), List.of(2), new LevelState(Set.of()));

    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);

    assertEquals(1, materialized.entities().size());
    assertEquals(1, materialized.entities().getFirst().entityId());
  }

  /** Verifies entity deltas are applied into full materialized entity state. */
  @Test
  void materializeSnapshotAppliesEntityDeltas() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).position(new Point(1, 1)).build()),
            new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1, EntityState.builder().entityId(1).currentHealth(7).build(), Set.of())),
            List.of(),
            null);

    EntityState materialized =
        SnapshotDeltaCompressor.materializeSnapshot(baseline, delta).entities().getFirst();

    assertEquals(new Point(1, 1), materialized.position().orElseThrow());
    assertEquals(7, materialized.currentHealth().orElseThrow());
  }

  /** Verifies level-state deltas override baseline door states. */
  @Test
  void materializeSnapshotMergesLevelState() {
    Coordinate changed = new Coordinate(1, 2);
    Coordinate unchanged = new Coordinate(3, 4);
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(),
            new LevelState(
                Set.of(new DoorTileState(changed, false), new DoorTileState(unchanged, true))));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10, 11, List.of(), List.of(), new LevelState(Set.of(new DoorTileState(changed, true))));

    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);

    assertTrue(door(materialized, changed).open());
    assertTrue(door(materialized, unchanged).open());
  }

  /** Verifies position reverts are detected against the acknowledged baseline. */
  @Test
  void compressDetectsPositionRevertAgainstAckedBaseline() {
    SnapshotMessage snapshotA =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).position(new Point(1, 1)).build()),
            new LevelState(Set.of()));
    SnapshotMessage snapshotB =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).position(new Point(2, 2)).build()),
            new LevelState(Set.of()));

    SnapshotMessage materializedB =
        SnapshotDeltaCompressor.materializeSnapshot(
            snapshotA, SnapshotDeltaCompressor.compress(snapshotA, snapshotB).orElseThrow());
    DeltaSnapshotMessage revert =
        SnapshotDeltaCompressor.compress(materializedB, snapshotA).orElseThrow();

    assertEquals(
        new Point(1, 1), revert.entityDeltas().getFirst().changedState().position().orElseThrow());
  }

  /** Verifies door reverts are detected against the acknowledged baseline. */
  @Test
  void compressDetectsDoorRevertAgainstAckedBaseline() {
    Coordinate coordinate = new Coordinate(1, 2);
    SnapshotMessage snapshotA =
        new SnapshotMessage(
            10, List.of(), new LevelState(Set.of(new DoorTileState(coordinate, false))));
    SnapshotMessage snapshotB =
        new SnapshotMessage(
            11, List.of(), new LevelState(Set.of(new DoorTileState(coordinate, true))));

    SnapshotMessage materializedB =
        SnapshotDeltaCompressor.materializeSnapshot(
            snapshotA, SnapshotDeltaCompressor.compress(snapshotA, snapshotB).orElseThrow());
    DeltaSnapshotMessage revert =
        SnapshotDeltaCompressor.compress(materializedB, snapshotA).orElseThrow();

    assertFalse(
        revert.levelStateDeltaOptional().orElseThrow().doorStates().iterator().next().open());
  }

  /** Verifies cleared fields are applied in the materialized data snapshot. */
  @Test
  void materializeSnapshotAppliesClearedFieldsAtDataLevel() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).metadata(Map.of("state", "active")).build()),
            new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).build(),
                    Set.of(EntityStateField.METADATA))),
            List.of(),
            null);

    EntityState materialized =
        SnapshotDeltaCompressor.materializeSnapshot(baseline, delta).entities().getFirst();

    assertTrue(materialized.metadata().isEmpty());
  }

  /** Verifies known entity removals are emitted after the baseline. */
  @Test
  void compressIncludesKnownEntityRemovalAfterBaseline() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            12, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));

    DeltaSnapshotMessage delta =
        SnapshotDeltaCompressor.compress(baseline, current, Set.of(1, 2)).orElseThrow();

    assertEquals(List.of(2), delta.removedEntityIds());
  }

  /** Verifies inventory stack-size changes are detected without mutable item baselines. */
  @Test
  void compressDetectsInventoryStackSizeChanges() {
    ItemPotionHealth baselineItem = new ItemPotionHealth(HealthPotionType.GREATER);
    baselineItem.stackSize(1);
    ItemPotionHealth currentItem = new ItemPotionHealth(HealthPotionType.GREATER);
    currentItem.stackSize(2);
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).inventory(new Item[] {baselineItem}).build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).inventory(new Item[] {currentItem}).build()),
            new LevelState(Set.of()));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();

    assertTrue(delta.entityDeltas().getFirst().changedState().inventory().isPresent());
    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);
    ItemState materializedItem =
        entity(materialized, 1).inventory().orElseThrow().getFirst().item();
    assertEquals(2, materializedItem.stackSize());
  }

  /** Verifies materialized inventory state does not expose mutable baseline item instances. */
  @Test
  void materializedInventoryDoesNotShareMutableItems() {
    ItemPotionHealth item = new ItemPotionHealth(HealthPotionType.GREATER);
    item.stackSize(1);
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).inventory(new Item[] {item}).build()),
            new LevelState(Set.of()));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1, EntityState.builder().entityId(1).currentHealth(7).build(), Set.of())),
            List.of(),
            null);

    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);
    InventorySlotState slot = entity(materialized, 1).inventory().orElseThrow().getFirst();
    Item firstItem = slot.item().toItem();
    Item secondItem = slot.item().toItem();
    firstItem.stackSize(5);

    assertNotSame(firstItem, secondItem);
    assertEquals(1, slot.item().stackSize());
    assertEquals(1, secondItem.stackSize());
  }

  /** Verifies empty current metadata clears baseline metadata in deltas. */
  @Test
  void compressClearsMetadataWhenCurrentMetadataIsEmpty() {
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).metadata(Map.of("state", "active")).build()),
            new LevelState(Set.of()));
    SnapshotMessage current =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).metadata(Map.of()).build()),
            new LevelState(Set.of()));

    DeltaSnapshotMessage delta = SnapshotDeltaCompressor.compress(baseline, current).orElseThrow();

    assertTrue(delta.entityDeltas().getFirst().clearedFields().contains(EntityStateField.METADATA));
    SnapshotMessage materialized = SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);
    assertTrue(entity(materialized, 1).metadata().isEmpty());
  }

  /** Regression test for client/server delta flow across A -> B -> A. */
  @Test
  void materializedDeltaFlowSupportsRevertToPreviousState() {
    SnapshotHistory serverHistory = new SnapshotHistory(4);
    SnapshotMessage snapshotA =
        new SnapshotMessage(
            10,
            List.of(EntityState.builder().entityId(1).position(new Point(1, 1)).build()),
            new LevelState(Set.of()));
    SnapshotMessage snapshotB =
        new SnapshotMessage(
            11,
            List.of(EntityState.builder().entityId(1).position(new Point(2, 2)).build()),
            new LevelState(Set.of()));
    serverHistory.add(snapshotA);

    DeltaSnapshotMessage deltaAB =
        SnapshotDeltaCompressor.compress(serverHistory.snapshot(10).orElseThrow(), snapshotB)
            .orElseThrow();
    SnapshotMessage materializedB = SnapshotDeltaCompressor.materializeSnapshot(snapshotA, deltaAB);
    serverHistory.add(snapshotB);

    DeltaSnapshotMessage deltaBA =
        SnapshotDeltaCompressor.compress(serverHistory.snapshot(11).orElseThrow(), snapshotA)
            .orElseThrow();
    SnapshotMessage materializedA =
        SnapshotDeltaCompressor.materializeSnapshot(materializedB, deltaBA);

    assertEquals(new Point(1, 1), entity(materializedA, 1).position().orElseThrow());
  }

  private static EntityState entity(SnapshotMessage snapshot, int entityId) {
    return snapshot.entities().stream()
        .filter(entity -> entity.entityId() == entityId)
        .findFirst()
        .orElseThrow();
  }

  private static DoorTileState door(SnapshotMessage snapshot, Coordinate coordinate) {
    return snapshot.levelState().doorStates().stream()
        .filter(door -> door.coordinate().equals(coordinate))
        .findFirst()
        .orElseThrow();
  }
}
