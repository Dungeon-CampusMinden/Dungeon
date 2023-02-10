package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ecs.entities.Entity;
import ecs.items.Item;
import org.junit.Test;

public class InventoryComponentTest {
    private class ItemImpl extends Item {}

    /** makes sure the creation of the component works. */
    @Test
    public void validCreation() {

        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

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

    @Test
    public void removeItemExisting() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        ic.removeItem(item);

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    @Test
    public void removeItemTwice() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        ic.removeItem(item);
        ic.removeItem(item);

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    @Test
    public void removeItemNull() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        Item item = new ItemImpl();
        ic.addItem(item);
        ic.removeItem(null);

        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }
}
