package feature.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests which inventories of a dialog {@link HeroController} lets a player act on. */
public class HeroControllerOwnInventoryTest {

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  /** The own inventory counts only while a dialog shows it, whichever place it has there. */
  @Test
  public void ownInventoryOnlyWhileADialogShowsItInAnyPlace() {
    Entity player = entityWithInventory("player");
    Entity pot = entityWithInventory("pot");

    assertTrue(HeroController.shownOwnInventory(player).isEmpty(), "no dialog shows the inventory");

    showInventories(player, pot, player);

    assertEquals(Optional.of(inventory(player)), HeroController.shownOwnInventory(player));
  }

  /** A move between the own inventory and a container takes both from the entities it names. */
  @Test
  public void movesBetweenOwnInventoryAndContainer() {
    Entity player = entityWithInventory("player");
    Entity pot = entityWithInventory("pot");
    showInventories(player, pot, player);
    Item item = addItem(pot);

    assertTrue(HeroController.moveItem(player, pot.id(), 0, player.id(), 0));
    assertEquals(Optional.of(item), inventory(player).get(0));
    assertTrue(inventory(pot).isEmpty());

    assertTrue(HeroController.moveItem(player, player.id(), 0, pot.id(), 0));
    assertEquals(Optional.of(item), inventory(pot).get(0));
    assertTrue(inventory(player).isEmpty());
  }

  /** A player cannot reach into an inventory that their dialog does not show. */
  @Test
  public void rejectsMoveNamingAnInventoryTheDialogDoesNotShow() {
    Entity player = entityWithInventory("player");
    Entity pot = entityWithInventory("pot");
    Entity otherPot = entityWithInventory("other pot");
    showInventories(player, pot, player);
    Item item = addItem(otherPot);

    assertFalse(HeroController.moveItem(player, otherPot.id(), 0, player.id(), 0));
    assertEquals(Optional.of(item), inventory(otherPot).get(0));
    assertTrue(inventory(player).isEmpty());
  }

  /** A dialog left open does not let a player reach a container they have walked away from. */
  @Test
  public void rejectsMoveNamingAContainerOutOfInteractionRange() {
    Entity player = entityWithInventory("player");
    Entity pot = entityWithInventory("pot");
    pot.add(new PositionComponent(0, 0));
    pot.add(new InteractionComponent(new Interaction((interacted, interactor) -> {}, 2f)));
    player.add(new PositionComponent(1, 0));
    showInventories(player, player, pot);
    Item item = addItem(pot);

    assertTrue(HeroController.moveItem(player, pot.id(), 0, player.id(), 0));

    player.fetch(PositionComponent.class).orElseThrow().position(new Point(5, 0));
    assertFalse(HeroController.moveItem(player, player.id(), 0, pot.id(), 0));
    assertEquals(Optional.of(item), inventory(player).get(0));
  }

  private static Entity entityWithInventory(String name) {
    Entity entity = new Entity(name);
    entity.add(new InventoryComponent(1));
    Game.add(entity);
    return entity;
  }

  private static InventoryComponent inventory(Entity entity) {
    return entity.fetch(InventoryComponent.class).orElseThrow();
  }

  private static Item addItem(Entity entity) {
    Item item = mock(Item.class);
    inventory(entity).add(item);
    return item;
  }

  private static void showInventories(Entity player, Entity first, Entity second) {
    DialogContext dialog =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DUAL_INVENTORY)
            .put(DialogContextKeys.ENTITY, first.id())
            .put(DialogContextKeys.SECONDARY_ENTITY, second.id())
            .build();
    player.add(new UIComponent(dialog, true, player.id()));
  }
}
