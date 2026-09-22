package rooms.systemRecovery.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.systems.LevelSystem;
import engine.utils.Point;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.petrinet.ProgressDebugSnapshot;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.story.SystemRecoveryDialogTriggers;

/** Guarantees that moving across story-only room triggers never advances puzzle progress. */
class SystemRecoveryDialogTriggerProgressTest {

  private SystemRecoveryLevel level;
  private Entity player;

  @BeforeEach
  void setUp() throws ReflectiveOperationException {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new LevelSystem());
    Game.add(new PetriNetSystem());
    Game.add(new HintSystem());
    SystemRecoveryProgressNet.initialize();

    Map<String, Point> points = new LinkedHashMap<>();
    int index = 0;
    for (SystemRecoveryDialogTriggers.DialogTrigger trigger :
        SystemRecoveryDialogTriggers.ROOM_ENTRY) {
      points.put(trigger.pointName(), new Point(index++ * 2, 2));
    }
    LevelElement[][] tiles = new LevelElement[5][index * 2 + 1];
    for (LevelElement[] row : tiles) {
      java.util.Arrays.fill(row, LevelElement.FLOOR);
    }
    level = new SystemRecoveryLevel(tiles, DesignLabel.DEFAULT, points);
    Field resolvedPoints = SystemRecoveryLevel.class.getDeclaredField("resolvedPoints");
    resolvedPoints.setAccessible(true);
    resolvedPoints.set(level, Map.copyOf(points));
    Game.currentLevel(level);

    player = new Entity("story-trigger-test-player");
    player.add(new PlayerComponent(true, "Tester"));
    player.add(new PositionComponent(new Point(0, 2)));
    Game.add(player);
  }

  @AfterEach
  void tearDown() {
    Game.currentLevel(null);
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void movingPlayerAcrossEveryStoryTriggerDoesNotChangeTheProgressMarking()
      throws ReflectiveOperationException {
    Method triggerTick = SystemRecoveryLevel.class.getDeclaredMethod("triggerDialogPoints");
    triggerTick.setAccessible(true);

    for (SystemRecoveryDialogTriggers.DialogTrigger trigger :
        SystemRecoveryDialogTriggers.ROOM_ENTRY) {
      player
          .fetch(PositionComponent.class)
          .orElseThrow()
          .position(level.getPoint(trigger.pointName()));
      triggerTick.invoke(level);

      ProgressDebugSnapshot snapshot = SystemRecoveryProgressNet.debugSnapshot();
      assertEquals(
          SystemRecoveryLearningStep.ENERGY_ARRAY,
          SystemRecoveryProgressNet.activeStep().orElseThrow());
      assertTrue(snapshot.consistent());
      assertEquals(1, snapshot.totalTokens());
      assertEquals(1, snapshot.tokenCounts().get("energy-array"));
    }
  }

  @Test
  void completedRoomTriggerDoesNotReplayAfterAProgressRestore()
      throws ReflectiveOperationException {
    Method enabled =
        SystemRecoveryLevel.class.getDeclaredMethod(
            "isDialogTriggerEnabled", SystemRecoveryDialogTriggers.DialogTrigger.class);
    enabled.setAccessible(true);
    SystemRecoveryDialogTriggers.DialogTrigger archiveTrigger =
        SystemRecoveryDialogTriggers.ROOM_ENTRY.stream()
            .filter(trigger -> trigger.pointName().equals(SystemRecoveryDialogTriggers.DATA_ARCHIVE))
            .findFirst()
            .orElseThrow();

    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.ARCHIVE_ARRAYS);
    assertTrue((boolean) enabled.invoke(level, archiveTrigger));

    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.SEARCH_PROGRAM);
    assertFalse((boolean) enabled.invoke(level, archiveTrigger));
  }
}
