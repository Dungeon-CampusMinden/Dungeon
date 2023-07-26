package contrib.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.position.Point;
import core.utils.position.Position;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class ChestTest {

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
    }

    /** checks the correct creation of the Chest */
    @Test
    public void checkCreation() throws IOException {
        Set<ItemData> itemData = Set.of();
        Position position = new Point(0, 0);
        Entity c = null;
        c = EntityFactory.newChest(itemData, position);

        assertTrue(
                "Needs the AnimationComponent to be visible to the player.",
                c.fetch(DrawComponent.class).isPresent());
        Optional<InventoryComponent> inventoryComponent = c.fetch(InventoryComponent.class);
        assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
        assertEquals(
                "Chest should have the given Items",
                itemData,
                inventoryComponent.map(InventoryComponent.class::cast).get().items());
        Optional<PositionComponent> positionComponent = c.fetch(PositionComponent.class);
        assertTrue(
                "Needs the PositionComponent to be somewhere in the Level",
                positionComponent.isPresent());
        assertEquals(
                "Position should be equal to the given Position",
                position,
                positionComponent.map(PositionComponent.class::cast).get().position());
    }

    /**
     * checks the Chest Dropping all the Items it holds
     *
     * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
     * cant be tested.
     */
    /* @Test
    public void checkInteractionDroppingItems() {
        List<ItemData> itemData = List.of(new ItemDataGenerator().generateItemData());
        Position position = new Position(0, 0);
        Entity c = EntityFactory.getChest(itemData, position);

       // assertEquals(1, Game.getEntitiesStream().count());
        c.getComponent(InteractionComponent.class)
                .map(InteractionComponent.class::cast)
                .get()
                .triggerInteraction();
       // assertEquals(2, Game.getEntitiesStream().count());
    }*/

    /**
     * checks the dropped Item
     *
     * <p>Since we cant update the {@link Game#entities} from outside the gameloop, this is testcase
     * cant be tested.
     */
    /* @Test
    public void checkInteractionOnDroppedItems() {
        List<ItemData> itemData = List.of(new ItemDataGenerator().generateItemData());
        Position position = new Position(0, 0);
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
    @Test
    public void checkGeneratorMethod() throws IOException {
        Game.addSystem(new LevelSystem(null, null, () -> {}));

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
                "Chest should have atleast 1 Item",
                1 <= inventoryComponent.map(InventoryComponent.class::cast).get().items().size());
        assertEquals(
                "x Position has to be 0. Only Tile is at 0,0",
                0,
                newChest.fetch(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .get()
                        .position()
                        .point()
                        .x,
                0.00001f);
        assertEquals(
                "y Position has to be 0. Only Tile is at 0,0",
                0,
                newChest.fetch(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .get()
                        .position()
                        .point()
                        .y,
                0.00001f);
    }
}
