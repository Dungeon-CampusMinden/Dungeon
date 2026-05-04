package core.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.entities.CharacterClass;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link ClientState}. */
public class ClientStateTest {

  /** Verifies client-side network entity tracking can be updated and cleared. */
  @Test
  public void networkSyncedEntityIdsTracksAndClearsEntities() {
    ClientState state = clientState();

    state.trackNetworkEntity(10);
    state.trackNetworkEntity(11);
    state.untrackNetworkEntity(10);

    assertEquals(Set.of(11), state.networkSyncedEntityIds());

    state.clearNetworkEntities();

    assertTrue(state.networkSyncedEntityIds().isEmpty());
  }

  /** Verifies server-side snapshot entity tracking follows the active full baseline window. */
  @Test
  public void knownSnapshotEntityIdsTracksFullBaselineAndDeltaEntities() {
    ClientState state = clientState();
    SnapshotMessage snapshot =
        new SnapshotMessage(
            20, List.of(EntityState.builder().entityId(1).build()), new LevelState(Set.of()));

    state.resetKnownSnapshotEntityIds(snapshot);
    state.trackKnownSnapshotEntityIds(List.of(2));

    assertEquals(Set.of(1, 2), state.knownSnapshotEntityIds());

    state.clearSnapshotBaseline();

    assertTrue(state.knownSnapshotEntityIds().isEmpty());
  }

  private static ClientState clientState() {
    return new ClientState((short) 1, "tester", 1, new byte[] {1, 2, 3}, CharacterClass.WIZARD);
  }
}
