package feature.entities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for the Chest classes. */
public class ChestTest {

  /** WTF? . */
  @AfterEach
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

    assertTrue(c.isPresent(DrawComponent.class));
    Optional<InventoryComponent> inventoryComponent = c.fetch(InventoryComponent.class);
    assertTrue(inventoryComponent.isPresent());
    assertArrayEquals(
        new Item[] {null, null, null, null, null, null, null, null, null, null, null, null},
        inventoryComponent.get().items());
    Optional<PositionComponent> positionComponent = c.fetch(PositionComponent.class);
    assertTrue(positionComponent.isPresent());
    assertTrue(
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

}
