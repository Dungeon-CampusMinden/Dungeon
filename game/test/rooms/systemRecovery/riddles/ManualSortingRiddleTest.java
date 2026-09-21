package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import feature.components.CollideComponent;
import feature.entities.WorldItemBuilder;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.entities.SortingEntityFactory;
import rooms.systemRecovery.entities.SystemRecoveryDisplayFactory;
import rooms.systemRecovery.items.SortProgramStickItem;

/** Exercises the real riddle controller without requiring textures or a graphical client. */
class ManualSortingRiddleTest {
  private final List<Entity> containers = new ArrayList<>();
  private final DungeonLevel level = mock(DungeonLevel.class);
  private final BubbleSortRiddle machine = mock(BubbleSortRiddle.class);
  private final Entity player = new Entity("player");
  private final Entity reward = new Entity("reward");
  private MockedStatic<Game> game;
  private MockedStatic<SortingEntityFactory> factory;
  private MockedStatic<SystemRecoveryDisplayFactory> displays;
  private MockedStatic<DialogUtils> dialogs;
  private MockedStatic<DialogFactory> choiceDialogs;
  private MockedStatic<WorldItemBuilder> worldItems;
  private MockedConstruction<SortProgramStickItem> sticks;
  private ManualSortingRiddle riddle;

  @BeforeEach
  void setup() {
    SystemRecovery.configureDebugMode("--debug");
    game = mockStatic(Game.class, RETURNS_DEEP_STUBS);
    factory = mockStatic(SortingEntityFactory.class);
    displays = mockStatic(SystemRecoveryDisplayFactory.class);
    dialogs = mockStatic(DialogUtils.class);
    choiceDialogs = mockStatic(DialogFactory.class);
    worldItems = mockStatic(WorldItemBuilder.class);
    sticks = mockConstruction(SortProgramStickItem.class);
    game.when(Game::isHeadless).thenReturn(true);
    game.when(() -> Game.entityAtPoint(any())).thenAnswer(ignored -> Stream.empty());
    when(level.getPoint(anyString()))
        .thenAnswer(
            call -> {
              String name = call.getArgument(0);
              return name.startsWith("sort_data")
                  ? new Point(Integer.parseInt(name.substring(name.length() - 1)), 0)
                  : new Point(10, 10);
            });
    factory
        .when(() -> SortingEntityFactory.dataCrystal(any(), anyInt()))
        .thenAnswer(
            call -> {
              Entity container = new Entity("sort_data_" + call.getArgument(1));
              container.add(new PositionComponent(call.getArgument(0)));
              container.add(new CollideComponent());
              containers.add(container);
              return container;
            });
    displays
        .when(() -> SystemRecoveryDisplayFactory.moduleDisplay(any(), any(), any()))
        .thenAnswer(ignored -> new Entity("display"));
    worldItems
        .when(() -> WorldItemBuilder.buildWorldItem(any(SortProgramStickItem.class), any()))
        .thenReturn(reward);
    riddle = new ManualSortingRiddle(level, machine);
    riddle.setup();
  }

  @AfterEach
  void cleanup() {
    SystemRecovery.configureDebugMode();
    sticks.close();
    worldItems.close();
    dialogs.close();
    choiceDialogs.close();
    displays.close();
    factory.close();
    game.close();
  }

  @Test
  void swapsEntitiesAndAdvancesComparison_riddle5() {
    riddle.applySortChoice(true, player);

    assertEquals(new Point(1, 0), position(containers.get(0)));
    assertEquals(new Point(0, 0), position(containers.get(1)));
    assertArrayEquals(
        new int[] {containers.get(0).id(), containers.get(2).id()},
        riddle.currentSortComparisonEntityIds());
  }

  @Test
  void wrongAnswerRestoresOriginalOrderAndCollisionPositions_riddle5() {
    riddle.applySortChoice(true, player);
    riddle.applySortChoice(false, player); // 42 and 8 must be swapped.

    for (int index = 0; index < containers.size(); index++) {
      Entity container = containers.get(index);
      assertEquals(new Point(index, 0), position(container));
      assertEquals(
          position(container),
          container.fetch(CollideComponent.class).orElseThrow().collider().position());
    }
    assertArrayEquals(
        new int[] {containers.get(0).id(), containers.get(1).id()},
        riddle.currentSortComparisonEntityIds());
  }

  @Test
  void completesAfterResetAndAwardsOnlyOneStick_riddle5() {
    riddle.applySortChoice(false, player); // Wrong first answer resets the exercise.
    boolean[] choices = {true, true, true, true, true, false, true, false, false, false};
    for (boolean swap : choices) riddle.applySortChoice(swap, player);

    assertArrayEquals(new int[] {-1, -1}, riddle.currentSortComparisonEntityIds());
    int[] originalIndicesInSortedOrder = {2, 1, 4, 3, 0};
    for (int index = 0; index < originalIndicesInSortedOrder.length; index++) {
      assertEquals(
          new Point(index, 0), position(containers.get(originalIndicesInSortedOrder[index])));
    }
    riddle.applySortChoice(true, player);
    riddle.applySortChoice(false, player);
    assertEquals(1, sticks.constructed().size());
    game.verify(() -> Game.add(reward), times(1));
  }

  @Test
  void newRiddleInstanceDoesNotInheritPreviousProgress_riddle5() {
    riddle.applySortChoice(true, player);
    ManualSortingRiddle next = new ManualSortingRiddle(level, machine);
    next.setup();

    assertArrayEquals(
        new int[] {containers.get(5).id(), containers.get(6).id()},
        next.currentSortComparisonEntityIds());
    assertArrayEquals(
        new int[] {containers.get(0).id(), containers.get(2).id()},
        riddle.currentSortComparisonEntityIds());
  }

  @Test
  void runningMachineBlocksManualChoices_riddle5() {
    when(machine.running()).thenReturn(true);
    riddle.applySortChoice(true, player);
    assertArrayEquals(
        new int[] {containers.get(0).id(), containers.get(1).id()},
        riddle.currentSortComparisonEntityIds());
    assertEquals(new Point(0, 0), position(containers.get(0)));
  }

  @Test
  void debugSkipCompletesSortingAndAwardsOnlyOneStick_riddle5() {
    riddle.skipForDebug(player);
    riddle.skipForDebug(player);

    assertTrue(riddle.completed());
    assertArrayEquals(new int[] {-1, -1}, riddle.currentSortComparisonEntityIds());
    int[] originalIndicesInSortedOrder = {2, 1, 4, 3, 0};
    for (int index = 0; index < originalIndicesInSortedOrder.length; index++) {
      assertEquals(
          new Point(index, 0), position(containers.get(originalIndicesInSortedOrder[index])));
    }
    assertEquals(1, sticks.constructed().size());
    game.verify(() -> Game.add(reward), times(1));
  }

  private Point position(Entity entity) {
    return entity.fetch(PositionComponent.class).orElseThrow().position();
  }
}
