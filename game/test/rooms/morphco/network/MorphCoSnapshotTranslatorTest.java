package rooms.morphco.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.MessageDispatcher;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.timer.WorldTimerComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link MorphCoSnapshotTranslator}. */
class MorphCoSnapshotTranslatorTest {

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
  }

  @Test
  void shiftClockReachesTheClientAsStopwatch() {
    Entity serverClock = new Entity(7, "shift-clock");
    serverClock.add(new PositionComponent(new Point(1, 1)));
    serverClock.add(new DrawComponent(new SimpleIPath("rooms/morphCo/art/hotspot.png")));
    serverClock.add(WorldTimerComponent.stopwatch(1_234));
    Game.add(serverClock);
    SnapshotMessage snapshot = new MorphCoSnapshotTranslator().translateToSnapshot(1).orElseThrow();
    Game.removeAllEntities();
    Entity clientClock = new Entity(7, "shift-clock");
    Game.add(clientClock);

    new MorphCoSnapshotTranslator().applySnapshot(snapshot, new MessageDispatcher());

    assertEquals(
        WorldTimerComponent.stopwatch(1_234),
        clientClock.fetch(WorldTimerComponent.class).orElseThrow());
  }
}
