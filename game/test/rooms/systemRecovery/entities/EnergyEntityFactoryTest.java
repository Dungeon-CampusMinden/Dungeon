package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.items.BatteryItem;

class EnergyEntityFactoryTest {

  @Test
  void markingInsertionTurnsOnAndRemovesTheBoxInventoryOnlyOnce() {
    Entity box = new Entity("battery-box");
    DrawComponent draw = mock(DrawComponent.class);
    InventoryComponent inventory = new InventoryComponent(1);
    assertTrue(inventory.add(new BatteryItem()));
    box.add(draw);
    box.add(inventory);

    EnergyEntityFactory.markBatteryInserted(box);
    EnergyEntityFactory.markBatteryInserted(box);

    verify(draw, times(1)).sendSignal("battery_inserted");
    assertFalse(box.isPresent(InventoryComponent.class));
  }

  @Test
  void insertingBatteryClosesOnlyTheDualInventoryForThatBoxBeforeRunningTheCallback() {
    DrawComponent draw = mock(DrawComponent.class);
    boolean[] callbackSawClosedInventory = {false};
    Entity[] boxReference = new Entity[1];
    Entity box =
        EnergyEntityFactory.batteryBox(
            new Point(0, 0),
            () ->
                callbackSawClosedInventory[0] =
                    !boxReference[0].isPresent(InventoryComponent.class));
    boxReference[0] = box;
    box.add(draw);

    Entity player = new Entity(101);
    DialogContext context =
        new DialogContext(
            DialogType.DefaultTypes.DUAL_INVENTORY,
            true,
            Map.of(
                DialogContextKeys.ENTITY, player.id(),
                DialogContextKeys.SECONDARY_ENTITY, box.id()));
    context.owner(player.id());
    UIComponent inventoryDialog = new UIComponent(context, true, true, player.id());
    player.add(inventoryDialog);

    Entity otherPlayer = new Entity(102);
    DialogContext otherContext =
        new DialogContext(
            DialogType.DefaultTypes.DUAL_INVENTORY,
            true,
            Map.of(
                DialogContextKeys.ENTITY,
                otherPlayer.id(),
                DialogContextKeys.SECONDARY_ENTITY,
                box.id() + 1));
    otherContext.owner(otherPlayer.id());
    UIComponent otherInventoryDialog = new UIComponent(otherContext, true, true, otherPlayer.id());
    otherPlayer.add(otherInventoryDialog);

    try (MockedStatic<Game> game = org.mockito.Mockito.mockStatic(Game.class);
        MockedStatic<UIUtils> uiUtils = org.mockito.Mockito.mockStatic(UIUtils.class)) {
      game.when(Game::allPlayers).thenReturn(Stream.of(player, otherPlayer));
      box.fetch(InventoryComponent.class).orElseThrow().add(new BatteryItem());

      uiUtils.verify(() -> UIUtils.closeDialog(inventoryDialog, true), times(1));
      uiUtils.verifyNoMoreInteractions();
      verify(draw, times(1)).sendSignal("battery_inserted");
      assertFalse(box.isPresent(InventoryComponent.class));
      assertTrue(callbackSawClosedInventory[0]);
    }
  }
}
