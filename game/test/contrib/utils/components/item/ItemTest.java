package contrib.utils.components.item;

import static org.junit.Assert.*;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.Animation;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class ItemTest {

    Animation defaultAnimation = Animation.of("animation/missing_texture.png");
    Animation worldAnimation = Animation.of("item/key/gold_key");
    Animation inventoryAnimation = Animation.of("item/key/red_key");

    @Before
    public void before() {
        Game.removeAllEntities();
    }

    @Test
    public void testConstructorThreeParameter() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        assertEquals(item.displayName(), "Test item");
        assertEquals(item.description(), "Test description");
        assertEquals(item.inventoryAnimation(), defaultAnimation);
        assertEquals(item.worldAnimation(), defaultAnimation);
        assertEquals(1, item.stackSize());
        assertEquals(1, item.maxStackSize());
    }

    @Test
    public void testConstructorFourParameter() {
        Item item =
                new Item(
                        "Test item 2",
                        "Another test description",
                        inventoryAnimation,
                        worldAnimation);
        assertEquals(item.displayName(), "Test item 2");
        assertEquals(item.description(), "Another test description");
        assertEquals(item.inventoryAnimation(), inventoryAnimation);
        assertEquals(item.worldAnimation(), worldAnimation);
        assertEquals(1, item.stackSize());
        assertEquals(1, item.maxStackSize());
    }

    @Test
    public void testConstructorSixParameter() {
        Item item =
                new Item(
                        "Test item 3",
                        "More description",
                        inventoryAnimation,
                        worldAnimation,
                        2,
                        6);
        assertEquals(item.displayName(), "Test item 3");
        assertEquals(item.description(), "More description");
        assertEquals(item.inventoryAnimation(), inventoryAnimation);
        assertEquals(item.worldAnimation(), worldAnimation);
        assertEquals(2, item.stackSize());
        assertEquals(6, item.maxStackSize());
    }

    @Test
    public void testDisplayName() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.displayName("New Display Name");

        assertEquals(item.displayName(), "New Display Name");
    }

    @Test
    public void testDescription() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.description("New Description");

        assertEquals(item.description(), "New Description");
    }

    @Test
    public void testInventoryAnimation() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.inventoryAnimation(inventoryAnimation);

        assertEquals(item.inventoryAnimation(), inventoryAnimation);
    }

    @Test
    public void testWorldAnimation() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.worldAnimation(worldAnimation);

        assertEquals(item.worldAnimation(), worldAnimation);
    }

    @Test
    public void testStackSize() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.stackSize(2);

        assertEquals(2, item.stackSize());
    }

    @Test
    public void testMaxStackSize() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.maxStackSize(8);

        assertEquals(8, item.maxStackSize());
    }

    /** Tests if item is removed from inventory and present in Game world after drop. */
    @Test
    public void testDrop() {
        Item item = new Item("Test item", "Test description", defaultAnimation);

        Point point = new Point(3, 3);
        item.drop(new Entity(), point);
        assertEquals("There should only be one entity in the game", 1, Game.entityStream().count());
        Entity worldItem = Game.entityStream().findFirst().get();
        assertTrue(worldItem.isPresent(PositionComponent.class));
        assertTrue(worldItem.fetch(PositionComponent.class).get().position().equals(point));
        assertTrue(worldItem.isPresent(DrawComponent.class));
        assertTrue(worldItem.isPresent(InteractionComponent.class));
    }

    /** Tests if item is present in inventory and removed from Game world after collect */
    @Test
    public void testCollect() {
        assertEquals("There should be no entity in the game", 0, Game.entityStream().count());

        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.drop(null, new Point(0, 0));
        assertEquals("There should only be one entity in the game", 1, Game.entityStream().count());
        Entity collector = new Entity();
        collector.addComponent(new InventoryComponent(3));
        Entity worldItem = Game.entityStream().findFirst().get();

        assertTrue(item.collect(worldItem, collector));

        assertTrue(
                collector
                        .fetch(InventoryComponent.class)
                        .map(inventoryComponent -> inventoryComponent.hasItem(item))
                        .get());

        assertEquals("There should be no item in the gameworld.", 0, Game.entityStream().count());
    }

    /** Tests if item can be collected from entity with no InventoryComponent. */
    @Test
    public void testCollectNoInventory() {
        assertEquals("There should be no entity in the game", 0, Game.entityStream().count());

        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.drop(null, new Point(0, 0));
        assertEquals("There should only be one entity in the game", 1, Game.entityStream().count());
        Entity collector = new Entity();
        Entity worldItem = Game.entityStream().findFirst().get();

        assertFalse(item.collect(worldItem, collector));
        assertEquals(
                "There should still be the item in the gameworld.", 1, Game.entityStream().count());
    }

    /** Tests if item can be collected from entity with full inventory. */
    @Test
    public void testCollectFullInventory() {
        assertEquals("There should be no entity in the game", 0, Game.entityStream().count());

        Item item = new Item("Test item", "Test description", defaultAnimation);
        item.drop(null, new Point(0, 0));
        assertEquals("There should only be one entity in the game", 1, Game.entityStream().count());
        Entity collector = new Entity();
        collector.addComponent(new InventoryComponent(0));
        Entity worldItem = Game.entityStream().findFirst().get();

        assertFalse(item.collect(worldItem, collector));
        assertEquals(
                "There should still be the item in the gameworld.", 1, Game.entityStream().count());
    }

    /** Tests if item is removed from inventory after use. */
    @Test
    public void testUse() {
        Item item = new Item("Test item", "Test description", defaultAnimation);
        Entity entity = new Entity();
        InventoryComponent inventoryComponent = new InventoryComponent(2);
        entity.addComponent(inventoryComponent);
        inventoryComponent.add(item);
        assertTrue(
                "ItemActive needs to be in entities inventory.",
                Arrays.asList(inventoryComponent.items()).contains(item));
        item.use(entity);
        assertFalse(
                "Item was not removed from inventory after use.",
                Arrays.asList(inventoryComponent.items()).contains(item));
    }
}
