package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ecs.entities.Entity;
import ecs.items.ItemData;
import java.util.List;
import org.junit.Test;

public class InventoryComponentTest {
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
        ItemData itemData = new ItemData();
        assertTrue(ic.addItem(itemData));
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
        ic.addItem(new ItemData());
        assertTrue(ic.addItem(new ItemData()));

        assertEquals(2, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(3, ic.getMaxSize());
    }

    /** Adding two Items to an Inventory with a size of 1 should only add the first */
    @Test
    public void addItemOverSize() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ic.addItem(new ItemData());
        assertFalse(ic.addItem(new ItemData()));
        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** removing of an added Item */
    @Test
    public void removeItemExisting() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ItemData itemData = new ItemData();
        ic.addItem(itemData);
        assertTrue(ic.removeItem(itemData));

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** removing an Item which was already removed before */
    @Test
    public void removeItemTwice() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ItemData itemData = new ItemData();
        ic.addItem(itemData);
        ic.removeItem(itemData);
        assertFalse(ic.removeItem(itemData));

        assertEquals(0, ic.filledSlots());
        assertEquals(1, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** null should not remove any Item */
    @Test
    public void removeItemNull() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ItemData itemData = new ItemData();
        ic.addItem(itemData);
        assertFalse(ic.removeItem(null));

        assertEquals(1, ic.filledSlots());
        assertEquals(0, ic.emptySlots());
        assertEquals(1, ic.getMaxSize());
    }

    /** empty inventory should return an empty List */
    @Test
    public void getAllItemsEmptyInventory() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 0);
        List<ItemData> list = ic.getItems();
        assertEquals("should have no Items", 0, list.size());
    }

    /** an inventory with one Item should return a List with this Item */
    @Test
    public void getAllItemsInventoryWithOnlyOneItem() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ItemData itemData = new ItemData();
        ic.addItem(itemData);
        List<ItemData> list = ic.getItems();
        assertEquals("should have one Item", 1, list.size());
        assertTrue("Item should be in returned List", list.contains(itemData));
    }

    /** an inventory with one Item should return a List with this Item */
    @Test
    public void getAllItemsInventoryWithTwoItems() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 2);
        ItemData itemData1 = new ItemData();
        ic.addItem(itemData1);
        ItemData itemData2 = new ItemData();
        ic.addItem(itemData2);
        List<ItemData> list = ic.getItems();
        assertEquals("should have two Items", 2, list.size());
        assertTrue("Item 1 should be in returned List", list.contains(itemData1));
        assertTrue("Item 2 should be in returned List", list.contains(itemData2));
    }

    /** an inventory should only be able to return Items it contains */
    @Test
    public void getAllItemsInventoryNoAddedItemButCreated() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(e, 1);
        ItemData itemData = new ItemData();
        List<ItemData> list = ic.getItems();
        assertEquals("should have no Items", 0, list.size());
        assertFalse("Item should be in returned List", list.contains(itemData));
    }
}
