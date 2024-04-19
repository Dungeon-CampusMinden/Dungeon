package contrib.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.junit.After;
import org.junit.Test;

/** Tests for the Chest classes. */
public class ChestTest {

  /** WTF? . */
  @After
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** Checks the correct creation of the Chest. */
  @Test
  public void checkCreation() throws IOException {
    Set<Item> itemData = Set.of();
    Point position = new Point(0, 0);
    Entity c = null;
    c = EntityFactory.newChest(itemData, position);

    assertTrue(
        "Needs the AnimationComponent to be visible to the player.",
        c.fetch(DrawComponent.class).isPresent());
    Optional<InventoryComponent> inventoryComponent = c.fetch(InventoryComponent.class);
    assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
    assertEquals(
        "Chest should have the given Items",
        new Item[] {null, null, null, null, null, null, null, null, null, null, null, null},
        inventoryComponent.get().items());
    Optional<PositionComponent> positionComponent = c.fetch(PositionComponent.class);
    assertTrue(
        "Needs the PositionComponent to be somewhere in the Level", positionComponent.isPresent());
    assertTrue(
        "Position should be equal to the given Position",
        position.equals(positionComponent.map(PositionComponent.class::cast).get().position()));
  }

  /*
   * Checks the Chest Dropping all the Items it holds.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
   * cant be tested.
   */
  /* @Test
  public void checkInteractionDroppingItems() {
      Point position = new Point(0, 0);
      Entity c = EntityFactory.getChest(itemData, position);

     // assertEquals(1, Game.getEntitiesStream().count());
      c.getComponent(InteractionComponent.class)
              .map(InteractionComponent.class::cast)
              .get()
              .triggerInteraction();
     // assertEquals(2, Game.getEntitiesStream().count());
  }*/

  /*
   * Checks the dropped Item.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
   * cant be tested.
   */
  /*@Test
  public void checkInteractionOnDroppedItems() {
      List<Item> itemData = List.of(new ItemGenerator().generateItemData());
      Point position = new Point(0, 0);
      Entity c = EntityFactory.getChest(itemData, position);
      c.getComponent(InteractionComponent.class)
              .map(InteractionComponent.class::cast)
              .ifPresent(InteractionComponent::triggerInteraction);
      Game.removeEntity(c);

      assertEquals(1, Game.getEntitiesStream().count());
      Entity droppedItem = Game.getEntitiesStream().iterator().next();
      assertTrue(
              "droppedItem should have the HitboxComponent",
              droppedItem
                      .getComponent(CollideComponent.class)
                      .map(CollideComponent.class::cast)
                      .isPresent());
  }*/

  /** WTF? . */
  @Test
  public void checkGeneratorMethod() throws IOException {
    Game.add(new LevelSystem(null, null, () -> {}));

    Game.currentLevel(
        new TileLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.FLOOR,
              }
            },
            DesignLabel.DEFAULT));
    Entity newChest = EntityFactory.newChest();

    // assertTrue("Chest is added to Game", Game.getEntitiesStream().anyMatch(e -> e ==
    // newChest));
    assertTrue(
        "Needs the AnimationComponent to be visible to the player.",
        newChest.fetch(DrawComponent.class).isPresent());
    Optional<InventoryComponent> inventoryComponent = newChest.fetch(InventoryComponent.class);
    assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
    assertTrue(
        "Chest should have at least 1 Item",
        1 <= inventoryComponent.map(InventoryComponent.class::cast).get().items().length);

    assertEquals(
        "x Position has to be ILLEGAL",
        PositionComponent.ILLEGAL_POSITION.x,
        newChest
            .fetch(PositionComponent.class)
            .map(PositionComponent.class::cast)
            .get()
            .position()
            .x,
        0.00001f);
    assertEquals(
        "y Position has to be ILLEGAL",
        PositionComponent.ILLEGAL_POSITION.y,
        newChest
            .fetch(PositionComponent.class)
            .map(PositionComponent.class::cast)
            .get()
            .position()
            .y,
        0.00001f);
  }
}
