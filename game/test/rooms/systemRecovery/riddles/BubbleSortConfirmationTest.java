package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.SortProgramStickItem;

/** Checks that a server-side dialog response, not the initial interaction, consumes the USB. */
class BubbleSortConfirmationTest {
  @ParameterizedTest
  @CsvSource({"Abbrechen,true,false", "Einsetzen,false,false", "Einsetzen,true,true"})
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
        var factory = mockStatic(EntityFactory.class);
        var dialogs = mockStatic(DialogFactory.class);
        var popups = mockStatic(DialogUtils.class);
        var scheduler = mockStatic(EventScheduler.class);
        var effects = mockStatic(SkillTools.class)) {
      factory
          .when(() -> EntityFactory.bubbleSortMachine(any(), any()))
          .thenAnswer(
              call -> {
                interact.set(call.getArgument(1));
                return new Entity("machine");
              });
      factory
          .when(() -> EntityFactory.transportPackage(any(), anyInt()))
          .thenAnswer(call -> entityAt(call.getArgument(0)));
      factory
          .when(() -> EntityFactory.transportScanner(any(), anyFloat()))
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
      verify(inventory, times("Einsetzen".equals(choice) ? 1 : 0)).remove(stick);
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
