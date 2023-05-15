package contrib.entities;

import static org.junit.Assert.*;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemDataGenerator;

import core.Component;
import core.Entity;
import core.Game;
import core.components.*;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class ChestTest {

    /** Helper cleans up class attributes used by Chest Initializes the Item#ITEM_REGISTER */
    private static void cleanup() {
        Game.getDelayedEntitySet().clear();
    }

    /** checks the correct creation of the Chest */
    @Test
    public void checkCreation() {
        cleanup();
        List<ItemData> itemData = List.of();
        Point position = new Point(0, 0);
        Entity c = EntityFactory.getChest(itemData, position);
        Game.getDelayedEntitySet().update();
        assertEquals("Chest is added to Game", 1, Game.getEntities().size());
        assertTrue(
                "Needs the AnimationComponent to be visible to the player.",
                c.getComponent(DrawComponent.class).isPresent());
        Optional<Component> inventoryComponent = c.getComponent(InventoryComponent.class);
        assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
        assertEquals(
                "Chest should have the given Items",
                itemData,
                inventoryComponent.map(InventoryComponent.class::cast).get().getItems());
        Optional<Component> positionComponent = c.getComponent(PositionComponent.class);
        assertTrue(
                "Needs the PositionComponent to be somewhere in the Level",
                positionComponent.isPresent());
        assertEquals(
                "Position should be equal to the given Position",
                position,
                positionComponent.map(PositionComponent.class::cast).get().getPosition());
        cleanup();
    }

    /** checks the Chest Dropping all the Items it holds */
    @Test
    public void checkInteractionDroppingItems() {
        cleanup();
        List<ItemData> itemData = List.of(new ItemDataGenerator().generateItemData());
        Point position = new Point(0, 0);
        Entity c = EntityFactory.getChest(itemData, position);
        Game.getDelayedEntitySet().update();

        assertEquals(1, Game.getEntities().size());
        c.getComponent(InteractionComponent.class)
                .map(InteractionComponent.class::cast)
                .get()
                .triggerInteraction();
        Game.getDelayedEntitySet().update();
        assertEquals(2, Game.getEntities().size());

        cleanup();
    }

    /** checks the dropped Item */
    @Test
    public void checkInteractionOnDroppedItems() {
        cleanup();
        List<ItemData> itemData = List.of(new ItemDataGenerator().generateItemData());
        Point position = new Point(0, 0);
        Entity c = EntityFactory.getChest(itemData, position);
        Game.removeEntity(c);
        Game.getDelayedEntitySet().update();
        assertEquals(0, Game.getEntities().size());
        c.getComponent(InteractionComponent.class)
                .map(InteractionComponent.class::cast)
                .ifPresent(InteractionComponent::triggerInteraction);
        Game.getDelayedEntitySet().update();
        assertEquals(1, Game.getEntities().size());
        Entity droppedItem = Game.getEntities().iterator().next();
        assertTrue(
                "droppedItem should have the HitboxComponent",
                droppedItem
                        .getComponent(CollideComponent.class)
                        .map(CollideComponent.class::cast)
                        .isPresent());

        cleanup();
    }

    @Test
    public void checkGeneratorMethod() {
        cleanup();
        Game.currentLevel =
                new TileLevel(
                        new LevelElement[][] {
                            new LevelElement[] {
                                LevelElement.FLOOR,
                            }
                        },
                        DesignLabel.DEFAULT);

        Entity newChest = EntityFactory.getChest();
        Game.getDelayedEntitySet().update();

        assertTrue("Chest is added to Game", Game.getEntities().contains(newChest));
        assertTrue(
                "Needs the AnimationComponent to be visible to the player.",
                newChest.getComponent(DrawComponent.class).isPresent());
        Optional<Component> inventoryComponent = newChest.getComponent(InventoryComponent.class);
        assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
        assertTrue(
                "Chest should have atleast 1 Item",
                1
                        <= inventoryComponent
                                .map(InventoryComponent.class::cast)
                                .get()
                                .getItems()
                                .size());
        assertEquals(
                "x Position has to be 0. Only Tile is at 0,0",
                0,
                newChest.getComponent(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .get()
                        .getPosition()
                        .x,
                0.00001f);
        assertEquals(
                "y Position has to be 0. Only Tile is at 0,0",
                0,
                newChest.getComponent(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .get()
                        .getPosition()
                        .y,
                0.00001f);
        cleanup();
    }
}
