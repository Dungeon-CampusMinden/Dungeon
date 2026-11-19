package engine.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import engine.System;
import engine.systems.FrictionSystem;
import engine.systems.MoveSystem;
import engine.systems.VelocitySystem;
import feature.questlog.QuestLogSyncSystem;
import feature.systems.LevelTickSystem;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the order in which {@link ECSManagement} keeps its systems. */
class ECSManagementTest {

  @BeforeEach
  void setup() {
    ECSManagement.removeAllSystems();
  }

  @AfterEach
  void cleanup() {
    ECSManagement.removeAllSystems();
  }

  /** Tests that the order of the systems follows {@link SystemOrder}, not the order of adding. */
  @Test
  void addSortsSystemsIntoTheDefinedOrder() {
    ECSManagement.add(new QuestLogSyncSystem());
    ECSManagement.add(new MoveSystem());
    ECSManagement.add(new RoomSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new LevelTickSystem());

    assertEquals(
        List.of(
            LevelTickSystem.class,
            FrictionSystem.class,
            VelocitySystem.class,
            MoveSystem.class,
            RoomSystem.class,
            QuestLogSyncSystem.class),
        List.copyOf(ECSManagement.systems().keySet()));
  }

  /** Tests that a system added again takes the place of the stored system of its type. */
  @Test
  void addReplacesASystemInPlace() {
    ECSManagement.add(new RoomSystem());
    ECSManagement.add(new OtherRoomSystem());
    System replacement = new RoomSystem();

    ECSManagement.add(replacement);

    assertEquals(
        List.of(RoomSystem.class, OtherRoomSystem.class),
        List.copyOf(ECSManagement.systems().keySet()));
    assertSame(replacement, ECSManagement.systems().get(RoomSystem.class));
  }

  /** A system {@link SystemOrder} does not name. */
  private static final class RoomSystem extends System {
    @Override
    public void execute() {}
  }

  /** A second system {@link SystemOrder} does not name. */
  private static final class OtherRoomSystem extends System {
    @Override
    public void execute() {}
  }
}
