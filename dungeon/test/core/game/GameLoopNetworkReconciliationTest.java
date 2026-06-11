package core.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.entities.CharacterClass;
import core.Entity;
import core.Game;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.network.server.ClientState;
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
}
