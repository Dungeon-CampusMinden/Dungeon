package contrib.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import contrib.utils.components.item.ItemData;

import core.Entity;
import core.Game;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

public class InventoryComponentTest {

    @After
    public void cleanup() {
        Game.removeAllEntities();
    }

    /** constructor should create the inventory with the given parameters. */
    @Test
    public void validCreation() {

        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        assertEquals(0, ic.count());
    }

    /** Adding one valid Item */
    @Test
    public void addItemValid() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        assertTrue(ic.add(itemData));
        assertEquals(1, ic.count());
    }

    /**
     * when there is enough space in the Inventory it should be possible to add more than one Item
     */
    @Test
    public void addItemValidMultiple() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(3);
        e.addComponent(ic);
        ic.add(new ItemData());
        assertTrue(ic.add(new ItemData()));

        assertEquals(2, ic.count());
    }

    /** Adding two Items to an Inventory with a size of 1 should only add the first */
    @Test
    public void addItemOverSize() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ic.add(new ItemData());
        assertFalse(ic.add(new ItemData()));
        assertEquals(1, ic.count());
    }

    /** removing of an added Item */
    @Test
    public void removeItemExisting() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        ic.add(itemData);
        assertTrue(ic.remove(itemData));

        assertEquals(0, ic.count());
    }

    /** removing an Item which was already removed before */
    @Test
    public void removeItemTwice() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        ic.add(itemData);
        ic.remove(itemData);
        assertFalse(ic.remove(itemData));

        assertEquals(0, ic.count());
    }

    /** null should not remove any Item */
    @Test
    public void removeItemNull() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        ic.add(itemData);
        assertFalse(ic.remove(null));

        assertEquals(1, ic.count());
    }

    /** empty inventory should return an empty List */
    @Test
    public void getAllItemsEmptyInventory() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(0);
        e.addComponent(ic);
        Set<ItemData> list = ic.items();
        assertEquals("should have no Items", 0, list.size());
    }

    /** an inventory with one Item should return a List with this Item */
    @Test
    public void getAllItemsInventoryWithOnlyOneItem() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        ic.add(itemData);
        Set<ItemData> list = ic.items();
        assertEquals("should have one Item", 1, list.size());
        assertTrue("Item should be in returned List", list.contains(itemData));
    }

    /** an inventory with one Item should return a List with this Item */
    @Test
    public void getAllItemsInventoryWithTwoItems() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(2);
        e.addComponent(ic);
        ItemData itemData1 = new ItemData();
        ic.add(itemData1);
        ItemData itemData2 = new ItemData();
        ic.add(itemData2);
        Set<ItemData> list = ic.items();
        assertEquals("should have two Items", 2, list.size());
        assertTrue("Item 1 should be in returned List", list.contains(itemData1));
        assertTrue("Item 2 should be in returned List", list.contains(itemData2));
    }

    /** an inventory should only be able to return Items it contains */
    @Test
    public void getAllItemsInventoryNoAddedItemButCreated() {
        Entity e = new Entity();
        InventoryComponent ic = new InventoryComponent(1);
        e.addComponent(ic);
        ItemData itemData = new ItemData();
        Set<ItemData> list = ic.items();
        assertEquals("should have no Items", 0, list.size());
        assertFalse("Item should be in returned List", list.contains(itemData));
    }

    @Test
    public void tranfserItem() {
        InventoryComponent ic = new InventoryComponent(1);
        InventoryComponent other = new InventoryComponent(1);
        ItemData item = Mockito.mock(ItemData.class);
        ic.add(item);
        assertTrue("Item should be in the inventory.", ic.items().contains(item));
        assertTrue("Transfer should be successfully.", ic.transfer(item, other));
        assertTrue("Item should now be in the other inventory.", other.items().contains(item));
        assertFalse("Item should be removed from this inventroy.", ic.items().contains(item));
    }

    @Test
    public void tranfserItemNoSpace() {
        InventoryComponent ic = new InventoryComponent(1);
        InventoryComponent other = new InventoryComponent(0);
        ItemData item = Mockito.mock(ItemData.class);
        ic.add(item);
        assertTrue("Item should be in the inventory.", ic.items().contains(item));
        assertFalse("Other inventory is full, no transfer possible", ic.transfer(item, other));
        assertFalse("Item should not be transfered", other.items().contains(item));
        assertTrue("Item should still be in tis inventroy.", ic.items().contains(item));
    }

    @Test
    public void tranfserItemNoItem() {
        InventoryComponent ic = new InventoryComponent(1);
        InventoryComponent other = new InventoryComponent(1);
        ItemData item = Mockito.mock(ItemData.class);
        assertFalse("No item, no transfer", ic.transfer(item, other));
    }

    @Test
    public void transferItemToItself() {
        InventoryComponent ic = new InventoryComponent(1);
        ItemData item = Mockito.mock(ItemData.class);
        ic.add(item);
        assertTrue("Item should be in the inventory.", ic.items().contains(item));
        assertFalse("Can not transfer item to itself.", ic.transfer(item, ic));
        assertTrue("Item should still be in tis inventroy.", ic.items().contains(item));
    }
}
