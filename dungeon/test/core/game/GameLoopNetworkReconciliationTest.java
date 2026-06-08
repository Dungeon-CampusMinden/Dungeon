package core.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.entities.CharacterClass;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.network.delta.SnapshotDeltaCompressor;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DoorTileState;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
import core.utils.Direction;
import core.utils.Point;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for client-side network entity reconciliation in {@link GameLoop}. */
public class GameLoopNetworkReconciliationTest {

  /** Cleans up global game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  /** Verifies authoritative full snapshots remove only tracked network entities. */
  @Test
  public void reconcileNetworkEntitiesRemovesOnlyTrackedEntitiesMissingFromSnapshot() {
    ClientState state =
        new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
    Entity keptNetworkEntity = new Entity();
    Entity staleNetworkEntity = new Entity();
    Entity localEntity = new Entity();
    Game.add(keptNetworkEntity);
    Game.add(staleNetworkEntity);
    Game.add(localEntity);
    state.trackNetworkEntity(keptNetworkEntity.id());
    state.trackNetworkEntity(staleNetworkEntity.id());
    SnapshotMessage fullSnapshot =
        new SnapshotMessage(
            20,
            List.of(EntityState.builder().entityId(keptNetworkEntity.id()).build()),
            new LevelState(Set.of()));

    GameLoop.reconcileNetworkEntities(state, fullSnapshot);

    assertTrue(Game.findEntityById(keptNetworkEntity.id()).isPresent());
    assertTrue(Game.findEntityById(localEntity.id()).isPresent());
    assertTrue(Game.findEntityById(staleNetworkEntity.id()).isEmpty());
    assertTrue(state.networkSyncedEntityIds().contains(keptNetworkEntity.id()));
    assertFalse(state.networkSyncedEntityIds().contains(staleNetworkEntity.id()));
  }

  /** Verifies delta application forwards only changed entities with materialized field state. */
  @Test
  public void changedSnapshotForDeltaContainsOnlyMaterializedChangedEntities() {
    Coordinate changedDoor = new Coordinate(2, 3);
    SnapshotMessage baseline =
        new SnapshotMessage(
            10,
            List.of(
                EntityState.builder()
                    .entityId(1)
                    .position(new Point(1, 1))
                    .viewDirection(Direction.LEFT)
                    .build(),
                EntityState.builder().entityId(2).position(new Point(5, 5)).build()),
            new LevelState(Set.of(new DoorTileState(changedDoor, false))));
    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            10,
            11,
            List.of(
                new EntityDelta(
                    1,
                    EntityState.builder().entityId(1).position(new Point(3, 4)).build(),
                    Set.of())),
            List.of(),
            new LevelState(Set.of(new DoorTileState(changedDoor, true))));
    SnapshotMessage materializedSnapshot =
        SnapshotDeltaCompressor.materializeSnapshot(baseline, delta);

    SnapshotMessage changedSnapshot = GameLoop.changedSnapshotForDelta(delta, materializedSnapshot);

    assertEquals(1, changedSnapshot.entities().size());
    EntityState changedEntity = changedSnapshot.entities().getFirst();
    assertEquals(1, changedEntity.entityId());
    assertEquals(new Point(3, 4), changedEntity.position().orElseThrow());
    assertEquals("LEFT", changedEntity.viewDirection().orElseThrow());
    assertEquals(1, changedSnapshot.levelState().doorStates().size());
    assertTrue(changedSnapshot.levelState().doorStates().iterator().next().open());
  }
}
