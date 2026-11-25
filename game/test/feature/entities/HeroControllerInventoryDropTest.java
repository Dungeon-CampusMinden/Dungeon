package feature.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests which inventory {@link HeroController} lets a player drop items from. */
public class HeroControllerInventoryDropTest {

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
}
