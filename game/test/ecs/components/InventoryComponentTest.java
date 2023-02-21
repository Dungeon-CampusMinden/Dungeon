package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ecs.entities.Entity;
import ecs.items.Item;
import org.junit.Test;

public class InventoryComponentTest {
    /** simple Item implementation since Item is abstract and can´t be used for testing otherwise */
    private static class ItemImpl extends Item {}

    /** constructor should create the inventory with the given parameters. */
    @Test
    public void validCreation() {

        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** Adding one valid Item */
    @Test
    public void addItemValid() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        assertTrue(ic.addItem(item));
        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /**
     * when there is enough space in the Inventory it should be possible to add more than one Item
     */
    @Test
    public void addItemValidMultiple() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 3);
        ic.addItem(new ItemImpl());
        assertTrue(ic.addItem(new ItemImpl()));

        assertEquals(2, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(3, ic.getMaxSize());
    }

    /** Adding two Items to an Inventory with a size of 1 should only add the first */
    @Test
    public void addItemOverSize() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ic.addItem(new ItemImpl());
        assertFalse(ic.addItem(new ItemImpl()));
        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** removing of an added Item */
    @Test
    public void removeItemExisting() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        assertTrue(ic.removeItem(item));

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** removing an Item which was already removed before */
    @Test
    public void removeItemTwice() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        ic.removeItem(item);
        assertFalse(ic.removeItem(item));

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** null should not remove any Item */
    @Test
    public void removeItemNull() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        assertFalse(ic.removeItem(null));

        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }
}
