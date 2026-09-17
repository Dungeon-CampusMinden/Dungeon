package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.Item;
import feature.skills.SkillTools;
import feature.systems.EventScheduler;
import feature.utils.IAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rooms.systemRecovery.entities.SortingEntityFactory;
import rooms.systemRecovery.entities.TransportEntityFactory;
import rooms.systemRecovery.items.SortProgramStickItem;

/** Checks Bubble Sort insertion confirmation and synchronized comparison state. */
class BubbleSortConfirmationTest {
  @Test
  void keepsPublishedComparisonAlignedWithScannerUntilNextMovement_riddle6() {
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.getPoint(anyString()))
        .thenAnswer(
            call -> {
              String pointName = call.getArgument(0);
              if (pointName.startsWith("band")) {
                return new Point(Integer.parseInt(pointName.substring("band".length())), 0);
              }
              return new Point(0, 0);
            });
    Entity player = new Entity("player");
    InventoryComponent inventory = mock(InventoryComponent.class);
    player.add(inventory);
    SortProgramStickItem stick = mock(SortProgramStickItem.class);
    when(stick.programmed()).thenReturn(true);
    when(inventory.items()).thenReturn(new Item[] {stick});
    when(inventory.remove(stick)).thenReturn(Optional.of(stick));
    AtomicReference<BiConsumer<Entity, Entity>> interact = new AtomicReference<>();
    AtomicReference<Consumer<DialogResponseMessage.Payload>> respond = new AtomicReference<>();
    List<IAction> scheduledActions = new ArrayList<>();

    try (var game = mockStatic(Game.class, RETURNS_DEEP_STUBS);
        var factory = mockStatic(SortingEntityFactory.class);
        var transportFactory = mockStatic(TransportEntityFactory.class);
        var dialogs = mockStatic(DialogFactory.class);
        var scheduler = mockStatic(EventScheduler.class)) {
      factory
          .when(() -> SortingEntityFactory.bubbleSortMachine(any(), any()))
          .thenAnswer(
              call -> {
                interact.set(call.getArgument(1));
                return new Entity("machine");
              });
      transportFactory
          .when(() -> TransportEntityFactory.packageEntity(any(), anyInt()))
          .thenAnswer(call -> entityAt(call.getArgument(0)));
      transportFactory
          .when(() -> TransportEntityFactory.scanner(any(), anyFloat()))
          .thenAnswer(call -> entityAt(call.getArgument(0)));
      dialogs
          .when(
              () ->
                  DialogFactory.showMultipleChoiceDialog(
                      anyString(), anyString(), any(), anyBoolean(), any(), any(), eq(player.id())))
          .thenAnswer(
              call -> {
                respond.set(call.getArgument(4));
                return null;
              });
      scheduler
          .when(() -> EventScheduler.scheduleAction(any(IAction.class), eq(700L)))
          .thenAnswer(
              call -> {
                scheduledActions.add(call.getArgument(0));
                return mock(EventScheduler.ScheduledAction.class);
              });

      BubbleSortRiddle riddle = new BubbleSortRiddle(level, new TransportStorageRiddle(level));
      riddle.setup();
      interact.get().accept(new Entity("machine"), player);
      respond.get().accept(new DialogResponseMessage.StringValue("insert"));

      int[] comparisonWhileScannerIsAtBandZero = riddle.currentBeltSortEntityIds();
      assertEquals(1, scheduledActions.size());

      scheduledActions.get(0).execute();

      assertArrayEquals(
          comparisonWhileScannerIsAtBandZero,
          riddle.currentBeltSortEntityIds(),
          "The published pair must not advance before the scanner moves to the next pair");

      assertEquals(2, scheduledActions.size());
      scheduledActions.get(1).execute();

      int[] comparisonWhileScannerIsAtBandOne = riddle.currentBeltSortEntityIds();
      assertEquals(comparisonWhileScannerIsAtBandZero[1], comparisonWhileScannerIsAtBandOne[0]);
      assertEquals(comparisonWhileScannerIsAtBandZero[2], comparisonWhileScannerIsAtBandOne[2]);
    }
  }

  @ParameterizedTest
  @CsvSource({"cancel,true,false", "insert,false,false", "insert,true,true"})
  void consumesStickOnlyAfterConfirmationAndInventoryRecheck_riddle6(
      String choice, boolean stillPresent, boolean starts) {
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.getPoint(anyString())).thenReturn(new Point(0, 0));
    Entity player = new Entity("player");
    InventoryComponent inventory = mock(InventoryComponent.class);
    player.add(inventory);
    SortProgramStickItem stick = mock(SortProgramStickItem.class);
    when(stick.programmed()).thenReturn(true);
    when(inventory.items()).thenReturn(new Item[] {stick});
    when(inventory.remove(stick)).thenReturn(stillPresent ? Optional.of(stick) : Optional.empty());
    AtomicReference<BiConsumer<Entity, Entity>> interact = new AtomicReference<>();
    AtomicReference<Consumer<DialogResponseMessage.Payload>> respond = new AtomicReference<>();

    try (var game = mockStatic(Game.class, RETURNS_DEEP_STUBS);
        var factory = mockStatic(SortingEntityFactory.class);
        var transportFactory = mockStatic(TransportEntityFactory.class);
        var dialogs = mockStatic(DialogFactory.class);
        var popups = mockStatic(DialogUtils.class);
        var scheduler = mockStatic(EventScheduler.class);
        var effects = mockStatic(SkillTools.class)) {
      factory
          .when(() -> SortingEntityFactory.bubbleSortMachine(any(), any()))
          .thenAnswer(
              call -> {
                interact.set(call.getArgument(1));
                return new Entity("machine");
              });
      transportFactory
          .when(() -> TransportEntityFactory.packageEntity(any(), anyInt()))
          .thenAnswer(call -> entityAt(call.getArgument(0)));
      transportFactory
          .when(() -> TransportEntityFactory.scanner(any(), anyFloat()))
          .thenAnswer(call -> entityAt(call.getArgument(0)));
      dialogs
          .when(
              () ->
                  DialogFactory.showMultipleChoiceDialog(
                      anyString(), anyString(), any(), anyBoolean(), any(), any(), eq(player.id())))
          .thenAnswer(
              call -> {
                respond.set(call.getArgument(4));
                return null;
              });

      BubbleSortRiddle riddle = new BubbleSortRiddle(level, new TransportStorageRiddle(level));
      riddle.setup();
      interact.get().accept(new Entity("machine"), player);
      assertNotNull(respond.get());
      verify(inventory, never()).remove(stick);
      assertFalse(riddle.running());

      respond.get().accept(new DialogResponseMessage.StringValue(choice));
      assertEquals(starts, riddle.running());
      verify(inventory, times("insert".equals(choice) ? 1 : 0)).remove(stick);
      if (starts) {
        respond.get().accept(new DialogResponseMessage.StringValue(choice));
        verify(inventory, times(1)).remove(stick);
      }
    }
  }

  private static Entity entityAt(Point point) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    return entity;
  }
}
