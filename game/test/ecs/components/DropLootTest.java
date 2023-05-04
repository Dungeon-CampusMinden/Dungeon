package ecs.components;

import static org.junit.Assert.*;

import ecs.entities.Entity;
import ecs.items.ItemData;
import org.junit.Test;
import starter.Game;
import tools.Point;

public class DropLootTest {
    /** Checks the handling when the InventoryComponent is missing on the entity */
    @Test
    public void entityMissingInventoryComponent() {
        DropLoot dropLoot = new DropLoot();
        MissingComponentException mce =
                assertThrows(MissingComponentException.class, () -> dropLoot.onDeath(new Entity()));
        assertTrue(mce.getMessage().contains(InventoryComponent.class.getName()));
        assertTrue(mce.getMessage().contains(DropLoot.class.getName()));
    }

    /** Checks the handling when the PositionComponent is missing on the entity */
    @Test
    public void entityMissingPositionComponent() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        new InventoryComponent(entity, 10);
        MissingComponentException mce =
                assertThrows(MissingComponentException.class, () -> dropLoot.onDeath(entity));
        assertTrue(mce.getMessage().contains(PositionComponent.class.getName()));
        assertTrue(mce.getMessage().contains(DropLoot.class.getName()));
    }

    /** Checks the handling when the InventoryComponent has no Items */
    @Test
    public void entityInventoryComponentEmpty() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        new PositionComponent(entity, new Point(1, 2));
        new InventoryComponent(entity, 10);
        Game.getEntities().clear();
        dropLoot.onDeath(entity);
        assertTrue(Game.getEntities().isEmpty());
    }

    /** Checks the handling when the InventoryComponent has exactly one Item */
    @Test
    public void entityInventoryComponentOneItem() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        Point entityPosition = new Point(1, 2);
        new PositionComponent(entity, entityPosition);
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 10);
        inventoryComponent.addItem(new ItemData());
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        Game.getEntities().clear();
        dropLoot.onDeath(entity);
        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        assertEquals(1, Game.getEntities().size());
        assertTrue(
                Game.getEntities().stream()
                        .allMatch(
                                x ->
                                        x.getComponent(PositionComponent.class)
                                                .map(
                                                        component ->
                                                                isPointEqual(
                                                                        entityPosition,
                                                                        (PositionComponent)
                                                                                component))
                                                .orElse(false)));
    }

    /** Checks the handling when the InventoryComponent has more than one Item */
    @Test
    public void entityInventoryComponentMultipleItems() {
        DropLoot dropLoot = new DropLoot();
        Entity entity = new Entity();
        Point entityPosition = new Point(1, 2);
        new PositionComponent(entity, entityPosition);
        InventoryComponent inventoryComponent = new InventoryComponent(entity, 10);
        inventoryComponent.addItem(new ItemData());
        inventoryComponent.addItem(new ItemData());

        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        Game.getEntities().clear();
        dropLoot.onDeath(entity);

        Game.getEntities().addAll(Game.getEntitiesToAdd());
        Game.getEntitiesToAdd().clear();
        assertEquals(2, Game.getEntities().size());
        assertTrue(
                Game.getEntities().stream()
                        .allMatch(
                                x ->
                                        x.getComponent(PositionComponent.class)
                                                .map(
                                                        component ->
                                                                isPointEqual(
                                                                        entityPosition,
                                                                        (PositionComponent)
                                                                                component))
                                                .orElse(false)));
    }

    /**
     * Helpermethod, checks Points for same values for x and y
     *
     * @param entityPosition the Position of the Entity itself
     * @return a function which returns true when the Points are equal, otherwise false
     */
    private static boolean isPointEqual(Point entityPosition, PositionComponent component) {
        Point a = component.getPosition();
        return a.x == entityPosition.x && a.y == entityPosition.y;
    }
}
