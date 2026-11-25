package feature.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import engine.Entity;
import engine.Game;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.inventory.Item;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests which inventory of a dialog {@link HeroController} takes as the player's own. */
public class HeroControllerOwnInventoryTest {

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  /** A dialog that lists the container first still drops from the player's own inventory. */
  @Test
  public void dropsFromOwnInventoryWhateverItsPlaceInTheDialog() {
    Entity player = new Entity("player");
    InventoryComponent own = new InventoryComponent(1);
    player.add(own);
    Entity pot = new Entity("pot");
    pot.add(new InventoryComponent(1));
    Game.add(player);
    Game.add(pot);

    assertTrue(HeroController.shownOwnInventory(player).isEmpty(), "no dialog shows the inventory");

    DialogContext dialog =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DUAL_INVENTORY)
            .put(DialogContextKeys.ENTITY, pot.id())
            .put(DialogContextKeys.SECONDARY_ENTITY, player.id())
            .build();
    player.add(new UIComponent(dialog, true, player.id()));

    assertEquals(Optional.of(own), HeroController.shownOwnInventory(player));
  }

  /** A negative slot is the player's own inventory, also in a dialog that lists it second. */
  @Test
  public void movesBetweenOwnInventoryAndContainerListedFirst() {
    Entity player = new Entity("player");
    InventoryComponent own = new InventoryComponent(1);
    player.add(own);
    Entity pot = new Entity("pot");
    InventoryComponent potInventory = new InventoryComponent(1);
    Item item = mock(Item.class);
    potInventory.add(item);
    pot.add(potInventory);
    Game.add(player);
    Game.add(pot);
    DialogContext dialog =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DUAL_INVENTORY)
            .put(DialogContextKeys.ENTITY, pot.id())
            .put(DialogContextKeys.SECONDARY_ENTITY, player.id())
            .build();
    player.add(new UIComponent(dialog, true, player.id()));

    assertTrue(HeroController.moveItem(player, 0, -1));

    assertEquals(Optional.of(item), own.get(0));
    assertTrue(potInventory.get(0).isEmpty());
  }
}
