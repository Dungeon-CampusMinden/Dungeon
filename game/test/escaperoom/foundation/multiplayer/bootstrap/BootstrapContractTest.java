package escaperoom.foundation.multiplayer.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.components.PositionComponent;
import engine.network.MessageDispatcher;
import engine.network.messages.s2c.EntitySpawnBatch;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** Room-identity marker and normal initial-batch bootstrap contracts. */
final class BootstrapContractTest {
  private static final String ROOM_INPUT_SHA_256 = "a".repeat(64);

  @Test
  void roomIdentityIsRawCanonicalMarkerMetadata() {
    EntitySpawnEvent marker = BootstrapMarker.event(50, ROOM_INPUT_SHA_256);

    assertEquals(Map.of("foundation.bootstrap", ROOM_INPUT_SHA_256), marker.metadata());
    assertEquals(ROOM_INPUT_SHA_256, BootstrapMarker.decode(marker));
    assertThrows(
        IllegalArgumentException.class,
        () -> BootstrapMarker.event(50, ROOM_INPUT_SHA_256.toUpperCase()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            BootstrapMarker.decode(
                EntitySpawnEvent.builder()
                    .entityId(50)
                    .metadata(Map.of(BootstrapMarker.METADATA_KEY, "a".repeat(63)))
                    .build()));
  }

  @Test
  void buffersUntilMarkerAndPreservesOrdinaryEntityOrderAcrossBatches() {
    MessageDispatcher dispatcher = new MessageDispatcher();
    List<EntitySpawnEvent> released = new ArrayList<>();
    List<String> failures = new ArrayList<>();
    dispatcher.registerHandler(EntitySpawnEvent.class, (session, event) -> released.add(event));
    AtomicReference<String> receivedIdentity = new AtomicReference<>();
    ClientBootstrapCoordinator coordinator =
        new ClientBootstrapCoordinator(dispatcher, receivedIdentity::set, failures::add);
    coordinator.install();
    EntitySpawnEvent marker = BootstrapMarker.event(50, ROOM_INPUT_SHA_256);
    EntitySpawnEvent first = EntitySpawnEvent.builder().entityId(1).build();
    EntitySpawnEvent second =
        EntitySpawnEvent.builder()
            .entityId(2)
            .positionComponent(new PositionComponent(new Point(4, 5)))
            .build();
    EntitySpawnEvent third =
        EntitySpawnEvent.builder().entityId(3).metadata(Map.of("ordinary", "state")).build();
    EntitySpawnEvent fourth = EntitySpawnEvent.builder().entityId(4).build();

    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(first, second)));

    assertTrue(released.isEmpty());
    assertFalse(coordinator.bootstrapComplete());
    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(third, marker, fourth)));

    assertEquals(List.of(first, second, third, fourth), released);
    assertEquals(ROOM_INPUT_SHA_256, receivedIdentity.get());
    assertTrue(failures.isEmpty());
    assertTrue(coordinator.bootstrapComplete());

    EntitySpawnEvent followup = EntitySpawnEvent.builder().entityId(5).build();
    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(followup)));

    assertEquals(List.of(first, second, third, fourth, followup), released);
  }

  @Test
  void resetDropsOldBufferedSpawnsAndRetainsRoomIdentity() {
    MessageDispatcher dispatcher = new MessageDispatcher();
    List<EntitySpawnEvent> released = new ArrayList<>();
    List<String> failures = new ArrayList<>();
    dispatcher.registerHandler(EntitySpawnEvent.class, (session, event) -> released.add(event));
    ClientBootstrapCoordinator coordinator =
        new ClientBootstrapCoordinator(dispatcher, roomInputSha256 -> {}, failures::add);
    coordinator.install();
    EntitySpawnEvent marker = BootstrapMarker.event(50, ROOM_INPUT_SHA_256);
    EntitySpawnEvent abandoned = EntitySpawnEvent.builder().entityId(1).build();
    EntitySpawnEvent current = EntitySpawnEvent.builder().entityId(2).build();

    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(marker)));
    coordinator.resetConnection();
    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(abandoned)));
    coordinator.resetConnection();
    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(current, marker)));

    assertEquals(List.of(current), released);
    assertTrue(failures.isEmpty());
    assertTrue(coordinator.bootstrapComplete());

    EntitySpawnEvent changedMarker = BootstrapMarker.event(51, "b".repeat(64));
    EntitySpawnEvent rejected = EntitySpawnEvent.builder().entityId(3).build();
    coordinator.resetConnection();
    dispatcher.dispatch(null, new EntitySpawnBatch(List.of(rejected, changedMarker)));

    assertEquals(List.of(current), released);
    assertEquals(1, failures.size());
    assertFalse(coordinator.bootstrapComplete());
  }
}
