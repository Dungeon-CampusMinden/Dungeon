package ecs.entities;

import static org.junit.Assert.*;

import ecs.components.*;
import ecs.items.BasicItem;
import ecs.items.Item;
import java.util.List;
import java.util.Optional;
import level.elements.TileLevel;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import org.junit.Test;
import starter.Game;
import tools.Point;

public class ChestTest {

    /** Helper cleans up class attributes used by Chest Initializes the Item#ITEM_REGISTER */
    private static void cleanup() {
        Game.entities.clear();
        Item.ITEM_REGISTER.clear();
        BasicItem.FillRegister();
    }

    /** checks the correct creation of the Chest */
    @Test
    public void checkCreation() {
        cleanup();
        List<Item> items = List.of();
        Point position = new Point(0, 0);
        Chest c = new Chest(items, position);
        assertEquals("Chest is added to Game", 1, Game.entities.size());
        assertTrue(
                "Needs the AnimationComponent to be visible to the player.",
                c.getComponent(AnimationComponent.class).isPresent());
        Optional<Component> inventoryComponent = c.getComponent(InventoryComponent.class);
        assertTrue("Needs the InventoryComponent to be a chest", inventoryComponent.isPresent());
        assertEquals(
                "Chest should have the given Items",
                items,
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

        List<Item> items = List.of(Item.ITEM_REGISTER.get(0));
        Point position = new Point(0, 0);
        Chest c = new Chest(items, position);
        assertEquals(1, Game.entities.size());
        c.getComponent(InteractionComponent.class)
                .map(InteractionComponent.class::cast)
                .get()
                .triggerInteraction();
        assertEquals(2, Game.entities.size());

        cleanup();
    }

    /** checks the dropped Item */
    @Test
    public void checkInteractionOnDroppedItems() {
        List<Item> items = List.of(Item.ITEM_REGISTER.get(0));
        Point position = new Point(0, 0);
        Chest c = new Chest(items, position);
        Game.entities.remove(c);
        assertEquals(0, Game.entities.size());
        c.getComponent(InteractionComponent.class)
                .map(InteractionComponent.class::cast)
                .ifPresent(InteractionComponent::triggerInteraction);
        assertEquals(1, Game.entities.size());
        Entity droppedItem = Game.entities.iterator().next();
        assertTrue(
                "droppedItem should have the HitboxComponent",
                droppedItem
                        .getComponent(HitboxComponent.class)
                        .map(HitboxComponent.class::cast)
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
        Chest newChest = Chest.createNewChest();
        assertTrue("Chest is added to Game", Game.entities.contains(newChest));
        assertTrue(
                "Needs the AnimationComponent to be visible to the player.",
                newChest.getComponent(AnimationComponent.class).isPresent());
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
