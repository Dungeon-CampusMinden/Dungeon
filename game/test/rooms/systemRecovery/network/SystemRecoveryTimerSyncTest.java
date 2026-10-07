package rooms.systemRecovery.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import engine.Entity;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.timer.WorldTimerComponent;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemRecoveryTimerSyncTest {

  @Test
  void initialSpawnAndSnapshotCarryTheSameServerTimerState() {
    Entity serverTimer = new Entity("system_recovery_timer");
    serverTimer.add(new PositionComponent(new Point(4, 7)));
    serverTimer.add(new WorldTimerComponent(1_800_000_000, 1234));
    Map<String, String> spawn =
        new SystemRecoveryEntitySpawnStrategy()
            .buildSpawnEvent(serverTimer)
            .orElseThrow()
            .metadata();
    Map<String, String> snapshot =
        new SystemRecoverySnapshotTranslator().snapshotMetadata(serverTimer);

    assertEquals(
        spawn.get(SystemRecoveryTimerSync.TIMESTAMP),
        snapshot.get(SystemRecoveryTimerSync.TIMESTAMP));
    assertEquals("1234", spawn.get(SystemRecoveryTimerSync.DURATION));
    Entity clientTimer = new Entity("client-timer");
    SystemRecoveryComponentSync.applyEntityMetadata(clientTimer, spawn);
    assertEquals(1234, clientTimer.fetch(WorldTimerComponent.class).orElseThrow().duration());

    serverTimer.add(new WorldTimerComponent(1_800_000_001, 1233));
    SystemRecoveryComponentSync.applyEntityMetadata(
        clientTimer, new SystemRecoverySnapshotTranslator().snapshotMetadata(serverTimer));
    assertEquals(1233, clientTimer.fetch(WorldTimerComponent.class).orElseThrow().duration());
  }

  @Test
  void displayIsAnchoredToTheLocalReceiveTimeInsteadOfTheServerClock() {
    Entity client = new Entity();

    // The client clock is five minutes ahead of the server.
    SystemRecoveryTimerSync.apply(client, serverUpdate(1000, 600), 1300);
    assertEquals(
        new WorldTimerComponent(1300, 600), client.fetch(WorldTimerComponent.class).orElseThrow());

    // A repeated snapshot of the same server update must not restart the local second.
    SystemRecoveryTimerSync.apply(client, serverUpdate(1000, 600), 1301);
    assertEquals(
        new WorldTimerComponent(1300, 600), client.fetch(WorldTimerComponent.class).orElseThrow());

    // A frozen countdown only advances the server timestamp; re-anchoring keeps it frozen.
    SystemRecoveryTimerSync.apply(client, serverUpdate(1001, 600), 1302);
    assertEquals(
        new WorldTimerComponent(1302, 600), client.fetch(WorldTimerComponent.class).orElseThrow());
  }

  private static Map<String, String> serverUpdate(int timestamp, int duration) {
    return Map.of(
        SystemRecoveryTimerSync.TIMESTAMP,
        String.valueOf(timestamp),
        SystemRecoveryTimerSync.DURATION,
        String.valueOf(duration));
  }

  @Test
  void malformedOrUnrelatedMetadataDoesNotRemoveAValidTimer() {
    Entity timer = new Entity();
    WorldTimerComponent previous = new WorldTimerComponent(1000, 600);
    timer.add(previous);

    for (Map<String, String> metadata :
        java.util.List.of(
            Map.<String, String>of(),
            Map.of(SystemRecoveryTimerSync.DURATION, "600"),
            Map.of(
                SystemRecoveryTimerSync.TIMESTAMP,
                "not a number",
                SystemRecoveryTimerSync.DURATION,
                "600"),
            Map.of(
                SystemRecoveryTimerSync.TIMESTAMP,
                "1000",
                SystemRecoveryTimerSync.DURATION,
                "-1"))) {
      SystemRecoveryTimerSync.apply(timer, metadata);
      assertSame(previous, timer.fetch(WorldTimerComponent.class).orElseThrow());
    }
  }

  @Test
  void zeroBudgetIsSynchronizedForTheFailureScreen() {
    Entity timer = new Entity();
    timer.add(new WorldTimerComponent(1000, 0));
    Map<String, String> metadata = new HashMap<>();
    SystemRecoveryTimerSync.append(timer, metadata);
    Entity client = new Entity();

    SystemRecoveryTimerSync.apply(client, metadata);

    assertEquals(0, client.fetch(WorldTimerComponent.class).orElseThrow().duration());
  }
}
